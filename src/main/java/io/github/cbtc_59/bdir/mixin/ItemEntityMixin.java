package io.github.cbtc_59.bdir.mixin;

import io.github.cbtc_59.bdir.util.ItemEntityRotator;
import net.minecraft.entity.ItemEntity;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(ItemEntity.class)
public class ItemEntityMixin implements ItemEntityRotator {
    @Unique
    private Vec3d rotation = new Vec3d(0, 0, 0);
    @Unique
    private int bdiLastDebugAge = -1;

    @Override
    public Vec3d getRotation() {
        return rotation;
    }

    @Override
    public void setRotation(Vec3d rotation) {
        this.rotation = rotation;
    }

    @Override
    public int bdi$getLastDebugAge() {
        return bdiLastDebugAge;
    }

    @Override
    public void bdi$setLastDebugAge(int age) {
        this.bdiLastDebugAge = age;
    }
}