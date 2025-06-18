package net.artur.nacikmod.worldgen.portal;

import net.artur.nacikmod.block.custom.ModPortalBlock;
import net.artur.nacikmod.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.common.util.ITeleporter;

import java.util.function.Function;

public class ModTeleporter implements ITeleporter {
    public static BlockPos thisPos = BlockPos.ZERO;
    public static boolean insideDimension = true;

    public ModTeleporter(BlockPos pos, boolean insideDim) {
        thisPos = pos;
        insideDimension = insideDim;
    }

    @Override
    public Entity placeEntity(Entity entity, ServerLevel currentWorld, ServerLevel destinationWorld,
                              float yaw, Function<Boolean, Entity> repositionEntity) {
        entity = repositionEntity.apply(false);

        if (!insideDimension) {
            // Телепортация из измерения
            BlockPos destinationPos = new BlockPos(thisPos.getX(), thisPos.getY(), thisPos.getZ());
            entity.setPos(destinationPos.getX(), destinationPos.getY(), destinationPos.getZ());
            return entity;
        }

        // Телепортация в измерение
        BlockPos platformPos = new BlockPos(0, 70, 0);

        // Создаем платформу 5x5
        for (int x = -2; x <= 2; x++) {
            for (int z = -2; z <= 2; z++) {
                destinationWorld.setBlock(platformPos.offset(x, 0, z), Blocks.STONE.defaultBlockState(), 3);
            }
        }

        // Размещаем портал на платформе
        destinationWorld.setBlock(platformPos, ModBlocks.MOD_PORTAL.get().defaultBlockState(), 3);

        // Телепортируем игрока на платформу
        entity.setPos(platformPos.getX() + 0.5, platformPos.getY() + 1, platformPos.getZ() + 0.5);

        return entity;
    }
}
