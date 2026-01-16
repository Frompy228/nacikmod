package net.artur.nacikmod.worldgen.dimension;

import net.artur.nacikmod.NacikMod;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.level.levelgen.VerticalAnchor;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.placement.*;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.List;

public class ModFeatures {
    // 1. Регистрация самого типа фичи (её логики)
    public static final DeferredRegister<Feature<?>> FEATURES = DeferredRegister.create(ForgeRegistries.FEATURES, NacikMod.MOD_ID);
    public static final RegistryObject<Feature<BedrockPillarConfig>> BEDROCK_PILLAR = FEATURES.register("bedrock_pillar",
            () -> new BedrockPillarFeature(BedrockPillarConfig.CODEC));

    // 2. Ключи для реестров (понадобятся в Bootstrap)
    public static final ResourceKey<ConfiguredFeature<?,?>> PILLAR_CONFIGURED = ResourceKey.create(Registries.CONFIGURED_FEATURE,
            new ResourceLocation(NacikMod.MOD_ID, "bedrock_pillar"));
    public static final ResourceKey<PlacedFeature> PILLAR_PLACED = ResourceKey.create(Registries.PLACED_FEATURE,
            new ResourceLocation(NacikMod.MOD_ID, "bedrock_pillar"));

    // 3. Настройка параметров (высота, ширина)
    public static void bootstrapConfigured(BootstapContext<ConfiguredFeature<?,?>> context) {
        context.register(PILLAR_CONFIGURED, new ConfiguredFeature<>(BEDROCK_PILLAR.get(),
                new BedrockPillarConfig(UniformInt.of(1, 8), UniformInt.of(40, 60))));
    }

    // 4. Правила размещения (частота, разброс)
    public static void bootstrapPlaced(BootstapContext<PlacedFeature> context) {
        var configured = context.lookup(Registries.CONFIGURED_FEATURE);
        context.register(PILLAR_PLACED, new PlacedFeature(configured.getOrThrow(PILLAR_CONFIGURED),
                List.of(
                        CountPlacement.of(20), // 20 попыток на чанк
                        InSquarePlacement.spread(), // Разброс по чанку
                        BiomeFilter.biome()
                )));
    }
}