package net.artur.nacikmod.item;

import net.artur.nacikmod.capability.mana.IMana;
import net.artur.nacikmod.capability.mana.ManaProvider;
import net.artur.nacikmod.network.ModMessages;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.network.PacketDistributor;

import java.util.List;

public class MagicCircuit extends Item {

    public MagicCircuit(Properties properties) {
        super(new Item.Properties()
                .rarity(Rarity.UNCOMMON)); // Редкость
    }


    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        if (!level.isClientSide) { // Выполняем только на сервере
            ItemStack stack = player.getItemInHand(hand);

            LazyOptional<IMana> manaCap = player.getCapability(ManaProvider.MANA_CAPABILITY);
            manaCap.ifPresent(mana -> {
                int newMaxMana = mana.getMaxMana() + 50;
                mana.setMaxMana(newMaxMana); // Увеличиваем макс. ману

                if (player instanceof ServerPlayer serverPlayer && serverPlayer.connection != null) {
                    ModMessages.sendManaToClient(serverPlayer, mana.getMana(), mana.getMaxMana());
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
    public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(net.minecraft.network.chat.Component.translatable("item.nacikmod.magic_circuit.desc1")
                .withStyle(style -> style.withColor(0x00FFFF))); // Цвет - голубой
    }
}
