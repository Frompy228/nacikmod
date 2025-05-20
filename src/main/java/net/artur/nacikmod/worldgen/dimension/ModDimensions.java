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
import net.minecraft.world.level.levelgen.flat.FlatLevelGeneratorSettings;

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
                OptionalLong.of(12000), // fixedTime
                true, // hasSkylight
                false, // hasCeiling
                false, // ultraWarm
                false, // natural
                1.0, // coordinateScale
                true, // bedWorks
                false, // respawnAnchorWorks
                0, // minY
                256, // height
                256, // logicalHeight
                BlockTags.INFINIBURN_OVERWORLD, // infiniburn
                BuiltinDimensionTypes.OVERWORLD_EFFECTS, // effectsLocation
                1.0f, // ambientLight
                new DimensionType.MonsterSettings(false, false, ConstantInt.of(0), 0)));
    }

    public static void bootstrapStem(BootstapContext<LevelStem> context) {
        HolderGetter<Biome> biomeRegistry = context.lookup(Registries.BIOME);
        HolderGetter<DimensionType> dimTypes = context.lookup(Registries.DIMENSION_TYPE);
        HolderGetter<NoiseGeneratorSettings> noiseGenSettings = context.lookup(Registries.NOISE_SETTINGS);

        NoiseSettings noiseSettings = new NoiseSettings(
                0, // minY
                256, // height
                1, // noiseSizeHorizontal
                1  // noiseSizeVertical
        );

        NoiseBasedChunkGenerator wrappedChunkGenerator = new NoiseBasedChunkGenerator(
                new FixedBiomeSource(biomeRegistry.getOrThrow(Biomes.DESERT)),
                noiseGenSettings.getOrThrow(NoiseGeneratorSettings.OVERWORLD));
        

        LevelStem stem = new LevelStem(dimTypes.getOrThrow(ModDimensions.SPARTA_DIM_TYPE), wrappedChunkGenerator);

        context.register(SPARTA_KEY, stem);
    }
}

