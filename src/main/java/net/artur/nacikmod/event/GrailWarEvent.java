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

    private static final Map<ServerLevel, WarState> worldsWarState = new HashMap<>();
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

    private static WarState getState(ServerLevel level) {
        return worldsWarState.computeIfAbsent(level, l -> new WarState());
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || !(event.player instanceof ServerPlayer)) return;
        ServerPlayer player = (ServerPlayer) event.player;
        if (player.level().isClientSide()) return;

        ServerLevel level = (ServerLevel) player.level();
        WarState state = getState(level);

        if (state.warCompleted) return;

        state.tickCounter++;
        if (state.tickCounter < CHECK_INTERVAL) return;
        state.tickCounter = 0;

        if (!state.warActive) {
            checkForWarStart(level, state);
        } else if (state.warStartDelay > 0) {
            state.warStartDelay -= CHECK_INTERVAL;
            if (state.warStartDelay <= 0) {
                broadcastMessage(level, "§6§lThe Grail War has officially begun!");
            }
        } else {
            checkWarStatus(level, state);
        }
    }

    @SubscribeEvent
    public static void onEntityDeath(LivingDeathEvent event) {
        if (event.getEntity() instanceof GraalEntity graal) {
            Level level = graal.level();
            if (!(level instanceof ServerLevel serverLevel)) return;
            WarState state = getState(serverLevel);

            if (!state.warActive || state.warCompleted) return;

            // Грааль убит — война заканчивается без победителя
            broadcastMessage(serverLevel, "§cThe Holy Grail has been destroyed! The war ends without a winner!");
            endWar(serverLevel, state);
        }
    }


    @SubscribeEvent
    public static void onPlayerDeath(LivingDeathEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer)) return;

        ServerPlayer player = (ServerPlayer) event.getEntity();
        ServerLevel level = (ServerLevel) player.level();
        WarState state = getState(level);

        if (state.warCompleted) return;
        if (!state.warParticipants.contains(player.getUUID())) return;

        state.warParticipants.remove(player.getUUID());
        broadcastMessage(level, "§c" + player.getName().getString() + " has fallen from the Grail War!");

        if (state.warParticipants.size() == 2 && !state.graalSpawned) {
            spawnGraal(level, state);
        } else if (state.warParticipants.size() == 1) {
            checkWarEndCondition(level, state);
        }
    }

    private static void checkForWarStart(ServerLevel level, WarState state) {
        List<UUID> trueMages = new ArrayList<>();
        for (ServerPlayer player : level.getServer().getPlayerList().getPlayers()) {
            player.getCapability(ManaProvider.MANA_CAPABILITY).ifPresent(mana -> {
                if (mana.isTrueMage()) trueMages.add(player.getUUID());
            });
        }

        if (trueMages.size() >= 3) {
            state.warActive = true;
            state.warParticipants = new ArrayList<>(trueMages);
            state.warStartDelay = WAR_DELAY_TICKS;
            broadcastMessage(level, "§6§lAt least 3 True Mages detected! War will start in 5 minutes!");
        }
    }

    private static void checkWarStatus(ServerLevel level, WarState state) {
        state.warParticipants.removeIf(uuid -> {
            ServerPlayer p = level.getServer().getPlayerList().getPlayer(uuid);
            return p == null || p.isDeadOrDying();
        });

        if (state.warParticipants.size() == 2 && !state.graalSpawned) {
            spawnGraal(level, state);
        } else if (state.warParticipants.size() == 1) {
            checkWarEndCondition(level, state);
        } else if (state.warParticipants.isEmpty()) {
            endWar(level, state);
        }
    }

    private static void spawnGraal(ServerLevel level, WarState state) {
        state.graalSpawned = true;

        // Опорный блок — центр мира
        BlockPos basePos = new BlockPos(0, 0, 0);
        BlockPos surfacePos = level.getHeightmapPos(Heightmap.Types.WORLD_SURFACE, basePos);

        int spawnY;
        if (surfacePos == null || surfacePos.getY() < 0) {
            spawnY = 100; // если поверхность ниже 0 или отсутствует
        } else {
            spawnY = surfacePos.getY() + 75; // иначе выше поверхности на 75 блоков
        }

        BlockPos spawnPos = new BlockPos(basePos.getX(), spawnY, basePos.getZ());

        GraalEntity graal = ModEntities.GRAIL.get().create(level);
        if (graal != null) {
            graal.setPos(spawnPos.getX() + 0.5, spawnPos.getY(), spawnPos.getZ() + 0.5);
            graal.finalizeSpawn(level, level.getCurrentDifficultyAt(spawnPos), MobSpawnType.MOB_SUMMONED, null, null);
            if (level.addFreshEntity(graal)) {
                state.graalEntity = graal;
                broadcastMessage(level, "§5§lThe Holy Grail has appeared at (0," + spawnPos.getY() + ",0)!");
            } else {
                broadcastMessage(level, "§cFailed to spawn Grail entity!");
            }
        }
    }


    private static void checkWarEndCondition(ServerLevel level, WarState state) {
        if (state.warParticipants.size() != 1) return;

        ServerPlayer winner = level.getServer().getPlayerList().getPlayer(state.warParticipants.get(0));
        if (winner != null && state.graalEntity != null && !state.graalEntity.isDeadOrDying()) {
            giveWinnerReward(winner);
            state.graalEntity.remove(net.minecraft.world.entity.Entity.RemovalReason.DISCARDED);
        }

        endWar(level, state);
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

    private static void endWar(ServerLevel level, WarState state) {
        state.warActive = false;
        state.graalSpawned = false;
        state.warCompleted = true;
        state.warParticipants.clear();
        state.graalEntity = null;
        broadcastMessage(level, "§c§lThe Holy Grail War has ended!");
    }

    private static void broadcastMessage(Level level, String msg) {
        if (!(level instanceof ServerLevel serverLevel)) return;
        for (ServerPlayer p : serverLevel.getServer().getPlayerList().getPlayers()) {
            p.sendSystemMessage(Component.literal(msg));
        }
    }
}
