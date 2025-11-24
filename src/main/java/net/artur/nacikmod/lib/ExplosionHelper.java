package net.artur.nacikmod.lib;

import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ThreadedLevelLightEngine;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.phys.AABB;

import java.util.*;

public class ExplosionHelper {

    private final ServerLevel level;
    private final BlockPos center;
    private final int radius;

    private final Set<LevelChunk> modifiedChunks = new HashSet<>();
    private final Map<ChunkPos, LevelChunk> chunkCache = new HashMap<>();

    private final Queue<BlockPos> blocksToRemove = new LinkedList<>();
    private boolean finished = false;

    private static final BlockState AIR = Blocks.AIR.defaultBlockState();

    public ExplosionHelper(ServerLevel level, BlockPos center, int radius) {
        this.level = level;
        this.center = center;
        this.radius = radius;

        collectBlocks();
    }

    /** Собираем блоки для удаления */
    private void collectBlocks() {
        int rSq = radius * radius;
        for (int x = -radius; x <= radius; x++) {
            for (int y = -radius; y <= radius; y++) {
                for (int z = -radius; z <= radius; z++) {
                    if (x * x + y * y + z * z <= rSq) {
                        blocksToRemove.add(center.offset(x, y, z));
                    }
                }
            }
        }
    }

    /** Запускаем процесс */
    public void start() {
        level.getServer().execute(() -> new RemovalProcess(this).start());
    }

    private LevelChunk getChunk(BlockPos pos) {
        ChunkPos cp = new ChunkPos(pos);
        return chunkCache.computeIfAbsent(cp, p -> level.getChunk(cp.x, cp.z));
    }

    private LevelChunkSection getBlockStorage(BlockPos pos) {
        LevelChunk chunk = getChunk(pos);
        return chunk.getSection(chunk.getSectionIndex(pos.getY()));
    }

    private void removeBlock(BlockPos pos) {
        LevelChunkSection storage = getBlockStorage(pos);
        if (storage != null) {
            storage.setBlockState(pos.getX() & 15, pos.getY() & 15, pos.getZ() & 15, AIR);
        }
        modifiedChunks.add(getChunk(pos));
        level.getLightEngine().checkBlock(pos);
    }

    /** Завершение: обновление чанков и нанесение урона */
    private void finish() {
        // Обновляем освещение и отправляем чанки клиентам
        for (LevelChunk chunk : modifiedChunks) {
            chunk.setLightCorrect(false);
            ThreadedLevelLightEngine lightManager = (ThreadedLevelLightEngine) level.getLightEngine();
            level.getChunkSource().chunkMap.getPlayers(chunk.getPos(), false)
                    .forEach(p -> p.connection.send(new net.minecraft.network.protocol.game.ClientboundLevelChunkWithLightPacket(
                            chunk, level.getLightEngine(), null, null
                    )));
        }
        modifiedChunks.clear();

        // Наносим урон всем существам в радиусе
        AABB aabb = new AABB(center).inflate(radius);
        for (Entity entity : level.getEntities(null, aabb)) {
            if (entity instanceof LivingEntity living) {
                double dist = entity.distanceToSqr(center.getX(), center.getY(), center.getZ());
                if (dist <= radius * radius) {
                    float damage = (float) (1000.0 * (1.0 - (Math.sqrt(dist) / radius))); // чем ближе, тем больше урон
                    living.hurt(level.damageSources().explosion(null), damage);
                }
            }
        }

        finished = true;
    }

    public boolean isFinished() {
        return finished;
    }

    /** Внутренний процесс по тик-лимиту */
    private static class RemovalProcess {
        private final ExplosionHelper helper;
        private final MinecraftServer server;

        public RemovalProcess(ExplosionHelper helper) {
            this.helper = helper;
            this.server = helper.level.getServer();
        }

        public void start() {
            server.execute(this::tick);
        }

        private void tick() {
            long start = Util.getMillis();
            while (!helper.blocksToRemove.isEmpty() && Util.getMillis() - start < 40) {
                helper.removeBlock(helper.blocksToRemove.poll());
            }

            if (helper.blocksToRemove.isEmpty()) {
                helper.finish();
            } else {
                server.execute(this::tick);
            }
        }
    }
}
