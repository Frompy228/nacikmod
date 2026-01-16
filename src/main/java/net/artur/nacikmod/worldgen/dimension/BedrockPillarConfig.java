package net.artur.nacikmod.worldgen.dimension;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.valueproviders.IntProvider;
// В Forge 1.20.1 используется именно этот интерфейс:
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;

public record BedrockPillarConfig(IntProvider width, IntProvider height) implements FeatureConfiguration {
    public static final Codec<BedrockPillarConfig> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    IntProvider.codec(1, 8).fieldOf("width").forGetter(BedrockPillarConfig::width),
                    IntProvider.codec(1, 256).fieldOf("height").forGetter(BedrockPillarConfig::height)
            ).apply(instance, BedrockPillarConfig::new)
    );
}