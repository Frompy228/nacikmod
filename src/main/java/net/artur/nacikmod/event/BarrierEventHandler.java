package net.artur.nacikmod.event;

import net.artur.nacikmod.NacikMod;
import net.artur.nacikmod.item.BarrierSeal;
import net.artur.nacikmod.registry.ModBlocks;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityLeaveLevelEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.*;

@Mod.EventBusSubscriber(modid = NacikMod.MOD_ID)
public class BarrierEventHandler {
    private static final double BARRIER_RADIUS = 20.0;
    private static final double BARRIER_RADIUS_SQ = BARRIER_RADIUS * BARRIER_RADIUS;

    // Игроки и мобы с активными барьерами
    private static final Map<UUID, Set<String>> playerBarrierKeys = new HashMap<>();
    private static final Map<UUID, Set<String>> entityBarrierKeys = new HashMap<>();

    private static final Map<ServerLevel, Map<String, BarrierInfo>> cachedActiveBarriers = new HashMap<>();
    private static int cacheTickCounter = 0;

    private static class BarrierInfo {
        final BlockPos pos;
        final int slot;
        final UUID owner;

        BarrierInfo(BlockPos pos, int slot, UUID owner) {
            this.pos = pos;
            this.slot = slot;
            this.owner = owner;
        }

        String key() {
            return pos.getX() + "," + pos.getY() + "," + pos.getZ() + "," + slot;
        }
    }

    // ---------- БЛОК РАЗРУШЕН ----------
    @SubscribeEvent
    public static void onBlockBreak(BlockEvent.BreakEvent event) {
        if (event.getLevel().isClientSide()) return;
        if (!(event.getLevel() instanceof ServerLevel serverLevel)) return;

        BlockState state = event.getState();
        if (!state.is(ModBlocks.BARRIER.get())) return;

        BlockPos pos = event.getPos();

        for (ServerPlayer player : serverLevel.getServer().getPlayerList().getPlayers()) {
            for (ItemStack stack : player.getInventory().items) {
                if (stack.getItem() instanceof BarrierSeal) {
                    BarrierSeal.removeBarrier(stack, pos);
                }
            }
        }
        cachedActiveBarriers.remove(serverLevel);
    }

    // ---------- ОБНОВЛЕНИЕ ИГРОКОВ ----------
    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        if (!(event.player instanceof ServerPlayer player)) return;
        if (player.level().isClientSide) return;
        if (player.tickCount % 20 != 0) return; // раз в секунду

        UUID uuid = player.getUUID();
        ServerLevel level = (ServerLevel) player.level();
        Vec3 pos = player.position();

        Map<String, BarrierInfo> barriers = cachedActiveBarriers.getOrDefault(level, Map.of());
        Set<String> current = new HashSet<>();
        for (BarrierInfo info : barriers.values()) {
            if (isInRange(pos, info.pos)) current.add(info.key());
        }

        Set<String> previous = playerBarrierKeys.getOrDefault(uuid, Set.of());

        Set<String> entered = new HashSet<>(current);
        entered.removeAll(previous);

        Set<String> exited = new HashSet<>(previous);
        exited.removeAll(current);

        for (String key : entered) {
            BarrierInfo info = barriers.get(key);
            if (info != null) notifyBarrierOwner(player, info, true);
        }

        for (String key : exited) {
            BarrierInfo info = barriers.get(key);
            if (info != null) notifyBarrierOwner(player, info, false);
        }

        if (current.isEmpty()) playerBarrierKeys.remove(uuid);
        else playerBarrierKeys.put(uuid, current);
    }

    // ---------- ОБНОВЛЕНИЕ МОБОВ ----------
    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        cacheTickCounter++;

        if (cacheTickCounter >= 20) {
            cacheTickCounter = 0;
            updateBarrierCache(event.getServer());
        }

        if (event.getServer().getTickCount() % 20 != 0) return;

        for (ServerLevel level : event.getServer().getAllLevels()) {
            Map<String, BarrierInfo> barriers = cachedActiveBarriers.getOrDefault(level, Map.of());
            if (barriers.isEmpty()) continue;

            AABB area = combinedAABB(barriers.values());
            List<LivingEntity> entities = level.getEntitiesOfClass(LivingEntity.class, area, e -> !(e instanceof Player));

            for (LivingEntity entity : entities) {
                UUID id = entity.getUUID();
                Vec3 pos = entity.position();
                Set<String> previous = entityBarrierKeys.getOrDefault(id, Set.of());
                Set<String> current = new HashSet<>();

                for (BarrierInfo info : barriers.values()) {
                    if (isInRange(pos, info.pos)) current.add(info.key());
                }

                Set<String> entered = new HashSet<>(current);
                entered.removeAll(previous);
                Set<String> exited = new HashSet<>(previous);
                exited.removeAll(current);

                for (String key : entered) {
                    BarrierInfo info = barriers.get(key);
                    if (info != null) notifyBarrierOwnerAboutEntity(entity, info, true);
                }

                for (String key : exited) {
                    BarrierInfo info = barriers.get(key);
                    if (info != null) notifyBarrierOwnerAboutEntity(entity, info, false);
                }

                if (current.isEmpty()) entityBarrierKeys.remove(id);
                else entityBarrierKeys.put(id, current);
            }
        }
    }

    // ---------- КЭШ ОБНОВЛЕНИЕ ----------
    private static void updateBarrierCache(net.minecraft.server.MinecraftServer server) {
        cachedActiveBarriers.clear();

        for (ServerLevel level : server.getAllLevels()) {
            Map<String, BarrierInfo> levelMap = new HashMap<>();

            for (ServerPlayer player : server.getPlayerList().getPlayers()) {
                if (player.level() != level) continue;

                for (ItemStack stack : player.getInventory().items) {
                    if (!(stack.getItem() instanceof BarrierSeal)) continue;
                    if (!BarrierSeal.hasOwner(stack)) continue;

                    for (int slot = 0; slot < 3; slot++) {
                        if (!BarrierSeal.isBarrierActive(stack, slot)) continue;

                        BlockPos pos = BarrierSeal.getBarrierPosition(stack, slot);
                        if (pos == null) continue;

                        if (!level.isLoaded(pos)) continue;
                        if (!level.getBlockState(pos).is(ModBlocks.BARRIER.get())) continue;

                        UUID owner = BarrierSeal.getBarrierOwner(stack, slot);
                        if (owner == null) continue;

                        BarrierInfo info = new BarrierInfo(pos, slot, owner);
                        levelMap.put(info.key(), info);
                    }
                }
            }

            if (!levelMap.isEmpty()) cachedActiveBarriers.put(level, levelMap);
        }
    }

    // ---------- ВСПОМОГАТЕЛЬНЫЕ ----------
    private static boolean isInRange(Vec3 pos, BlockPos barrierPos) {
        double dx = pos.x - (barrierPos.getX() + 0.5);
        double dy = pos.y - (barrierPos.getY() + 0.5);
        double dz = pos.z - (barrierPos.getZ() + 0.5);
        return (dx * dx + dy * dy + dz * dz) <= BARRIER_RADIUS_SQ;
    }

    private static AABB combinedAABB(Collection<BarrierInfo> infos) {
        double minX = Double.MAX_VALUE, minY = Double.MAX_VALUE, minZ = Double.MAX_VALUE;
        double maxX = -Double.MAX_VALUE, maxY = -Double.MAX_VALUE, maxZ = -Double.MAX_VALUE;
        for (BarrierInfo info : infos) {
            BlockPos p = info.pos;
            double r = BARRIER_RADIUS;
            minX = Math.min(minX, p.getX() - r);
            minY = Math.min(minY, p.getY() - r);
            minZ = Math.min(minZ, p.getZ() - r);
            maxX = Math.max(maxX, p.getX() + r);
            maxY = Math.max(maxY, p.getY() + r);
            maxZ = Math.max(maxZ, p.getZ() + r);
        }
        return new AABB(minX, minY, minZ, maxX, maxY, maxZ);
    }

    // ---------- УВЕДОМЛЕНИЯ ----------
    private static void notifyBarrierOwner(Player target, BarrierInfo info, boolean entered) {
        if (!(target.level() instanceof ServerLevel level)) return;
        ServerPlayer owner = level.getServer().getPlayerList().getPlayer(info.owner);
        if (owner == null) return;

        String msg = String.format("%s %s barrier in slot %d at (%d, %d, %d)",
                target.getGameProfile().getName(),
                entered ? "entered" : "exited",
                info.slot + 1,
                (int) target.getX(),
                (int) target.getY(),
                (int) target.getZ());
        owner.sendSystemMessage(Component.literal(msg)
                .withStyle(entered ? ChatFormatting.YELLOW : ChatFormatting.GRAY));
    }

    private static void notifyBarrierOwnerAboutEntity(LivingEntity entity, BarrierInfo info, boolean entered) {
        if (!(entity.level() instanceof ServerLevel level)) return;
        ServerPlayer owner = level.getServer().getPlayerList().getPlayer(info.owner);
        if (owner == null) return;

        String msg = String.format("%s %s barrier in slot %d at (%d, %d, %d)",
                entity.getName().getString(),
                entered ? "entered" : "exited",
                info.slot + 1,
                (int) entity.getX(),
                (int) entity.getY(),
                (int) entity.getZ());
        owner.sendSystemMessage(Component.literal(msg)
                .withStyle(entered ? ChatFormatting.YELLOW : ChatFormatting.GRAY));
    }

    @SubscribeEvent
    public static void onPlayerLogout(net.minecraftforge.event.entity.player.PlayerEvent.PlayerLoggedOutEvent event) {
        playerBarrierKeys.remove(event.getEntity().getUUID());
        if (event.getEntity().level() instanceof ServerLevel s)
            cachedActiveBarriers.remove(s);
    }

    @SubscribeEvent
    public static void onEntityRemove(EntityLeaveLevelEvent event) {
        if (event.getEntity() instanceof LivingEntity && !(event.getEntity() instanceof Player)) {
            entityBarrierKeys.remove(event.getEntity().getUUID());
        }
    }
}
