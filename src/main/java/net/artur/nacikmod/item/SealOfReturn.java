package net.artur.nacikmod.item;

import net.artur.nacikmod.capability.mana.ManaProvider;
import net.artur.nacikmod.network.ModMessages;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.util.ITeleporter;
import net.minecraftforge.network.PacketDistributor;

import java.util.List;
import java.util.function.Function;

public class SealOfReturn extends Item {
    private static final int MANA_COST = 1000;

    public SealOfReturn(Properties properties) {
        super(properties);
    }

    private static class SimpleTeleporter implements ITeleporter {
        private final BlockPos targetPos;

        public SimpleTeleporter(BlockPos targetPos) {
            this.targetPos = targetPos;
        }

        @Override
        public Entity placeEntity(Entity entity, ServerLevel currentWorld, ServerLevel destinationWorld,
                                float yaw, Function<Boolean, Entity> repositionEntity) {
            entity = repositionEntity.apply(false);
            entity.teleportTo(targetPos.getX() + 0.5, targetPos.getY(), targetPos.getZ() + 0.5);
            return entity;
        }
    }

    private boolean isValidBed(ServerPlayer player) {
        if (player.getRespawnPosition() == null) {
            return false;
        }
        
        ServerLevel respawnLevel = player.server.getLevel(player.getRespawnDimension());
        if (respawnLevel == null) {
            return false;
        }
        
        BlockState blockState = respawnLevel.getBlockState(player.getRespawnPosition());
        Block block = blockState.getBlock();
        
        // Проверяем, что блок на позиции кровати действительно является кроватью
        return block.toString().contains("bed") || block.toString().contains("BedBlock");
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (!level.isClientSide() && player instanceof ServerPlayer serverPlayer) {
            serverPlayer.getCapability(ManaProvider.MANA_CAPABILITY).ifPresent(mana -> {
                if (mana.getMana() >= MANA_COST) {
                    // Расход маны
                    mana.removeMana(MANA_COST);
                    ModMessages.sendManaToClient(serverPlayer, mana.getMana(), mana.getMaxMana());

                    // Определяем точку телепортации и измерение
                    if (serverPlayer.getRespawnPosition() != null && isValidBed(serverPlayer)) {
                        // Если у игрока есть кровать и она существует, телепортируем на неё
                        BlockPos respawnPos = serverPlayer.getRespawnPosition();
                        serverPlayer.changeDimension(serverPlayer.server.getLevel(serverPlayer.getRespawnDimension()), 
                            new SimpleTeleporter(respawnPos));
                    } else {
                        // Если кровати нет или она сломана, телепортируем в верхний мир на точку спавна
                        BlockPos spawnPos = serverPlayer.server.overworld().getSharedSpawnPos();
                        serverPlayer.changeDimension(serverPlayer.server.overworld(), 
                            new SimpleTeleporter(spawnPos));
                    }

                    // Синхронизированное удаление предмета
                    if (!player.getAbilities().instabuild) {
                        stack.shrink(1);
                        player.getInventory().setChanged();

                        // Явная синхронизация инвентаря
                        serverPlayer.containerMenu.broadcastChanges();
                    }

                    // Сообщение игроку
                    serverPlayer.sendSystemMessage(Component.literal(
                        (serverPlayer.getRespawnPosition() != null && isValidBed(serverPlayer))
                            ? "Teleported to your bed!" 
                            : "Teleported to spawn!"
                    ).withStyle(ChatFormatting.GREEN));

                    // Синхронизация звука телепортации
                    level.playSound(null,
                            serverPlayer.getX(),
                            serverPlayer.getY(),
                            serverPlayer.getZ(),
                            SoundEvents.ENDERMAN_TELEPORT,
                            SoundSource.PLAYERS,
                            1.0F,
                            1.0F);
                }
            });
        }

        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }



    @Override
    public void appendHoverText(ItemStack stack, Level level, List<net.minecraft.network.chat.Component> tooltip, TooltipFlag flag) {
        tooltip.add(net.minecraft.network.chat.Component.translatable("item.nacikmod.seal_of_return.desc1"));
        tooltip.add(net.minecraft.network.chat.Component.translatable("item.nacikmod.seal_of_return.desc2")
                .withStyle(style -> style.withColor(0x00FFFF))); // Цвет - голубой
        tooltip.add(Component.translatable("item.disappears")
                .withStyle(ChatFormatting.GRAY));
    }
}
