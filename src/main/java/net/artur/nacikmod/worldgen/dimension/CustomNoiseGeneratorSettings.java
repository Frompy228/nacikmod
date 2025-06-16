package net.artur.nacikmod.worldgen.dimension;

import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import net.minecraft.world.level.levelgen.NoiseRouter;
import net.minecraft.world.level.levelgen.NoiseSettings;
import net.minecraft.world.level.levelgen.SurfaceRules;
import net.minecraft.world.level.levelgen.DensityFunctions;
import net.artur.nacikmod.NacikMod;

import java.util.List;

public class CustomNoiseGeneratorSettings {
    public static final ResourceKey<NoiseGeneratorSettings> EMPTY = ResourceKey.create(Registries.NOISE_SETTINGS,
            new ResourceLocation(NacikMod.MOD_ID, "empty"));

    public static void bootstrap(BootstapContext<NoiseGeneratorSettings> context) {
        context.register(EMPTY, empty());
    }

    public static NoiseGeneratorSettings empty() {
        // Создаем настройки шума для пустого мира
        NoiseSettings noiseSettings = NoiseSettings.create(0, 256, 1, 1);

        // Создаем пустые правила поверхности
        SurfaceRules.RuleSource emptySurfaceRules = SurfaceRules.sequence(
            SurfaceRules.ifTrue(
                SurfaceRules.ON_FLOOR,
                SurfaceRules.state(Blocks.AIR.defaultBlockState())
            )
        );

        // Создаем пустой NoiseRouter
        NoiseRouter emptyRouter = new NoiseRouter(
            DensityFunctions.zero(), // barrier
            DensityFunctions.zero(), // fluid_level_floodedness
            DensityFunctions.zero(), // fluid_level_spread
            DensityFunctions.zero(), // lava
            DensityFunctions.zero(), // temperature
            DensityFunctions.zero(), // vegetation
            DensityFunctions.zero(), // continents
            DensityFunctions.zero(), // erosion
            DensityFunctions.zero(), // depth
            DensityFunctions.zero(), // ridges
            DensityFunctions.zero(), // initial_density_without_jaggedness
            DensityFunctions.zero(), // final_density
            DensityFunctions.zero(), // vein_toggle
            DensityFunctions.zero(), // vein_ridged
            DensityFunctions.zero()  // vein_gap
        );

        // Создаем пустые настройки генератора
        return new NoiseGeneratorSettings(
            noiseSettings,
            Blocks.AIR.defaultBlockState(), // defaultBlock
            Blocks.AIR.defaultBlockState(), // defaultFluid
            emptyRouter, // noiseRouter
            emptySurfaceRules, // surfaceRule
            List.of(), // spawnTarget
            0, // seaLevel
            true, // disableMobGeneration
            false, // aquifersEnabled
            false, // oreVeinsEnabled
            false  // useLegacyRandomSource
        );
    }
}
