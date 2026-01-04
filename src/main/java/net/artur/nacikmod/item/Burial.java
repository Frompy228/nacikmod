package net.artur.nacikmod.item;

import net.artur.nacikmod.item.ability.BurialAbility;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.Level;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class Burial extends Item {
    public Burial() {
        super(new Properties().stacksTo(1));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack itemStack = player.getItemInHand(hand);

        if (!level.isClientSide) {
            // Проверка кулдауна
            if (player.getCooldowns().isOnCooldown(this)) {
                float cooldownPercent = player.getCooldowns().getCooldownPercent(this, 0);
                int cooldownSeconds = (int) (cooldownPercent * 2); // 2 секунды = 100%
                player.sendSystemMessage(Component.literal("Погребение на кулдауне: " + cooldownSeconds + " секунд осталось!")
                        .withStyle(ChatFormatting.RED));
                return InteractionResultHolder.success(itemStack);
            }

            // Активация способности
            if (BurialAbility.activateAbility(player, level)) {
                // Установка кулдауна (2 секунды для теста)
                player.getCooldowns().addCooldown(this, 40);
                player.sendSystemMessage(Component.literal("Погребение активировано!")
                        .withStyle(ChatFormatting.GREEN));
            } else {
            }
        }

        return InteractionResultHolder.success(itemStack);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltipComponents, net.minecraft.world.item.TooltipFlag isAdvanced) {
        super.appendHoverText(stack, level, tooltipComponents, isAdvanced);
        tooltipComponents.add(Component.translatable("item.nacikmod.burial.desc1"));
        tooltipComponents.add(Component.translatable("item.nacikmod.burial.desc2"));
    }
}

