package net.artur.nacikmod.event;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.artur.nacikmod.NacikMod;
import net.artur.nacikmod.capability.lord_of_souls_summoned_entities.LordOfSoulsSummonedEntityProvider;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.UUID;

@Mod.EventBusSubscriber(modid = NacikMod.MOD_ID)
public class LordOfSoulsSummonedEntityLifetimeHandler {
    
    private static int tickCounter = 0;
    private static final int CHECK_INTERVAL = 20; // Check every second (20 ticks)

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;

        tickCounter++;
        if (tickCounter < CHECK_INTERVAL) return;
        tickCounter = 0;

        MinecraftServer server = event.getServer();

        // Для всех игроков проверяем их отслеживаемые сущности
        for (Player player : server.getPlayerList().getPlayers()) {
            player.getCapability(LordOfSoulsSummonedEntityProvider.LORD_OF_SOULS_SUMMONED_ENTITY_CAPABILITY)
                    .ifPresent(cap -> {
                        Set<UUID> toRemove = new HashSet<>();
                        
                        for (UUID uuid : cap.getTrackedEntities()) {
                            Entity entity = null;

                            // Ищем сущность по UUID во всех уровнях
                            for (ServerLevel level : server.getAllLevels()) {
                                entity = level.getEntity(uuid);
                                if (entity != null) break;
                            }

                            if (entity instanceof LivingEntity living) {
                                var data = living.getPersistentData();
                                if (data.contains("lord_of_souls_lifetime")) {
                                    try {
                                        int lifetime = data.getInt("lord_of_souls_lifetime");
                                        lifetime -= CHECK_INTERVAL;

                                        if (lifetime <= 0) {
                                            living.remove(Entity.RemovalReason.DISCARDED);
                                            toRemove.add(uuid);
                                        } else {
                                            data.putInt("lord_of_souls_lifetime", lifetime);
                                        }
                                    } catch (Exception e) {
                                        data.remove("lord_of_souls_lifetime");
                                        toRemove.add(uuid);
                                    }
                                } else {
                                    toRemove.add(uuid);
                                }
                            } else {
                                // Сущность не найдена или уже удалена
                                toRemove.add(uuid);
                            }
                        }

                        // Удаляем мёртвые ссылки из capability
                        toRemove.forEach(cap::removeSummonedEntity);
                    });
        }
    }
} 