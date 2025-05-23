package net.artur.nacikmod.item.ability;

import net.artur.nacikmod.capability.mana.IMana;
import net.artur.nacikmod.capability.mana.ManaProvider;
import net.artur.nacikmod.registry.ModEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

import java.util.ArrayList;
import java.util.List;

public class TimeStop {

    private static List<LivingEntity> affectedEntities = new ArrayList<>();
    private static int currentEntityIndex = 0;

    public static void activate(Player player) {
        Level world = player.level();
        affectedEntities.clear();

        AABB searchArea = new AABB(player.getX() - 25, player.getY() - 25, player.getZ() - 25,
                player.getX() + 25, player.getY() + 25, player.getZ() + 25);

        List<Entity> nearbyEntities = world.getEntities(player, searchArea);

        for (Entity entity : nearbyEntities) {
            if (entity instanceof LivingEntity livingEntity && entity != player && entity.isAlive()) {
                boolean intersectsHorizontally = Math.abs(player.getX() - entity.getX()) < 25 && Math.abs(player.getZ() - entity.getZ()) < 25;
                if (intersectsHorizontally) {
                    affectedEntities.add(livingEntity);
                }
            }
        }

        applyEffectToNextEntity(world, player);
    }

    private static void applyEffectToNextEntity(Level world, Player player) {
        if (currentEntityIndex < affectedEntities.size()) {
            LivingEntity entity = affectedEntities.get(currentEntityIndex);

            if (!world.isClientSide) {
                if (!entity.hasEffect(ModEffects.TIME_SLOW.get()) && entity.isAlive()) {
                    // Получаем ману владельца
                    int ownerMana = player.getCapability(ManaProvider.MANA_CAPABILITY)
                            .map(IMana::getMaxMana)
                            .orElse(100);

                    // Рассчитываем базовый amplifier от маны владельца
                    int amplifier;
                    if (ownerMana >= 100000) {
                        amplifier = 3;
                    } else if (ownerMana >= 10000) {
                        amplifier = 2;
                    } else if (ownerMana >= 1000) {
                        amplifier = 1;
                    } else {
                        amplifier = 0;
                    }

                    // Получаем ману цели
                    int targetMana = entity.getCapability(ManaProvider.MANA_CAPABILITY)
                            .map(IMana::getMaxMana)
                            .orElse(0);

                    // Корректируем amplifier если у цели больше маны
                    if (targetMana > ownerMana) {
                        amplifier = Math.max(amplifier - 1, 0); // Гарантируем неотрицательное значение
                    }

                    // Применяем эффект только если amplifier >= 0
                    if (amplifier >= 0) {
                        MobEffectInstance effectInstance = new MobEffectInstance(ModEffects.TIME_SLOW.get(), 100, amplifier);
                        entity.addEffect(effectInstance);
                    }
                }
            }

            currentEntityIndex++;
            if (currentEntityIndex < affectedEntities.size()) {
                applyEffectToNextEntity(world, player);
            } else {
                currentEntityIndex = 0;
            }
        }
    }
}
