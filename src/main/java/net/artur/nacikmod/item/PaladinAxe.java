package net.artur.nacikmod.item;

import net.minecraft.world.item.*;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;
import net.minecraft.ChatFormatting;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class PaladinAxe extends AxeItem {

    public PaladinAxe() {
        // Используем железный тир, урон чуть выше среднего, скорость атаки низкая
        super(Tiers.IRON, 12.0f, -3.2f, new Item.Properties().rarity(Rarity.UNCOMMON));
    }

    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        // Логика "Святого правосудия"
        if (!attacker.level().isClientSide) {
            if (target.getActiveEffects().stream().anyMatch(e -> !e.getEffect().isBeneficial())) {
                target.hurt(attacker.level().damageSources().magic(), 4.0f);
                target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 40, 2));
            }
        }
        return super.hurtEnemy(stack, target, attacker);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltipComponents, TooltipFlag isAdvanced) {
        tooltipComponents.add(Component.translatable("item.nacikmod.paladin_axe.desc")
                .withStyle(ChatFormatting.GOLD));
        tooltipComponents.add(Component.translatable("item.nacikmod.paladin_axe.ability")
                .withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
    }
}