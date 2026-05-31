package io.github.cbtc_59.bdir.mixin;

import io.github.cbtc_59.bdir.util.ItemEntityRenderStateExtender;
import net.minecraft.client.renderer.entity.state.ItemEntityRenderState;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(ItemEntityRenderState.class)
public class ItemEntityRenderStateMixin implements ItemEntityRenderStateExtender {

    @Unique
    private Vec3 bdiRotation = Vec3.ZERO;
    @Unique
    private int bdiLastDebugAge = -1;
    @Unique
    private boolean bdiIs3DModel;
    @Unique
    private float bdiBlockHeight;
    @Unique
    private boolean bdiShouldRotateRender = true;
    @Unique
    private boolean bdiSoulSand;
    @Unique
    private boolean bdiSkull;

    @Override public Vec3 bdi$getRotation() { return bdiRotation; }
    @Override public void bdi$setRotation(Vec3 v) { this.bdiRotation = v; }
    @Override public int bdi$getLastDebugAge() { return bdiLastDebugAge; }
    @Override public void bdi$setLastDebugAge(int a) { this.bdiLastDebugAge = a; }
    @Override public boolean bdi$is3DModel() { return bdiIs3DModel; }
    @Override public void bdi$set3DModel(boolean v) { this.bdiIs3DModel = v; }
    @Override public float bdi$getBlockHeight() { return bdiBlockHeight; }
    @Override public void bdi$setBlockHeight(float v) { this.bdiBlockHeight = v; }
    @Override public boolean bdi$shouldRotateRender() { return bdiShouldRotateRender; }
    @Override public void bdi$setShouldRotateRender(boolean v) { this.bdiShouldRotateRender = v; }
    @Override public boolean bdi$isSoulSand() { return bdiSoulSand; }
    @Override public void bdi$setSoulSand(boolean v) { this.bdiSoulSand = v; }
    @Override public boolean bdi$isSkull() { return bdiSkull; }
    @Override public void bdi$setSkull(boolean v) { this.bdiSkull = v; }
}
