package net.artur.nacikmod.event;

import net.artur.nacikmod.NacikMod;
import net.artur.nacikmod.capability.mana.ManaProvider;
import net.artur.nacikmod.entity.custom.GraalEntity;
import net.artur.nacikmod.registry.ModEntities;
import net.artur.nacikmod.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.*;

@Mod.EventBusSubscriber(modid = NacikMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class GrailWarEvent {

    // Глобальное состояние войны для всего сервера (только одна война может быть активна)
    private static final WarState globalWarState = new WarState();
    private static final int CHECK_INTERVAL = 20; // проверка каждую секунду
    private static final int WAR_DELAY_TICKS = 6000; // 5 минут = 6000 тиков

    private static class WarState {
        boolean warActive = false;
        boolean warCompleted = false;
        boolean graalSpawned = false;
        GraalEntity graalEntity = null;
        List<UUID> warParticipants = new ArrayList<>();
        int tickCounter = 0;
        int warStartDelay = 0;
    }

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;

        // Используем глобальное состояние войны
        if (globalWarState.warCompleted) return;

        // Получаем верхний мир
        ServerLevel overworld = event.getServer().overworld();
        if (overworld == null) return;

        // Проверяем только один раз в секунду
        globalWarState.tickCounter++;
        if (globalWarState.tickCounter < CHECK_INTERVAL) return;
        globalWarState.tickCounter = 0;

        if (!globalWarState.warActive) {
            checkForWarStart(overworld);
        } else if (globalWarState.warStartDelay > 0) {
            globalWarState.warStartDelay -= CHECK_INTERVAL;
            if (globalWarState.warStartDelay <= 0) {
                broadcastMessage(overworld, "§6§lThe Grail War has officially begun!");
            }
        } else {
            checkWarStatus(overworld);
        }
    }

    @SubscribeEvent
    public static void onEntityDeath(LivingDeathEvent event) {
        if (event.getEntity() instanceof GraalEntity graal) {
            Level level = graal.level();
            if (!(level instanceof ServerLevel serverLevel)) return;

            // Используем глобальное состояние войны
            if (!globalWarState.warActive || globalWarState.warCompleted) return;

            // Грааль убит — война заканчивается без победителя
            broadcastMessage(serverLevel, "§cThe Holy Grail has been destroyed! The war ends without a winner!");
            endWar(serverLevel);
        }
    }


    @SubscribeEvent
    public static void onPlayerDeath(LivingDeathEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer)) return;

        ServerPlayer player = (ServerPlayer) event.getEntity();
        ServerLevel level = (ServerLevel) player.level();

        // Используем глобальное состояние войны
        if (globalWarState.warCompleted) return;
        if (!globalWarState.warParticipants.contains(player.getUUID())) return;

        globalWarState.warParticipants.remove(player.getUUID());
        broadcastMessage(level, "§c" + player.getName().getString() + " has fallen from the Grail War!");

        // Получаем верхний мир для спавна Graal Entity
        ServerLevel overworld = level.getServer().overworld();
        if (overworld == null) return;

        if (globalWarState.warParticipants.size() == 2 && !globalWarState.graalSpawned) {
            spawnGraal(overworld);
        } else if (globalWarState.warParticipants.size() == 1) {
            checkWarEndCondition(overworld);
        }
    }

    private static void checkForWarStart(ServerLevel level) {
        // Проверяем, не началась ли уже война
        if (globalWarState.warActive || globalWarState.warCompleted) return;

        // Получаем верхний мир для проверки игроков
        ServerLevel overworld = level.getServer().overworld();
        if (overworld == null) return;

        // Проверяем только игроков, которые находятся в верхнем мире
        List<UUID> trueMages = new ArrayList<>();
        for (ServerPlayer player : level.getServer().getPlayerList().getPlayers()) {
            // Проверяем только игроков в верхнем мире
            if (!player.level().dimension().equals(Level.OVERWORLD)) continue;
            
            player.getCapability(ManaProvider.MANA_CAPABILITY).ifPresent(mana -> {
                if (mana.isTrueMage()) trueMages.add(player.getUUID());
            });
        }

        if (trueMages.size() >= 3) {
            globalWarState.warActive = true;
            globalWarState.warParticipants = new ArrayList<>(trueMages);
            globalWarState.warStartDelay = WAR_DELAY_TICKS;
            broadcastMessage(overworld, "§6§lAt least 3 True Mages detected in Overworld! War will start in 5 minutes!");
        }
    }

    private static void checkWarStatus(ServerLevel level) {
        // Получаем верхний мир
        ServerLevel overworld = level.getServer().overworld();
        if (overworld == null) return;

        globalWarState.warParticipants.removeIf(uuid -> {
            ServerPlayer p = overworld.getServer().getPlayerList().getPlayer(uuid);
            return p == null || p.isDeadOrDying();
        });

        if (globalWarState.warParticipants.size() == 2 && !globalWarState.graalSpawned) {
            spawnGraal(overworld);
        } else if (globalWarState.warParticipants.size() == 1) {
            checkWarEndCondition(overworld);
        } else if (globalWarState.warParticipants.isEmpty()) {
            endWar(overworld);
        }
    }

    private static void spawnGraal(ServerLevel overworld) {
        // Война может происходить только в верхнем мире
        if (!overworld.dimension().equals(Level.OVERWORLD)) return;

        globalWarState.graalSpawned = true;

        // Опорный блок — центр мира
        BlockPos basePos = new BlockPos(0, 0, 0);
        BlockPos surfacePos = overworld.getHeightmapPos(Heightmap.Types.WORLD_SURFACE, basePos);

        int spawnY;
        if (surfacePos == null || surfacePos.getY() < 0) {
            spawnY = 100; // если поверхность ниже 0 или отсутствует
        } else {
            spawnY = surfacePos.getY() + 75; // иначе выше поверхности на 75 блоков
        }

        BlockPos spawnPos = new BlockPos(basePos.getX(), spawnY, basePos.getZ());

        GraalEntity graal = ModEntities.GRAIL.get().create(overworld);
        if (graal != null) {
            graal.setPos(spawnPos.getX() + 0.5, spawnPos.getY(), spawnPos.getZ() + 0.5);
            graal.finalizeSpawn(overworld, overworld.getCurrentDifficultyAt(spawnPos), MobSpawnType.MOB_SUMMONED, null, null);
            if (overworld.addFreshEntity(graal)) {
                globalWarState.graalEntity = graal;
                broadcastMessage(overworld, "§5§lThe Holy Grail has appeared at (0," + spawnPos.getY() + ",0)!");
            } else {
                broadcastMessage(overworld, "§cFailed to spawn Grail entity!");
            }
        }
    }


    private static void checkWarEndCondition(ServerLevel overworld) {
        if (globalWarState.warParticipants.size() != 1) return;

        ServerPlayer winner = overworld.getServer().getPlayerList().getPlayer(globalWarState.warParticipants.get(0));
        if (winner != null && globalWarState.graalEntity != null && !globalWarState.graalEntity.isDeadOrDying()) {
            giveWinnerReward(winner);
            globalWarState.graalEntity.remove(net.minecraft.world.entity.Entity.RemovalReason.DISCARDED);
        }

        endWar(overworld);
    }

    private static void giveWinnerReward(ServerPlayer winner) {
        ItemStack sword = new ItemStack(ModItems.GRAIL.get());
        if (!winner.getInventory().add(sword)) winner.drop(sword, false);
        winner.sendSystemMessage(Component.literal("§6§lYou are the winner of the Grail War!"));

        var server = winner.getServer();
        if (server != null) {
            var advancement = server.getAdvancements().getAdvancement(new ResourceLocation("nacikmod", "grail_war"));
            if (advancement != null) {
                var progress = winner.getAdvancements().getOrStartProgress(advancement);
                if (!progress.isDone()) {
                    for (String criteria : progress.getRemainingCriteria()) {
                        winner.getAdvancements().award(advancement, criteria);
                    }
                }
            }
        }
    }

    private static void endWar(ServerLevel overworld) {
        globalWarState.warActive = false;
        globalWarState.graalSpawned = false;
        globalWarState.warCompleted = true;
        globalWarState.warParticipants.clear();
        globalWarState.graalEntity = null;
        broadcastMessage(overworld, "§c§lThe Holy Grail War has ended!");
    }

    private static void broadcastMessage(Level level, String msg) {
        if (!(level instanceof ServerLevel serverLevel)) return;
        for (ServerPlayer p : serverLevel.getServer().getPlayerList().getPlayers()) {
            p.sendSystemMessage(Component.literal(msg));
        }
    }
}
