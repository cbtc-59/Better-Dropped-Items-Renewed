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

    @Override
    public Vec3 bdi$getRotation() {
        return bdiRotation;
    }

    @Override
    public void bdi$setRotation(Vec3 rotation) {
        this.bdiRotation = rotation;
    }

    @Override
    public int bdi$getLastDebugAge() {
        return bdiLastDebugAge;
    }

    @Override
    public void bdi$setLastDebugAge(int age) {
        this.bdiLastDebugAge = age;
    }

    @Override
    public boolean bdi$is3DModel() {
        return bdiIs3DModel;
    }

    @Override
    public void bdi$set3DModel(boolean is3D) {
        this.bdiIs3DModel = is3D;
    }

    @Override
    public float bdi$getBlockHeight() {
        return bdiBlockHeight;
    }

    @Override
    public void bdi$setBlockHeight(float height) {
        this.bdiBlockHeight = height;
    }

    @Override
    public boolean bdi$shouldRotateRender() {
        return bdiShouldRotateRender;
    }

    @Override
    public void bdi$setShouldRotateRender(boolean should) {
        this.bdiShouldRotateRender = should;
    }
}
