package net.artur.nacikmod.event;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.Entity;

public class ModDamageTypes {
    public static final ResourceKey<DamageType> MAGIC_FIREBALL =
            ResourceKey.create(net.minecraft.core.registries.Registries.DAMAGE_TYPE, new ResourceLocation("nacikmod", "magic_fireball"));

    public static DamageSource magicFireball(ServerLevel level, Entity projectile, Entity attacker) {
        Holder<DamageType> holder = level.registryAccess()
                .registryOrThrow(net.minecraft.core.registries.Registries.DAMAGE_TYPE)
                .getHolderOrThrow(MAGIC_FIREBALL);

        return new DamageSource(holder, projectile, attacker);
    }
}
