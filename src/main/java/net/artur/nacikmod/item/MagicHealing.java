package net.artur.nacikmod.item;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.artur.nacikmod.item.ability.HealingOfShallowWounds;
import net.artur.nacikmod.capability.mana.ManaProvider;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class MagicHealing extends Item {
    private static final String ACTIVE_TAG = "active";
    private static final String HEALING_AMOUNT_TAG = "healing_amount";

    public MagicHealing(Properties properties) {
        super(properties.stacksTo(1).fireResistant());
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack itemStack = player.getItemInHand(hand);
        
        if (!level.isClientSide) {
            // Обновляем значение силы лечения
            float healingAmount = HealingOfShallowWounds.calculateHealingAmount(player);
            itemStack.getOrCreateTag().putFloat(HEALING_AMOUNT_TAG, healingAmount);

            // Toggle healing effect
            if (HealingOfShallowWounds.isHealingActive(player)) {
                HealingOfShallowWounds.stopHealing(player);
                itemStack.getOrCreateTag().putBoolean(ACTIVE_TAG, false);
            } else {
                HealingOfShallowWounds.startHealing(player);
                itemStack.getOrCreateTag().putBoolean(ACTIVE_TAG, true);
            }
            
            // Start cooldown to prevent spam
            player.getCooldowns().addCooldown(this, 20); // 1 second cooldown
        }
        
        return InteractionResultHolder.success(itemStack);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltipComponents, TooltipFlag isAdvanced) {
        super.appendHoverText(stack, level, tooltipComponents, isAdvanced);

        tooltipComponents.add(Component.translatable("item.nacikmod.magic_healing.desc1"));
        float healingAmount = stack.hasTag() ? stack.getTag().getFloat(HEALING_AMOUNT_TAG) : 1.0f;
        tooltipComponents.add(Component.translatable("item.nacikmod.magic_healing.desc2", healingAmount)
                .withStyle(style -> style.withColor(0x00FFFF)));

        boolean isActive = stack.hasTag() && stack.getTag().getBoolean(ACTIVE_TAG);
        if (isActive) {
            tooltipComponents.add(Component.translatable("item.nacikmod.magic_healing.active")
                    .withStyle(ChatFormatting.GREEN));
        } else {
            tooltipComponents.add(Component.translatable("item.nacikmod.magic_healing.inactive")
                    .withStyle(ChatFormatting.RED));
        }

    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return stack.hasTag() && stack.getTag().getBoolean(ACTIVE_TAG);
    }
}
