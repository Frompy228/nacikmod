package net.artur.nacikmod.worldgen.dimension;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;

public class BedrockPillarFeature extends Feature<BedrockPillarConfig> {
    public BedrockPillarFeature(Codec<BedrockPillarConfig> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<BedrockPillarConfig> context) {
        WorldGenLevel level = context.level();
        BlockPos origin = context.origin();
        RandomSource random = context.random();
        BedrockPillarConfig config = context.config();

        int width = config.width().sample(random);
        int height = config.height().sample(random);
        int radius = width / 2;
        int checkRadius = radius + 1; // Проверка на 1 блок шире для дистанции

        // Проверка: нет ли других столбов рядом
        for (int x = -checkRadius; x <= checkRadius; x++) {
            for (int z = -checkRadius; z <= checkRadius; z++) {
                if (level.getBlockState(origin.offset(x, 0, z)).is(Blocks.BEDROCK)) {
                    return false;
                }
            }
        }

        // Строим столб от дна мира до заданной высоты
        for (int dy = 0; dy < height; dy++) {
            for (int dx = -radius; dx <= radius; dx++) {
                for (int dz = -radius; dz <= radius; dz++) {
                    BlockPos pos = new BlockPos(origin.getX() + dx, level.getMinBuildHeight() + dy, origin.getZ() + dz);
                    if (pos.getY() < level.getMaxBuildHeight()) {
                        level.setBlock(pos, Blocks.BEDROCK.defaultBlockState(), 2);
                    }
                }
            }
        }
        return true;
    }
}