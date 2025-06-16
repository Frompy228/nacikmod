package net.artur.nacikmod.worldgen.structure;

import net.artur.nacikmod.NacikMod;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = NacikMod.MOD_ID)
public class ModStructures {
    public static final String PLATFORM_STRUCTURE = "platform";

    @SubscribeEvent
    public static void onWorldLoad(LevelEvent.Load event) {
        if (!(event.getLevel() instanceof ServerLevel level)) return;
        if (level.dimension().location().getPath().equals("sparta")) {
            generatePlatform(level);
        }
    }

    private static void generatePlatform(ServerLevel level) {
        StructureTemplateManager manager = level.getStructureManager();
        ResourceLocation structureId = new ResourceLocation(NacikMod.MOD_ID, PLATFORM_STRUCTURE);
        
        try {
            StructureTemplate template = manager.getOrCreate(structureId);
            if (template != null) {
                BlockPos pos = new BlockPos(0, 70, 0);
                template.placeInWorld(level, pos, pos, 
                    new StructureTemplate.PlaceSettings(), 
                    level.random, 3);
            }
        } catch (Exception e) {
            NacikMod.LOGGER.error("Failed to generate platform structure: " + e.getMessage());
        }
    }
} 