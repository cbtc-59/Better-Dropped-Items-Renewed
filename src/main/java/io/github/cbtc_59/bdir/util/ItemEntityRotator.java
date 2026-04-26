package io.github.cbtc_59.bdir.util;

import net.minecraft.util.math.Vec3d;

public interface ItemEntityRotator {
    Vec3d getRotation();
    void setRotation(Vec3d rotation);
}