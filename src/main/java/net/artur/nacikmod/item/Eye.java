package net.artur.nacikmod.item;

import net.artur.nacikmod.item.ability.EyeAbility;
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
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class Eye extends Item {
    private static final String ACTIVE_TAG = "EyesActive";

    public Eye(Properties properties) {
        super(properties.rarity(Rarity.EPIC).stacksTo(1));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (level.isClientSide) {
            return InteractionResultHolder.success(stack);
        }

        // Переключаем состояние глаз
        boolean isActive = stack.hasTag() && stack.getTag().getBoolean(ACTIVE_TAG);
        boolean newState = !isActive;
        
        stack.getOrCreateTag().putBoolean(ACTIVE_TAG, newState);
        EyeAbility.setEyesActive(player, newState);

        // Отправляем сообщение игроку
        if (player instanceof ServerPlayer serverPlayer) {
            if (newState) {
                serverPlayer.sendSystemMessage(Component.literal("Eyes activated!")
                        .withStyle(ChatFormatting.GREEN), true);
            } else {
                serverPlayer.sendSystemMessage(Component.literal("Eyes deactivated!")
                        .withStyle(ChatFormatting.RED), true);
            }
        }

        return InteractionResultHolder.success(stack);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltipComponents, TooltipFlag isAdvanced) {
        super.appendHoverText(stack, level, tooltipComponents, isAdvanced);

        boolean isActive = stack.hasTag() && stack.getTag().getBoolean(ACTIVE_TAG);
        if (isActive) {
            tooltipComponents.add(Component.literal("Eyes: Active")
                    .withStyle(ChatFormatting.GREEN));
        } else {
            tooltipComponents.add(Component.literal("Eyes: Inactive")
                    .withStyle(ChatFormatting.GRAY));
        }
        
        tooltipComponents.add(Component.literal("Right-click to toggle eyes")
                .withStyle(ChatFormatting.DARK_AQUA));
    }
}
