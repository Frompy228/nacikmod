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
import net.artur.nacikmod.item.ability.ManaRelease;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class Release extends Item {
    private static final String ACTIVE_TAG = "active";

    public Release(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack itemStack = player.getItemInHand(hand);
        
        if (!level.isClientSide) {
            if (player.isShiftKeyDown()) {
                // Switch level regardless of active state
                ManaRelease.switchLevel(player);
            } else {
                // Toggle release effect
                if (ManaRelease.isReleaseActive(player)) {
                    ManaRelease.stopRelease(player);
                    itemStack.getOrCreateTag().putBoolean(ACTIVE_TAG, false);
                } else {
                    ManaRelease.startRelease(player);
                    itemStack.getOrCreateTag().putBoolean(ACTIVE_TAG, true);
                }
            }
            
            // Start cooldown to prevent spam
            player.getCooldowns().addCooldown(this, 20); // 1 second cooldown
        }
        
        return InteractionResultHolder.success(itemStack);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltipComponents, TooltipFlag isAdvanced) {
        super.appendHoverText(stack, level, tooltipComponents, isAdvanced);

        ManaRelease.Level currentLevel = ManaRelease.getCurrentLevel(stack);
        
        tooltipComponents.add(Component.translatable("item.nacikmod.release.desc1"));
        tooltipComponents.add(Component.translatable("item.nacikmod.release.desc2", 
                currentLevel.manaCost, currentLevel.damage)
                .withStyle(style -> style.withColor(0x00FFFF)));

        boolean isActive = stack.hasTag() && stack.getTag().getBoolean(ACTIVE_TAG);
        if (isActive) {
            tooltipComponents.add(Component.translatable("item.nacikmod.release.active")
                    .withStyle(ChatFormatting.GREEN));
        } else {
            tooltipComponents.add(Component.translatable("item.nacikmod.release.inactive")
                    .withStyle(ChatFormatting.RED));
        }
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return stack.hasTag() && stack.getTag().getBoolean(ACTIVE_TAG);
    }
}
