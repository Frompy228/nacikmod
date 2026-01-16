package net.artur.nacikmod.worldgen.structure;

import net.artur.nacikmod.NacikMod;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.RandomSource;

import java.util.List;

@Mod.EventBusSubscriber(modid = NacikMod.MOD_ID)
public class ModStructures {
    private static final String STRUCTURE_GENERATED_TAG = "pocket_generated";
    private static final String STRUCTURE_NAME = "pocket";

    public static class StructureData extends SavedData {
        private boolean structureGenerated = false;

        public StructureData() {
            super();
        }

        public StructureData(CompoundTag tag) {
            this.structureGenerated = tag.getBoolean(STRUCTURE_GENERATED_TAG);
        }

        @Override
        public CompoundTag save(CompoundTag tag) {
            tag.putBoolean(STRUCTURE_GENERATED_TAG, structureGenerated);
            return tag;
        }

        public boolean isStructureGenerated() {
            return structureGenerated;
        }

        public void setStructureGenerated(boolean generated) {
            this.structureGenerated = generated;
            this.setDirty();
        }
    }

    @SubscribeEvent
    public static void onWorldLoad(LevelEvent.Load event) {
        if (!(event.getLevel() instanceof ServerLevel level)) return;
        if (level.dimension().location().getPath().equals("pocket")) {
            generateStructure(level);
        }
    }

    private static void generateStructure(ServerLevel level) {
        StructureData data = level.getDataStorage().computeIfAbsent(
                StructureData::new,
                StructureData::new,
                "nacikmod_structures"
        );

        if (data.isStructureGenerated()) {
            return; // Структура уже была сгенерирована
        }

        StructureTemplateManager manager = level.getStructureManager();
        ResourceLocation structureId = new ResourceLocation(NacikMod.MOD_ID, STRUCTURE_NAME);

        try {
            StructureTemplate template = manager.getOrCreate(structureId);
            if (template != null) {
                // Размещаем структуру на координатах 0 70 0
                BlockPos pos = new BlockPos(0, 70, 0);
                template.placeInWorld(level, pos, pos,
                        new StructurePlaceSettings(),
                        level.random, 3);

                // Отмечаем, что структура была сгенерирована
                data.setStructureGenerated(true);
            }
        } catch (Exception e) {
            NacikMod.LOGGER.error("Failed to generate structure: " + e.getMessage());
        }
    }

    private static void spawnDropStructureIfPossible(MinecraftServer server) {
        ServerLevel level = server.overworld();
        if (!level.dimension().location().equals(Level.OVERWORLD.location())) return;

        List<ServerPlayer> players = server.getPlayerList().getPlayers();
        if (players.isEmpty()) {
            return;
        }
        double avgX = 0, avgZ = 0;
        for (ServerPlayer player : players) {
            avgX += player.getX();
            avgZ += player.getZ();
        }
        avgX /= players.size();
        avgZ /= players.size();

        // Генерируем случайные смещения для x и z независимо друг от друга
        int xOffset = level.getRandom().nextInt(-3000, 3001); // от -3000 до +3000 включительно
        int zOffset = level.getRandom().nextInt(-3000, 3001); // от -3000 до +3000 включительно
        int x = (int)avgX + xOffset;
        int z = (int)avgZ + zOffset;

        // Принудительно загружаем чанк, чтобы getHeight вернул корректную высоту
        // Важно: это может вызвать генерацию чанка, что немного нагружает сервер, если делать часто и далеко от игроков
        level.getChunkSource().getChunk(x >> 4, z >> 4, true);
        int y = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z);
        BlockPos surfacePos = new BlockPos(x, y, z);

        StructureTemplateManager manager = level.getStructureManager();
        ResourceLocation structureRL = new ResourceLocation("nacikmod:drop");
        StructureTemplate template = manager.get(structureRL).orElse(null);
        if (template == null) {
            return;
        }

        template.placeInWorld(level, surfacePos, surfacePos, new StructurePlaceSettings(), level.getRandom(), 2);
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            player.sendSystemMessage(Component.literal(
                    "Something has appeared at: " + surfacePos.getX() + " " + surfacePos.getY() + " " + surfacePos.getZ()
            ));
        }
    }

    // --- Drop structure timed spawn logic ---
    private static int dropTickCounter = 0;
    private static final int DROP_TICKS_INTERVAL = 1000;
    private static final double DROP_SPAWN_CHANCE = 0.01; // 1%

    @net.minecraftforge.eventbus.api.SubscribeEvent
    public static void onServerTick(net.minecraftforge.event.TickEvent.ServerTickEvent event) {
        if (event.phase != net.minecraftforge.event.TickEvent.Phase.END) return;
        dropTickCounter++;
        if (dropTickCounter >= DROP_TICKS_INTERVAL) {
            dropTickCounter = 0;
            if (Math.random() < DROP_SPAWN_CHANCE && event.getServer() != null) {
                spawnDropStructureIfPossible(event.getServer());
            }
        }
    }
}