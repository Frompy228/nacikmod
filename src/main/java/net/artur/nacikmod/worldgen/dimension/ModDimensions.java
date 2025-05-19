package net.artur.nacikmod.worldgen.dimension;

import com.ibm.icu.impl.Pair;
import net.artur.nacikmod.NacikMod;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.valueproviders.ConstantInt;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.*;
import net.minecraft.world.level.dimension.BuiltinDimensionTypes;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import net.minecraft.world.level.levelgen.NoiseSettings;
import net.minecraft.world.level.levelgen.SurfaceRules;

import java.util.List;
import java.util.OptionalLong;

public class ModDimensions {
    public static final ResourceKey<LevelStem> SPARTA_KEY = ResourceKey.create(Registries.LEVEL_STEM,
            new ResourceLocation(NacikMod.MOD_ID, "sparta"));
    public static final ResourceKey<Level> SPARTA_LEVEL_KEY = ResourceKey.create(Registries.DIMENSION,
            new ResourceLocation(NacikMod.MOD_ID, "sparta"));
    public static final ResourceKey<DimensionType> SPARTA_DIM_TYPE = ResourceKey.create(Registries.DIMENSION_TYPE,
            new ResourceLocation(NacikMod.MOD_ID, "sparta_type"));

    public static void bootstrapType(BootstapContext<DimensionType> context) {
        context.register(SPARTA_DIM_TYPE, new DimensionType(
                OptionalLong.of(12000), // fixedTime - фиксированное время суток
                true, // hasSkylight - есть небо
                false, // hasCeiling - нет потолка
                false, // ultraWarm - не ультра-теплый (чтобы вода не испарялась)
                false, // natural - не натуральный мир
                1.0, // coordinateScale - масштаб координат
                true, // bedWorks - кровати работают
                false, // respawnAnchorWorks - якоря возрождения не работают
                0, // minY - минимальная высота
                256, // height - высота мира
                256, // logicalHeight - логическая высота
                BlockTags.INFINIBURN_OVERWORLD, // infiniburn - бесконечное горение
                BuiltinDimensionTypes.OVERWORLD_EFFECTS, // effectsLocation - эффекты как в обычном мире
                1.0f, // ambientLight - уровень освещения (1.0 = полное освещение)
                new DimensionType.MonsterSettings(false, false, ConstantInt.of(0), 0))); // отключаем спавн мобов
    }

    public static void bootstrapStem(BootstapContext<LevelStem> context) {
        HolderGetter<Biome> biomeRegistry = context.lookup(Registries.BIOME);
        HolderGetter<DimensionType> dimTypes = context.lookup(Registries.DIMENSION_TYPE);
        HolderGetter<NoiseGeneratorSettings> noiseGenSettings = context.lookup(Registries.NOISE_SETTINGS);

        // Создаем настройки для пустыни без воды
        NoiseSettings noiseSettings = new NoiseSettings(
                0, // minY
                256, // height
                1, // noiseSizeHorizontal
                1  // noiseSizeVertical
        );

        // Создаем генератор мира с настройками пустыни, но без воды
        NoiseBasedChunkGenerator wrappedChunkGenerator = new NoiseBasedChunkGenerator(
                new FixedBiomeSource(biomeRegistry.getOrThrow(Biomes.DESERT)),
                noiseGenSettings.getOrThrow(NoiseGeneratorSettings.OVERWORLD));

        LevelStem stem = new LevelStem(dimTypes.getOrThrow(ModDimensions.SPARTA_DIM_TYPE), wrappedChunkGenerator);

        context.register(SPARTA_KEY, stem);
    }
}

