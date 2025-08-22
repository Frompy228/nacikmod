package net.artur.nacikmod.item;

import net.artur.nacikmod.capability.mana.IMana;
import net.artur.nacikmod.capability.mana.ManaProvider;
import net.artur.nacikmod.network.ModMessages;
import net.artur.nacikmod.registry.ModEffects;
import net.minecraft.ChatFormatting;
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
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class ManaBlessing extends Item {
    public ManaBlessing(Properties properties) {
        super(new Item.Properties()
                .rarity(Rarity.RARE).fireResistant()); // Редкость
    }
    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        if (!level.isClientSide) { // Выполняем только на сервере
            ItemStack stack = player.getItemInHand(hand);

            player.addEffect(new net.minecraft.world.effect.MobEffectInstance(
                    ModEffects.EFFECT_MANA_BLESSING.get(), -1, 0, false, false));

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

        tooltipComponents.add(Component.translatable("item.nacikmod.mana_blessing.desc1"));

        tooltipComponents.add(Component.translatable("item.disappears")
                .withStyle(ChatFormatting.GRAY));
    }
}
