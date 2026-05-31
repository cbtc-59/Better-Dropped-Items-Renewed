package io.github.cbtc_59.bdir.mixin;

import io.github.cbtc_59.bdir.util.ItemEntityRotator;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(ItemEntity.class)
public class ItemEntityMixin implements ItemEntityRotator {
    @Unique
    private Vec3 rotation = Vec3.ZERO;
    @Unique
    private int bdiLastDebugAge = -1;

    @Override
    public Vec3 bdi$getRotation() {
        return rotation;
    }

    @Override
    public void bdi$setRotation(Vec3 rotation) {
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
