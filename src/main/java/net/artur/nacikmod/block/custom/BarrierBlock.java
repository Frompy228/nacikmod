package net.artur.nacikmod.block.custom;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class BarrierBlock extends Block {
    private static final VoxelShape SHAPE = Block.box(0, 0, 0, 16, 16, 16);
    private static final int DURATION_TICKS = 700;
    
    public BarrierBlock(Properties properties) {
        super(properties);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        // Возвращаем полный куб для коллизии - блок твердый и непроходимый
        return SHAPE;
    }


    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean isMoving) {
        if (!level.isClientSide) {
            // Планируем тик через 30 секунд для автоматического удаления
            level.scheduleTick(pos, this, DURATION_TICKS);
        }
    }


    @Override
    public void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        // Удаляем блок, если он все еще является barrier_block
        if (state.is(this)) {
            level.removeBlock(pos, false);
        }
    }

    // BarrierBlock может существовать в воздухе, поэтому не проверяем canSurvive
    // Блоки создаются программно и автоматически удаляются через 30 секунд
}
