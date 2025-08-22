package net.artur.nacikmod.block.custom;

import net.artur.nacikmod.entity.projectiles.FireHailEntity;
import net.artur.nacikmod.registry.ModEntities;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
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

public class FireTrap extends Block {
    private static final VoxelShape SHAPE = Block.box(0, 0, 0, 16, 2, 16); // 0.125 блока высоты (2/16)

    public FireTrap(Properties properties) {
        super(properties);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable BlockGetter level, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);
        tooltip.add(Component.translatable("block.nacikmod.fire_trap.desc1"));
        tooltip.add(Component.translatable("block.nacikmod.fire_trap.desc2")
                .withStyle(style -> style.withColor(0x00FFFF)));
        tooltip.add(Component.translatable("item.disappears")
                .withStyle(ChatFormatting.GRAY));
    }

    @Override
    public void stepOn(Level level, BlockPos pos, BlockState state, Entity entity) {
        super.stepOn(level, pos, state, entity);
        if (!level.isClientSide && entity instanceof LivingEntity livingEntity) {
            
            // Наносим урон и эффекты всем живым сущностям
            livingEntity.hurt(level.damageSources().magic(), 7.0f);
            livingEntity.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 100, 0));
            
            // Удаляем блок
            level.removeBlock(pos, false);

            // Спавним снаряды единоразово
            if (level instanceof ServerLevel serverLevel) {
                spawnHailStorm(serverLevel, pos);
            }
        }
    }

    @Override
    public void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
        super.entityInside(state, level, pos, entity);
        if (!level.isClientSide && entity instanceof LivingEntity livingEntity) {
            // Наносим урон и эффекты всем живым сущностям
            livingEntity.hurt(level.damageSources().magic(), 5.0f);
            livingEntity.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 120, 1));
            
            // Удаляем блок
            level.removeBlock(pos, false);

            // Спавним снаряды единоразово
            if (level instanceof ServerLevel serverLevel) {
                spawnHailStorm(serverLevel, pos);
            }
        }
    }

    private void spawnHailStorm(ServerLevel serverLevel, BlockPos trapPos) {
        RandomSource random = serverLevel.getRandom();


        int count = 40 + random.nextInt(11);

        for (int i = 0; i < count; i++) {
            // Случайная позиция в радиусе 10 блоков
            double angle = random.nextDouble() * 2 * Math.PI;
            double radius = random.nextDouble() * 10;

            double x = trapPos.getX() + radius * Math.cos(angle);
            double z = trapPos.getZ() + radius * Math.sin(angle);
            double y = trapPos.getY() + 8 + random.nextDouble() * 4; // Высота от 8 до 12 блоков

            // Создаем FireHailEntity
            FireHailEntity hail = new FireHailEntity(serverLevel, null);
            hail.setPos(x, y, z);
            serverLevel.addFreshEntity(hail);
        }
    }
}
