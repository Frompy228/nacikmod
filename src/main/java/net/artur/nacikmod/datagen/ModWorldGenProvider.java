package net.artur.nacikmod.datagen;
import net.artur.nacikmod.NacikMod;
import net.artur.nacikmod.registry.ModBiomes;
import net.artur.nacikmod.worldgen.dimension.ModDimensions;
import net.artur.nacikmod.worldgen.dimension.CustomNoiseGeneratorSettings;
import net.artur.nacikmod.worldgen.dimension.ModFeatures;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraftforge.common.data.DatapackBuiltinEntriesProvider;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Set;
import java.util.concurrent.CompletableFuture;

public class ModWorldGenProvider extends DatapackBuiltinEntriesProvider {
    public static final RegistrySetBuilder BUILDER = new RegistrySetBuilder()
            .add(Registries.CONFIGURED_FEATURE, ModFeatures::bootstrapConfigured)
            .add(Registries.PLACED_FEATURE, ModFeatures::bootstrapPlaced)
            .add(Registries.BIOME, ModBiomes::bootstrap)
            .add(Registries.DIMENSION_TYPE, ModDimensions::bootstrapType)
            .add(Registries.LEVEL_STEM, ModDimensions::bootstrapStem)
            .add(Registries.NOISE_SETTINGS, CustomNoiseGeneratorSettings::bootstrap);

    public ModWorldGenProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, registries, BUILDER, Set.of(NacikMod.MOD_ID));
    }
}
