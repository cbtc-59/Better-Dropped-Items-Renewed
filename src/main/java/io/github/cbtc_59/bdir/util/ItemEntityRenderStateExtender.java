package io.github.cbtc_59.bdir.util;

import net.minecraft.world.phys.Vec3;

/** 扩展 ItemEntityRenderState 的接口，用于存储自定义渲染数据 */
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
}
