package net.artur.nacikmod.registry;

import net.artur.nacikmod.NacikMod;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.biome.*;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.SurfaceRules;
import net.minecraft.world.level.block.Blocks;

public class ModBiomes {
    public static final ResourceKey<Biome> EMPTY_BIOME_KEY = ResourceKey.create(Registries.BIOME,
            new ResourceLocation(NacikMod.MOD_ID, "empty_biome"));

    public static void bootstrap(BootstapContext<Biome> context) {
        context.register(EMPTY_BIOME_KEY, emptyBiome(context));
    }

    public static Biome emptyBiome(BootstapContext<Biome> context) {
        MobSpawnSettings.Builder spawnBuilder = new MobSpawnSettings.Builder();
        
        BiomeGenerationSettings.Builder generationBuilder = new BiomeGenerationSettings.Builder(
            context.lookup(Registries.PLACED_FEATURE),
            context.lookup(Registries.CONFIGURED_CARVER)
        );
        
        // Create surface rules that only generate air
        SurfaceRules.RuleSource surfaceRule = SurfaceRules.sequence(
            SurfaceRules.ifTrue(
                SurfaceRules.ON_FLOOR,
                SurfaceRules.state(Blocks.AIR.defaultBlockState())
            )
        );

        return new Biome.BiomeBuilder()
            .hasPrecipitation(false)
            .temperature(0.8f)
            .downfall(0.0f)
            .specialEffects(new BiomeSpecialEffects.Builder()
                .waterColor(0x3F76E4)
                .waterFogColor(0x050533)
                .fogColor(0xC0D8FF)
                .skyColor(0x78A7FF)
                .build())
            .mobSpawnSettings(spawnBuilder.build())
            .generationSettings(generationBuilder.build())
                .build();
    }
} 