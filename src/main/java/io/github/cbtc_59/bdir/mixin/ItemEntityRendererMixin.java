package io.github.cbtc_59.bdir.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import io.github.cbtc_59.bdir.BetterDroppedItems;
import io.github.cbtc_59.bdir.util.ItemEntityRenderStateExtender;
import io.github.cbtc_59.bdir.util.ItemEntityRotator;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.ItemEntityRenderer;
import net.minecraft.client.renderer.entity.state.ItemEntityRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SkullBlock;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemEntityRenderer.class)
public abstract class ItemEntityRendererMixin {

    @Shadow
    @Final
    private RandomSource random;
    @Unique
    private static final java.util.Map<Block, Float> blockHeightCache = new java.util.IdentityHashMap<>();

    /**
     * 在提取渲染状态时计算并存储我们的自定义数据
     */
    @Inject(method = "extractRenderState(Lnet/minecraft/world/entity/item/ItemEntity;Lnet/minecraft/client/renderer/entity/state/ItemEntityRenderState;F)V",
            at = @At("TAIL"))
    private void extractOurState(ItemEntity entity, ItemEntityRenderState state, float partialTicks, CallbackInfo ci) {
        ItemEntityRenderStateExtender ext = (ItemEntityRenderStateExtender) state;
        ItemEntityRotator rotator = (ItemEntityRotator) entity;
        ItemStack itemStack = entity.getItem();
        Item item = itemStack.getItem();

        // 判断是否3D模型（方块物品）
        boolean is3DModel = item instanceof BlockItem;
        ext.bdi$set3DModel(is3DModel);

        // 方块高度检测
        float blockHeight = 0.0F;
        boolean shouldRotateRender = true;
        if (is3DModel) {
            Block block = ((BlockItem) item).getBlock();
            Float cached = blockHeightCache.get(block);
            if (cached == null) {
                Level level = entity.level();
                BlockPos pos = entity.blockPosition();
                var shape = block.defaultBlockState().getShape(level, pos);
                cached = (float) shape.max(Direction.Axis.Y);
                blockHeightCache.put(block, cached);
            }
            blockHeight = cached;
            if (blockHeight <= 0.5F) {
                shouldRotateRender = false;
            }
        }
        ext.bdi$setBlockHeight(blockHeight);
        ext.bdi$setShouldRotateRender(shouldRotateRender);

        // 调试输出
        int debugBucket = entity.getAge() / 20;
        if (debugBucket != rotator.bdi$getLastDebugAge() && BetterDroppedItems.CONFIG.debugMode) {
            rotator.bdi$setLastDebugAge(debugBucket);
            String msg = String.format("[BDI] %s h=%.2f rot=%b 3D=%b n=%d",
                item.getName(itemStack).getString(), blockHeight, shouldRotateRender, is3DModel, itemStack.getCount());
            BetterDroppedItems.LOGGER.info("[BDI] {}", msg);
        }

        // 旋转计算
        boolean isAboveWater = entity.level().getBlockState(entity.blockPosition().above()).getBlock() == Blocks.WATER;
        if (!entity.onGround() && !entity.isInWater() && !isAboveWater) {
            float speedMultiplier = BetterDroppedItems.CONFIG.rotationSpeed / 100.0F;
            float initialOffset = (float) Math.toRadians(BetterDroppedItems.CONFIG.initialRotationAngle);
            float rotation = (state.ageInTicks / 20.0F + state.bobOffset) * speedMultiplier + initialOffset;
            if (shouldRotateRender) {
                ext.bdi$setRotation(new Vec3(0, 0, rotation));
            } else {
                ext.bdi$setRotation(new Vec3(0, rotation, 0));
            }
            rotator.bdi$setRotation(ext.bdi$getRotation());
        } else {
            ext.bdi$setRotation(rotator.bdi$getRotation());
        }
        // 灵魂沙检测
        ext.bdi$setSoulSand(entity.level().getBlockState(entity.blockPosition()).getBlock() == Blocks.SOUL_SAND);
        // 头颅检测
        ext.bdi$setSkull(item instanceof BlockItem && ((BlockItem) item).getBlock() instanceof SkullBlock);

        ext.bdi$setLastDebugAge(debugBucket);
    }

    /**
     * 提交渲染——替代原版的 submit 实现我们的自定义渲染
     */
    @Inject(method = "submit(Lnet/minecraft/client/renderer/entity/state/ItemEntityRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;Lnet/minecraft/client/renderer/state/level/CameraRenderState;)V",
            at = @At("HEAD"), cancellable = true)
    private void submit(ItemEntityRenderState state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera, CallbackInfo ci) {
        ItemEntityRenderStateExtender ext = (ItemEntityRenderStateExtender) state;

        boolean is3DModel = ext.bdi$is3DModel();
        boolean shouldRotateRender = ext.bdi$shouldRotateRender();
        Vec3 rotation = ext.bdi$getRotation();
        int renderCount = state.count; // state.count 已经是 getRenderedAmount 转换后的模型数量

        random.setSeed(state.seed);
        poseStack.pushPose();

        poseStack.translate(0, -0.0625, 0);

        // 立起旋转
        if (shouldRotateRender) {
            rotateAroundPivotX(poseStack, -(float) Math.PI / 2);
        }

        // 应用旋转角度
        if (shouldRotateRender) {
            rotateAroundPivotZ(poseStack, (float) rotation.z);
        } else {
            poseStack.mulPose(Axis.YP.rotation((float) rotation.y));
        }

        // 2D物品微调
        if (!is3DModel) {
            poseStack.translate(0, 0.0625, -0.109375);
        }

        // 灵魂沙特殊处理
        if (ext.bdi$isSoulSand()) {
            double soulSandItemHeight = 0.003;
            if (!is3DModel) {
                poseStack.translate(0, 0, 0.09375 /* 3/32 */ + soulSandItemHeight);
            }
            if (!shouldRotateRender) {
                poseStack.translate(0, 0.125 - (ext.bdi$getBlockHeight() / 4) + soulSandItemHeight, 0);
            }
        }
        // 头颅
        if (ext.bdi$isSkull()) {
            poseStack.translate(0, 0.1275, 0);
        }

        // 堆叠渲染
        if (BetterDroppedItems.CONFIG.itemPhysic2DRenderMode && !is3DModel) {
            float spacing = 0.09375F;
            if (renderCount > 1) {
                poseStack.translate(0, 0, 0.046875F);
            }
            poseStack.translate(0, 0, -spacing * (renderCount - 1) * 0.5F);
            for (int u = 0; u < renderCount; u++) {
                poseStack.pushPose();
                state.item.submit(poseStack, collector, state.lightCoords, OverlayTexture.NO_OVERLAY, state.outlineColor);
                poseStack.popPose();
                poseStack.translate(0.0F, 0.0F, spacing);
            }
        } else {
            for (int u = 0; u < renderCount; u++) {
                poseStack.pushPose();
                if (u > 0) {
                    if (is3DModel) {
                        float x = (random.nextFloat() * 2.0F - 1.0F) * 0.15F;
                        float y = (random.nextFloat() * 2.0F - 1.0F) * 0.15F;
                        float z = (random.nextFloat() * 2.0F - 1.0F) * 0.15F;
                        poseStack.translate(x, y, z);
                    } else {
                        float x = (random.nextFloat() * 2.0F - 1.0F) * 0.15F * 0.5F;
                        float y = (random.nextFloat() * 2.0F - 1.0F) * 0.15F * 0.5F;
                        poseStack.translate(x, y, 0.0F);
                        poseStack.mulPose(Axis.ZP.rotation(random.nextFloat()));
                    }
                }
                state.item.submit(poseStack, collector, state.lightCoords, OverlayTexture.NO_OVERLAY, state.outlineColor);
                poseStack.popPose();
                if (!is3DModel) {
                    poseStack.translate(0.0F, 0.0F, 0.0625F);
                }
            }
        }

        poseStack.popPose();
        ci.cancel();
    }

    @Unique
    private void rotateAroundPivotX(PoseStack poseStack, float angle) {
        poseStack.translate(0, 0.1875, 0);
        poseStack.mulPose(Axis.XP.rotation(angle));
        poseStack.translate(0, -0.1875, 0);
    }

    @Unique
    private void rotateAroundPivotZ(PoseStack poseStack, float angle) {
        poseStack.translate(0, 0.1875, 0);
        poseStack.mulPose(Axis.ZP.rotation(angle));
        poseStack.translate(0, -0.1875, 0);
    }
}
