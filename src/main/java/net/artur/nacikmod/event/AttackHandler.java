package net.artur.nacikmod.event;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.damagesource.DamageSource;
import net.artur.nacikmod.registry.ModEffects;

public class AttackHandler {
    public static void onAttack(LivingEntity target, Player player) {
        if (player.hasEffect(ModEffects.MANA_LAST_MAGIC.get())) {
            AABB area = new AABB(target.getX() - 1, target.getY() - 1, target.getZ() - 1,
                               target.getX() + 1, target.getY() + 1, target.getZ() + 1);
            
            player.level().getEntities(player, area).forEach(entity -> {
                if (entity instanceof LivingEntity living && entity != target) {
                }
            });
        }
    }
} 