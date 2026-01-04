package net.artur.nacikmod.item;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.artur.nacikmod.item.ability.BreakingBodyLimitAbility;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class BreakingBodyLimit extends Item {
    private static final String ACTIVE_TAG = "active";

    public BreakingBodyLimit(Properties properties) {
        super(properties.stacksTo(1).rarity(Rarity.EPIC).fireResistant());
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack itemStack = player.getItemInHand(hand);
        if (!level.isClientSide) {
            if (player.isShiftKeyDown()) {
                BreakingBodyLimitAbility.switchLevel(player);
            } else {
                if (BreakingBodyLimitAbility.isActive(player)) {
                    BreakingBodyLimitAbility.stop(player);
                    itemStack.getOrCreateTag().putBoolean(ACTIVE_TAG, false);
                } else {
                    BreakingBodyLimitAbility.start(player);
                    itemStack.getOrCreateTag().putBoolean(ACTIVE_TAG, true);
                }
            }
            player.getCooldowns().addCooldown(this, 20);
        }
        return InteractionResultHolder.success(itemStack);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltipComponents, TooltipFlag isAdvanced) {
        super.appendHoverText(stack, level, tooltipComponents, isAdvanced);
        BreakingBodyLimitAbility.Level currentLevel = BreakingBodyLimitAbility.getCurrentLevel(stack);
        // Получаем номер уровня (0-4) и добавляем 1 для отображения (1-5)
        int levelNumber = stack.hasTag() ? stack.getTag().getInt("level") : 0;
        tooltipComponents.add(Component.translatable("item.nacikmod.breaking_body_limit.desc1"));
        tooltipComponents.add(Component.translatable("item.nacikmod.breaking_body_limit.desc2", 
                String.format("%.1f", (float) currentLevel.hpCost), levelNumber + 1)
                .withStyle(ChatFormatting.DARK_RED));
        boolean isActive = stack.hasTag() && stack.getTag().getBoolean(ACTIVE_TAG);
        if (isActive) {
            tooltipComponents.add(Component.translatable("item.nacikmod.breaking_body_limit.active")
                    .withStyle(ChatFormatting.GREEN));
        } else {
            tooltipComponents.add(Component.translatable("item.nacikmod.breaking_body_limit.inactive")
                    .withStyle(ChatFormatting.RED));
        }
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return stack.hasTag() && stack.getTag().getBoolean(ACTIVE_TAG);
    }
}
