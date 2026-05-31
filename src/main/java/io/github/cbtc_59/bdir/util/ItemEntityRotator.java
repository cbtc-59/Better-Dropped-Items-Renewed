package io.github.cbtc_59.bdir.util;

import net.minecraft.world.phys.Vec3;

public interface ItemEntityRotator {
    Vec3 bdi$getRotation();
    void bdi$setRotation(Vec3 rotation);

    /** 用于调试去重：上次输出调试信息的 age 桶（age / 20） */
    int bdi$getLastDebugAge();
    void bdi$setLastDebugAge(int age);
}
