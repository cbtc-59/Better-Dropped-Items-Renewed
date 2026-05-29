package io.github.cbtc_59.bdir.util;

import net.minecraft.util.math.Vec3d;

public interface ItemEntityRotator {
    Vec3d bdi$getRotation();
    void bdi$setRotation(Vec3d rotation);

    /** 用于调试去重：上次输出调试信息的 age 桶（age / 20） */
    int bdi$getLastDebugAge();
    void bdi$setLastDebugAge(int age);
}