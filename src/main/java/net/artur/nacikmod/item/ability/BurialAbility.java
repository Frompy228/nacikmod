package net.artur.nacikmod.item.ability;

import net.artur.nacikmod.NacikMod;
// import net.artur.nacikmod.register.ModParticles;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.server.ServerStoppingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import javax.annotation.Nullable;
import java.util.*;

@Mod.EventBusSubscriber(modid = NacikMod.MOD_ID)
public class BurialAbility {

    private static final int RANGE = 32;
    private static final int DEPTH = 4;
    private static final int RESTORE_DELAY = 50;

    private static final Map<UUID, BurialData> activeBurials = new HashMap<>();

    private static final Set<Block> FORBIDDEN_BLOCKS = Set.of(
            Blocks.OBSIDIAN, Blocks.CRYING_OBSIDIAN, Blocks.NETHER_PORTAL,
            Blocks.END_PORTAL, Blocks.END_PORTAL_FRAME, Blocks.BEDROCK,
            Blocks.BARRIER, Blocks.COMMAND_BLOCK
    );

    public static class BlockInfo {
        public final BlockPos originalPos;
        public final BlockState originalState;
        public final BlockPos movedPos;
        public final BlockState stateAtMovedPos;

        public BlockInfo(BlockPos originalPos, BlockState originalState, BlockPos movedPos, BlockState stateAtMovedPos) {
            this.originalPos = originalPos;
            this.originalState = originalState;
            this.movedPos = movedPos;
            this.stateAtMovedPos = stateAtMovedPos;
        }
    }

    public static class BurialData {
        public final ServerLevel level;
        public final List<BlockInfo> savedBlocks;
        public final Vec3 effectPos;
        public int ticksRemaining;

        public BurialData(ServerLevel level, List<BlockInfo> savedBlocks, Vec3 effectPos) {
            this.level = level;
            this.savedBlocks = savedBlocks;
            this.effectPos = effectPos;
            this.ticksRemaining = RESTORE_DELAY;
        }
    }

    public static boolean activateAbility(Player player, Level level) {
        if (!(level instanceof ServerLevel serverLevel)) return false;

        LivingEntity target = getTargetedEntity(player);
        if (target == null || activeBurials.containsKey(target.getUUID())) return false;

        Direction facing = player.getDirection();
        Direction side = facing.getClockWise();

        // 1. Находим ТОЧНЫЙ центр цели
        Vec3 targetPos = target.position();
        double targetY = target.getY();

        // 2. Определяем два блока по ширине (ось side)
        // Находим ближайшую к центру моба целую границу блоков по нужной оси
        Direction.Axis sideAxis = side.getAxis();
        double centerCoord = (sideAxis == Direction.Axis.X) ? target.getX() : target.getZ();

        // Ближайший "шов" между блоками
        int gridLine = (int) Math.round(centerCoord);

        // Определяем, какой из двух блоков находится со стороны 'side', а какой с 'opposite'
        // В Minecraft: East = +X, South = +Z
        int step = side.getStepX() + side.getStepZ();
        BlockPos posSide;
        BlockPos posOpposite;

        if (step > 0) {
            // Если side смотрит в сторону увеличения координат (East/South)
            posSide = getPosAtAxis(targetPos, sideAxis, gridLine, targetY);
            posOpposite = getPosAtAxis(targetPos, sideAxis, gridLine - 1, targetY);
        } else {
            // Если side смотрит в сторону уменьшения (West/North)
            posSide = getPosAtAxis(targetPos, sideAxis, gridLine - 1, targetY);
            posOpposite = getPosAtAxis(targetPos, sideAxis, gridLine, targetY);
        }

        List<BlockInfo> blocksToSave = new ArrayList<>();

        // 3. Собираем блоки (длина 3, ширина 2)
        for (int i = -1; i <= 1; i++) {
            // Блок со стороны 'side'
            saveColumn(serverLevel, posSide.relative(facing, i), side, blocksToSave);
            // Блок со стороны 'opposite'
            saveColumn(serverLevel, posOpposite.relative(facing, i), side.getOpposite(), blocksToSave);
        }

        if (blocksToSave.isEmpty()) return false;

        // 4. Сдвиг
        for (BlockInfo info : blocksToSave) {
            serverLevel.setBlock(info.movedPos, info.originalState, 3);
            serverLevel.setBlock(info.originalPos, Blocks.AIR.defaultBlockState(), 3);
        }

        activeBurials.put(target.getUUID(), new BurialData(serverLevel, blocksToSave, target.position()));

        // Твои частицы при старте (замени SMOKE на свои)
        spawnParticles(serverLevel, target.position(), ParticleTypes.SMOKE);

        return true;
    }

    private static BlockPos getPosAtAxis(Vec3 base, Direction.Axis axis, int coord, double y) {
        if (axis == Direction.Axis.X) {
            return BlockPos.containing(coord, y - 1, base.z);
        } else {
            return BlockPos.containing(base.x, y - 1, coord);
        }
    }

    private static void saveColumn(ServerLevel level, BlockPos startPos, Direction pushDir, List<BlockInfo> list) {
        for (int y = 0; y < DEPTH; y++) {
            BlockPos current = startPos.below(y);
            BlockState state = level.getBlockState(current);
            if (state.isAir() || FORBIDDEN_BLOCKS.contains(state.getBlock())) continue;

            BlockPos moved = current.relative(pushDir);
            list.add(new BlockInfo(current, state, moved, level.getBlockState(moved)));
        }
    }

    private static void restoreBlocks(BurialData data) {
        for (int i = data.savedBlocks.size() - 1; i >= 0; i--) {
            BlockInfo info = data.savedBlocks.get(i);
            data.level.setBlock(info.originalPos, info.originalState, 3);
            data.level.setBlock(info.movedPos, info.stateAtMovedPos, 3);
        }
        // Твои частицы при восстановлении (замени SMOKE на свои)
        spawnParticles(data.level, data.effectPos, ParticleTypes.SMOKE);
    }

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        activeBurials.entrySet().removeIf(entry -> {
            BurialData data = entry.getValue();
            data.ticksRemaining--;
            Entity target = data.level.getEntity(entry.getKey());
            if (data.ticksRemaining <= 0 || target == null || !target.isAlive()) {
                restoreBlocks(data);
                return true;
            }
            return false;
        });
    }

    @SubscribeEvent
    public static void onServerStopping(ServerStoppingEvent event) {
        activeBurials.values().forEach(BurialAbility::restoreBlocks);
        activeBurials.clear();
    }

    private static void spawnParticles(ServerLevel level, Vec3 pos, ParticleOptions particle) {
        level.sendParticles(particle, pos.x, pos.y + 0.5, pos.z, 40, 0.7, 0.5, 0.7, 0.1);
    }

    @Nullable
    private static LivingEntity getTargetedEntity(Player player) {
        Vec3 eyePos = player.getEyePosition();
        Vec3 lookVec = player.getLookAngle().scale(RANGE);
        Vec3 endPos = eyePos.add(lookVec);
        AABB searchBox = player.getBoundingBox().expandTowards(lookVec).inflate(1.0D);
        LivingEntity bestTarget = null;
        double closestDist = RANGE;

        for (Entity entity : player.level().getEntities(player, searchBox, e -> e instanceof LivingEntity && e.isAlive())) {
            AABB targetBox = entity.getBoundingBox().inflate(0.3);
            Optional<Vec3> hit = targetBox.clip(eyePos, endPos);
            if (hit.isPresent()) {
                double dist = eyePos.distanceTo(hit.get());
                if (dist < closestDist) {
                    closestDist = dist;
                    bestTarget = (LivingEntity) entity;
                }
            }
        }
        return bestTarget;
    }
}