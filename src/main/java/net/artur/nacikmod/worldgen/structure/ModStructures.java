package net.artur.nacikmod.worldgen.structure;

import net.artur.nacikmod.NacikMod;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

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
        if (level.dimension().location().getPath().equals("sparta")) {
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
}