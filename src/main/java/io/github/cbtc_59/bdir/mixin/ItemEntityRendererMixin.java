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
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import java.util.Random;
@Mixin(ItemEntityRenderer.class)
public abstract class ItemEntityRendererMixin extends EntityRenderer<ItemEntity> {
    @Shadow
    @Final
    private ItemRenderer itemRenderer;
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
        // 计算随机种子
        long seed;
        if (itemStack.isEmpty()) {
            seed = 187;
        } else {
            seed = Registries.ITEM.getRawId(item) + itemStack.getDamage();
        }
        Random random = new Random(seed);
        matrix.push();
        // 获取模型
        BakedModel bakedModel = itemRenderer.getModel(itemStack, dropped.getWorld(), null, 0);
        boolean is3DModel = bakedModel.hasDepth();
        int renderCount = getRenderedAmount(itemStack);
        ItemEntityRotator rotator = (ItemEntityRotator) dropped;
        // 获取物品的ground渲染变换，用于检测渲染高度
        var transform = bakedModel.getTransformation();
        // 物品分类判断：检测渲染高度
        boolean shouldRotateRender = true;  // 默认开启旋转渲染（立起来）
        float blockHeight = 0.0F;  // 初始化方块高度
        
        // 判断是否应该旋转
        if (is3DModel) {
            Block block = ((BlockItem) item).getBlock();
            World world = dropped.getWorld();
            BlockPos blockPos = dropped.getBlockPos();
            var shape = block.getDefaultState().getOutlineShape(world, blockPos);
            blockHeight = (float) shape.getMax(Direction.Axis.Y);

            // 两个条件都满足的话，关闭旋转渲染（保持平放）
            if (blockHeight <= 0.5F) {
                shouldRotateRender = false;
            }
        }
        
        // 调试输出：在游戏聊天栏打印物品的详细信息（每秒一次）
        if (dropped.age % 60 == 0 && BetterDroppedItems.CONFIG.debugMode) {
            String msg = String.format("[BDI调试] 物品: %s | 方块高度: %.4f | 是否旋转: %b | 是否为3D模型: %b | 堆叠数: %d",
                item.getName().getString(), blockHeight, shouldRotateRender, is3DModel, itemStack.getCount());
            MinecraftClient.getInstance().inGameHud.getChatHud().addMessage(Text.literal(msg));
        }

        // 在旋转90度前对物品的渲染位置进行调整
        matrix.translate(0, -0.0625, 0);

        // 立起旋转：只有开启旋转渲染的物品才执行
        if (shouldRotateRender) {
            matrix.translate(0, 0.1875, 0);
            // 修改为负值：绕X轴向前旋转90度
            matrix.multiply(new Quaternionf().fromAxisAngleRad(new Vector3f(1, 0, 0), -(float) Math.PI / 2));
            matrix.translate(0, -0.1875, 0);
        }

        // 状态分支处理
        boolean isAboveWater = dropped.getWorld().getBlockState(dropped.getBlockPos().up()).getBlock() == Blocks.WATER;
        if (!dropped.isOnGround() && !dropped.isSubmergedInWater() && !isAboveWater) {
            // 空中旋转
            float rotation = ((float) dropped.age + partialTicks) / 20.0F + dropped.getHeight();
            if (shouldRotateRender) {
                // 立起的物品：绕Z轴旋转
                matrix.translate(0, 0.1875, 0);
                matrix.multiply(new Quaternionf().fromAxisAngleRad(new Vector3f(0, 0, 1), rotation));
                matrix.translate(0, -0.1875, 0);
                rotator.setRotation(new Vec3d(0, 0, rotation));
            } else {
                // 平放的物品：绕Y轴旋转
                matrix.multiply(new Quaternionf().fromAxisAngleRad(new Vector3f(0, 1, 0), rotation));
                rotator.setRotation(new Vec3d(0, rotation, 0));
            }
        } else {
            // 落地状态：保持之前的旋转角度
            if (shouldRotateRender) {
                matrix.translate(0, 0.1875, 0);
                matrix.multiply(new Quaternionf().fromAxisAngleRad(new Vector3f(0, 0, 1), (float) rotator.getRotation().z));
                matrix.translate(0, -0.1875, 0);
            } else {
                // 平放的物品：绕Y轴保持旋转
                matrix.multiply(new Quaternionf().fromAxisAngleRad(new Vector3f(0, 1, 0), (float) rotator.getRotation().y));
            }
        }

        // 对2D物品进行微调
        if (!is3DModel){
            matrix.translate(0, 0.0625, -0.109375);
        }

        // 特殊方块修正
        World world = dropped.getWorld();
        BlockPos blockPos = dropped.getBlockPos();
        if (world.getBlockState(blockPos).getBlock() == Blocks.SOUL_SAND) {
            double soulSandItemHeight = 0.003;
            if (!is3DModel){
                matrix.translate(0, 0, 0.09375 + soulSandItemHeight);
            }
            if (!shouldRotateRender){
                matrix.translate(0, 0.125 - (blockHeight / 4) + soulSandItemHeight, 0);
            }
        }
        if (item instanceof BlockItem && ((BlockItem)item).getBlock() instanceof SkullBlock) {
            matrix.translate(0, 0.1275, 0);
        }

        // 堆叠渲染准备
        float scaleX = transform.ground.scale.x;
        float scaleY = transform.ground.scale.y;
        float scaleZ = transform.ground.scale.z;

        // 循环渲染每个物品模型
        for (int u = 0; u < renderCount; u++) {
            matrix.push();
            if (u > 0) {
                if (is3DModel) {
                    // 3D模型：三轴随机偏移
                    float x = (random.nextFloat() * 2.0F - 1.0F) * 0.15F;
                    float y = (random.nextFloat() * 2.0F - 1.0F) * 0.15F;
                    float z = (random.nextFloat() * 2.0F - 1.0F) * 0.15F;
                    matrix.translate(x, y, z);
                } else {
                    // 2D模型：两轴偏移+随机旋转
                    float x = (random.nextFloat() * 2.0F - 1.0F) * 0.15F * 0.5F;
                    float y = (random.nextFloat() * 2.0F - 1.0F) * 0.15F * 0.5F;
                    matrix.translate(x, y, 0.0F);
                    matrix.multiply(new Quaternionf().fromAxisAngleRad(new Vector3f(0, 0, 1), random.nextFloat()));
                }
            }
            // 渲染单个物品
            itemRenderer.renderItem(
                    itemStack,
                    ModelTransformationMode.GROUND,
                    false,
                    matrix,
                    vertexConsumerProvider,
                    light,
                    OverlayTexture.DEFAULT_UV,
                    bakedModel
            );
            matrix.pop();
            // 垂直分层
            if (!is3DModel) {
                matrix.translate(0.0F, 0.0F, 0.0625F * scaleZ);
            }
        }
        matrix.pop();
        callback.cancel();
    }
    // 计算需要渲染的物品数量（原版逻辑）
    private static int getRenderedAmount(ItemStack stack) {
        if (stack.getCount() == 1) return 1;
        if (stack.getCount() <= 16) return 2;
        if (stack.getCount() <= 32) return 3;
        if (stack.getCount() <= 48) return 4;
        return 5;
    }
}