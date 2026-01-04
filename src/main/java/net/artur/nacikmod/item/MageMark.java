package net.artur.nacikmod.item;

import net.artur.nacikmod.capability.mana.ManaProvider;
import net.artur.nacikmod.registry.ModMessages;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class MageMark extends Item {
    
    public MageMark(Properties properties) {
        super(new Item.Properties()
                .rarity(ShardArtifact.RED).fireResistant()); // Редкость
    }
    
    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        if (!level.isClientSide) { // Выполняем только на сервере
            ItemStack stack = player.getItemInHand(hand);

            // Получаем mana capability
            player.getCapability(ManaProvider.MANA_CAPABILITY).ifPresent(mana -> {
                // Устанавливаем статус "Истинный маг"
                mana.setTrueMage(true);

                // Принудительно синхронизируем статус с клиентом
                if (player instanceof net.minecraft.server.level.ServerPlayer serverPlayer) {
                    ModMessages.sendTrueMageStatusToClient(serverPlayer, true);
                }
            });

            // Удаляем предмет после использования
            if (!player.isCreative()) { // Если игрок не в креативе, тратим предмет
                stack.shrink(1);
            }
        }

        return InteractionResultHolder.success(player.getItemInHand(hand));
    }
    
    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltipComponents, TooltipFlag isAdvanced) {
        super.appendHoverText(stack, level, tooltipComponents, isAdvanced);

        tooltipComponents.add(Component.translatable("item.nacikmod.mage_mark.desc1"));

        tooltipComponents.add(Component.translatable("item.disappears")
                .withStyle(ChatFormatting.GRAY));
    }
}