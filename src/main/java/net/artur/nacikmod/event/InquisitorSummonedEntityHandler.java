package net.artur.nacikmod.event;

import net.artur.nacikmod.NacikMod;
import net.artur.nacikmod.entity.ai.InquisitorGuardGoal;
import net.artur.nacikmod.entity.custom.InquisitorEntity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.WaterAnimal;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.UUID;

@Mod.EventBusSubscriber(modid = NacikMod.MOD_ID)
public class InquisitorSummonedEntityHandler {

    @SubscribeEvent
    public static void onLivingHurt(LivingHurtEvent event) {
        LivingEntity attacker = event.getSource().getEntity() instanceof LivingEntity ? 
                (LivingEntity) event.getSource().getEntity() : null;
        if (attacker == null) {
            return;
        }

        LivingEntity target = event.getEntity();
        
        // Проверяем, являются ли обе сущности призванными одним инквизитором
        UUID attackerOwner = getInquisitorOwner(attacker);
        UUID targetOwner = getInquisitorOwner(target);
        
        // Если обе сущности принадлежат одному инквизитору - отменяем урон
        if (attackerOwner != null && targetOwner != null && attackerOwner.equals(targetOwner)) {
            event.setCanceled(true);
            // Также очищаем цель, если это моб
            if (attacker instanceof Mob mob) {
                mob.setTarget(null);
            }
            return;
        }
        
        // Если атакующий - призванный инквизитором, а цель - сам инквизитор - отменяем урон
        if (attackerOwner != null && target instanceof InquisitorEntity inquisitor) {
            if (attackerOwner.equals(inquisitor.getUUID())) {
                event.setCanceled(true);
                if (attacker instanceof Mob mob) {
                    mob.setTarget(null);
                }
                return;
            }
        }
        
        // Если цель - призванный инквизитором, а атакующий - сам инквизитор - отменяем урон
        if (targetOwner != null && attacker instanceof InquisitorEntity inquisitor) {
            if (targetOwner.equals(inquisitor.getUUID())) {
                event.setCanceled(true);
                return;
            }
        }
        
        // Предотвращаем урон животным и рыбам от призванных сущностей
        if (attackerOwner != null) {
            if (target instanceof Animal || target instanceof WaterAnimal) {
                event.setCanceled(true);
                if (attacker instanceof Mob mob) {
                    mob.setTarget(null);
                }
                return;
            }
        }
    }

    @SubscribeEvent
    public static void onEntityJoinLevel(EntityJoinLevelEvent event) {
        if (!(event.getEntity() instanceof Mob mob)) {
            return;
        }

        // Проверяем, является ли это призванной сущностью инквизитора
        UUID ownerUUID = getInquisitorOwner(mob);
        if (ownerUUID == null) {
            return;
        }

        // Откладываем восстановление AI на следующий тик, чтобы все сущности успели загрузиться
        if (event.getLevel() instanceof ServerLevel serverLevel) {
            serverLevel.getServer().tell(new net.minecraft.server.TickTask(1, () -> {
                if (!mob.isAlive()) {
                    return;
                }

                // Проверяем, есть ли уже кастомный AI
                boolean hasCustomAI = false;
                for (var goal : mob.targetSelector.getAvailableGoals()) {
                    if (goal.getGoal() instanceof InquisitorGuardGoal) {
                        hasCustomAI = true;
                        break;
                    }
                }

                // Если кастомного AI нет, восстанавливаем его
                if (!hasCustomAI) {
                    // Находим инквизитора-владельца
                    InquisitorEntity owner = findInquisitorByUUID(serverLevel, ownerUUID);
                    if (owner != null && owner.isAlive()) {
                        // Очищаем все цели атаки и добавляем нашу
                        mob.targetSelector.removeAllGoals(goal -> true);
                        mob.targetSelector.addGoal(0, new InquisitorGuardGoal(mob, owner));
                        
                        // Очищаем цель, если это инквизитор-владелец
                        if (mob.getTarget() == owner) {
                            mob.setTarget(null);
                        }
                        
                        // Устанавливаем цель инквизитора как цель призванной сущности
                        if (owner.getTarget() != null && owner.getTarget().isAlive()) {
                            mob.setTarget(owner.getTarget());
                        }
                        
                        // Устанавливаем свойства призванной сущности
                        mob.setPersistenceRequired();
                    }
                } else {
                    // Если AI уже есть, просто очищаем цель, если это владелец
                    InquisitorEntity owner = findInquisitorByUUID(serverLevel, ownerUUID);
                    if (owner != null && mob.getTarget() == owner) {
                        mob.setTarget(null);
                        if (owner.getTarget() != null && owner.getTarget().isAlive()) {
                            mob.setTarget(owner.getTarget());
                        }
                    }
                }
            }));
        }
    }

    private static InquisitorEntity findInquisitorByUUID(ServerLevel level, UUID ownerUUID) {
        // Ищем инквизитора по UUID во всех уровнях сервера
        for (var serverLevel : level.getServer().getAllLevels()) {
            for (Entity entity : serverLevel.getAllEntities()) {
                if (entity instanceof InquisitorEntity inquisitor && 
                    inquisitor.getUUID().equals(ownerUUID)) {
                    return inquisitor;
                }
            }
        }
        return null;
    }

    private static UUID getInquisitorOwner(LivingEntity entity) {
        if (entity.getPersistentData().contains("inquisitor_owner")) {
            try {
                return entity.getPersistentData().getUUID("inquisitor_owner");
            } catch (Exception ignored) {
            }
        }
        return null;
    }
}


