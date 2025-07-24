package net.artur.nacikmod.item;

import net.artur.nacikmod.registry.ModAttributes;
import net.artur.nacikmod.item.ability.CursedSwordAbility;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.item.*;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class CursedSword extends SwordItem {
    public CursedSword() {
        super(new CustomTier(), 5, -2.4f, new Properties().rarity(ShardArtifact.RED));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack itemStack = player.getItemInHand(hand);
        
        if (!level.isClientSide) {
            // Проверяем перезарядку
            if (player.getCooldowns().isOnCooldown(this)) {
                float cooldownPercent = player.getCooldowns().getCooldownPercent(this, 0);
                int cooldownSeconds = (int) (cooldownPercent * 15); // 15 секунд = 100%
                player.sendSystemMessage(Component.literal("Ability on cooldown: " + cooldownSeconds + " seconds left!")
                        .withStyle(ChatFormatting.RED));
                return InteractionResultHolder.success(itemStack);
            }
            
            // Проверяем готовность способности
            if (CursedSwordAbility.isReady(player)) {
                // Активируем способность
                if (CursedSwordAbility.activateAbility(player)) {
                    // Устанавливаем перезарядку (10 секунд)
                    player.getCooldowns().addCooldown(this, 200);
                    player.sendSystemMessage(Component.literal("Cursed Sword ability activated!")
                            .withStyle(ChatFormatting.GREEN));
                }
            } else {
                // Способность уже активна
                int remainingTime = CursedSwordAbility.getActiveTime(player);
                player.sendSystemMessage(Component.literal("Ability active for " + (remainingTime / 20) + " more seconds!")
                        .withStyle(ChatFormatting.YELLOW));
            }
        }
        
        return InteractionResultHolder.success(itemStack);
    }


    private static class CustomTier implements Tier {
        @Override
        public int getUses() { return 1500; }
        @Override
        public float getSpeed() { return 2.0f; }
        @Override
        public float getAttackDamageBonus() { return 7.0f; }
        @Override
        public int getLevel() { return 4; }
        @Override
        public int getEnchantmentValue() { return 30; }
        @Override
        public net.minecraft.world.item.crafting.Ingredient getRepairIngredient() { return net.minecraft.world.item.crafting.Ingredient.EMPTY; }
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltipComponents, TooltipFlag isAdvanced) {
        super.appendHoverText(stack, level, tooltipComponents, isAdvanced);

        tooltipComponents.add(Component.translatable("item.nacikmod.cursed_sword.desc1"));
    }
}
