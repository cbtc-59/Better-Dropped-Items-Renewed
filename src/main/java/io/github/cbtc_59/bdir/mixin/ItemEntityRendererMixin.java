package io.github.cbtc_59.bdir.mixin;
import io.github.cbtc_59.bdir.BetterDroppedItems;
import io.github.cbtc_59.bdir.util.ItemEntityRotator;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.SkullBlock;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.ItemEntityRenderer;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.ItemEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import org.joml.Vector3f;
import org.joml.Quaternionf;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import java.util.Random;
@Mixin(ItemEntityRenderer.class)
public abstract class ItemEntityRendererMixin extends EntityRenderer<ItemEntity> {
    @Shadow
    @Final
    private ItemRenderer itemRenderer;

    @Unique
    private final Random bdiRandom = new Random();
    @Unique
    private final Quaternionf bdiQuat = new Quaternionf();
    @Unique
    private final Vector3f bdiVec = new Vector3f();
    @Unique
    private static final java.util.Map<Block, Float> blockHeightCache = new java.util.IdentityHashMap<>();
    protected ItemEntityRendererMixin(EntityRendererFactory.Context ctx) {
        super(ctx);
    }
    @Inject(method = "<init>", at = @At("TAIL"))
    private void init(EntityRendererFactory.Context context, CallbackInfo ci) {
        // 移除地面阴影
        this.shadowRadius = 0;
    }
    @Inject(method = "render(Lnet/minecraft/entity/ItemEntity;FFLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V", at = @At("HEAD"), cancellable = true)
    private void render(ItemEntity dropped, float f, float partialTicks, MatrixStack matrix, net.minecraft.client.render.VertexConsumerProvider vertexConsumerProvider, int light, CallbackInfo callback) {
        ItemStack itemStack = dropped.getStack();
        Item item = itemStack.getItem();
        World world = dropped.getWorld();
        BlockPos blockPos = dropped.getBlockPos();
        MinecraftClient client = MinecraftClient.getInstance();

        long seed;
        if (itemStack.isEmpty()) {
            seed = 187;
        } else {
            seed = Registries.ITEM.getRawId(item) + itemStack.getDamage();
        }

        bdiRandom.setSeed(seed);
        matrix.push();
        BakedModel bakedModel = itemRenderer.getModel(itemStack, world, null, 0);
        boolean is3DModel = bakedModel.hasDepth() && item instanceof BlockItem;
        int renderCount = getRenderedAmount(itemStack);
        ItemEntityRotator rotator = (ItemEntityRotator) dropped;
        // 获取物品的ground渲染变换，用于检测渲染高度
        var transform = bakedModel.getTransformation();
        boolean shouldRotateRender = true;  // 默认开启旋转渲染（立起来）
        float blockHeight = 0.0F;
        
        if (is3DModel) {
            Block block = ((BlockItem) item).getBlock();
            Float cached = blockHeightCache.get(block);
            if (cached == null) {
                var shape = block.getDefaultState().getOutlineShape(world, blockPos);
                cached = (float) shape.getMax(Direction.Axis.Y);
                blockHeightCache.put(block, cached);
            }
            blockHeight = cached;

            if (blockHeight <= 0.5F) {
                shouldRotateRender = false;
            }
        }

        // 调试输出：每秒一次（使用 age 桶去重，避免多 pass 重复输出）
        int debugBucket = dropped.age / 20;
        if (debugBucket != rotator.bdi$getLastDebugAge() && BetterDroppedItems.CONFIG.debugMode) {
            rotator.bdi$setLastDebugAge(debugBucket);
            String msg = String.format("[BDI调试] 物品: %s | 方块高度: %.4f | 是否旋转: %b | 是否为3D模型: %b | 堆叠数: %d",
                item.getName().getString(), blockHeight, shouldRotateRender, is3DModel, itemStack.getCount());
            client.inGameHud.getChatHud().addMessage(Text.literal(msg));
        }

        matrix.translate(0, -0.0625 /* 1/16 */, 0);

        // 立起旋转：只有开启旋转渲染的物品才执行
        if (shouldRotateRender) {
            rotateAroundPivot(matrix, 1, 0, 0, -(float) Math.PI / 2);
        }

        boolean isAboveWater = world.getBlockState(blockPos.up()).getBlock() == Blocks.WATER;
        if (!dropped.isOnGround() && !dropped.isSubmergedInWater() && !isAboveWater) {
            // 空中旋转（应用配置中的旋转速度倍率和初始方向偏移）
            float speedMultiplier = BetterDroppedItems.CONFIG.rotationSpeed / 100.0F;
            float initialOffset = (float) Math.toRadians(BetterDroppedItems.CONFIG.initialRotationAngle);
            float rotation = (((float) dropped.age + partialTicks) / 20.0F + dropped.getHeight()) * speedMultiplier + initialOffset;
            if (shouldRotateRender) {
                // 立起的物品：绕Z轴旋转
                rotateAroundPivot(matrix, 0, 0, 1, rotation);
                rotator.bdi$setRotation(new Vec3d(0, 0, rotation));
            } else {
                // 平放的物品：绕Y轴旋转
                matrix.multiply(bdiQuat.fromAxisAngleRad(bdiVec.set(0, 1, 0), rotation));
                rotator.bdi$setRotation(new Vec3d(0, rotation, 0));
            }
        } else {
            // 落地状态：保持之前的旋转角度
            if (shouldRotateRender) {
                rotateAroundPivot(matrix, 0, 0, 1, (float) rotator.bdi$getRotation().z);
            } else {
                // 平放的物品：绕Y轴保持旋转
                matrix.multiply(bdiQuat.fromAxisAngleRad(bdiVec.set(0, 1, 0), (float) rotator.bdi$getRotation().y));
            }
        }

        if (!is3DModel){
            matrix.translate(0, 0.0625 /* 1/16 */, -0.109375 /* 7/64 */);
        }

        if (world.getBlockState(blockPos).getBlock() == Blocks.SOUL_SAND) {
            double soulSandItemHeight = 0.003;
            if (!is3DModel){
                matrix.translate(0, 0, 0.09375 /* 3/32 */ + soulSandItemHeight);
            }
            if (!shouldRotateRender){
                matrix.translate(0, 0.125 - (blockHeight / 4) + soulSandItemHeight, 0);
            }
        }
        if (item instanceof BlockItem && ((BlockItem)item).getBlock() instanceof SkullBlock) {
            matrix.translate(0, 0.1275, 0);
        }

        float scaleZ = transform.ground.scale.z;
        if (BetterDroppedItems.CONFIG.itemPhysic2DRenderMode && !is3DModel) {
            // ItemPhysic 2D 堆叠：无随机偏移 + 固定 0.09375 间距 + 预居中
            float spacing = 0.09375F /* 3/32 */;
            if (renderCount > 1) {
                matrix.translate(0, 0, 0.046875F /* 3/64 */);
            }
            matrix.translate(0, 0, -spacing * (renderCount - 1) * 0.5F);
            for (int u = 0; u < renderCount; u++) {
                matrix.push();
                itemRenderer.renderItem(itemStack, ModelTransformationMode.GROUND, false, matrix, vertexConsumerProvider, light, OverlayTexture.DEFAULT_UV, bakedModel);
                matrix.pop();
                matrix.translate(0.0F, 0.0F, spacing);
            }
        } else {
            for (int u = 0; u < renderCount; u++) {
                matrix.push();
                if (u > 0) {
                    if (is3DModel) {
                        // 3D模型：三轴随机偏移
                        float x = (bdiRandom.nextFloat() * 2.0F - 1.0F) * 0.15F;
                        float y = (bdiRandom.nextFloat() * 2.0F - 1.0F) * 0.15F;
                        float z = (bdiRandom.nextFloat() * 2.0F - 1.0F) * 0.15F;
                        matrix.translate(x, y, z);
                    } else {
                        // 2D模型：两轴偏移+随机旋转
                        float x = (bdiRandom.nextFloat() * 2.0F - 1.0F) * 0.15F * 0.5F;
                        float y = (bdiRandom.nextFloat() * 2.0F - 1.0F) * 0.15F * 0.5F;
                        matrix.translate(x, y, 0.0F);
                        matrix.multiply(bdiQuat.fromAxisAngleRad(bdiVec.set(0, 0, 1), bdiRandom.nextFloat()));
                    }
                }
                itemRenderer.renderItem(itemStack, ModelTransformationMode.GROUND, false, matrix, vertexConsumerProvider, light, OverlayTexture.DEFAULT_UV, bakedModel);
                matrix.pop();
                if (!is3DModel) {
                    matrix.translate(0.0F, 0.0F, 0.0625F /* 1/16 */ * scaleZ);
                }
            }
        }
        matrix.pop();
        callback.cancel();
    }
    // 计算需要渲染的物品数量（原版逻辑）
    @Unique
    private static int getRenderedAmount(ItemStack stack) {
        if (stack.getCount() == 1) return 1;
        if (stack.getCount() <= 16) return 2;
        if (stack.getCount() <= 32) return 3;
        if (stack.getCount() <= 48) return 4;
        return 5;
    }

    @Unique
    private void rotateAroundPivot(MatrixStack matrix, float x, float y, float z, float angle) {
        matrix.translate(0, 0.1875 /* 3/16 */, 0);
        matrix.multiply(bdiQuat.fromAxisAngleRad(bdiVec.set(x, y, z), angle));
        matrix.translate(0, -0.1875 /* 3/16 */, 0);
    }
}