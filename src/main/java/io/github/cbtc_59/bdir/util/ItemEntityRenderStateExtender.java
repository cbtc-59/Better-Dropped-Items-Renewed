package io.github.cbtc_59.bdir.util;

import net.minecraft.world.phys.Vec3;

public interface ItemEntityRenderStateExtender {
    Vec3 bdi$getRotation();
    void bdi$setRotation(Vec3 rotation);

    int bdi$getLastDebugAge();
    void bdi$setLastDebugAge(int age);

    boolean bdi$is3DModel();
    void bdi$set3DModel(boolean is3D);

    float bdi$getBlockHeight();
    void bdi$setBlockHeight(float height);

    boolean bdi$shouldRotateRender();
    void bdi$setShouldRotateRender(boolean should);

    boolean bdi$isSoulSand();
    void bdi$setSoulSand(boolean is);

    boolean bdi$isSkull();
    void bdi$setSkull(boolean is);
}
