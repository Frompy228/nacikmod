package net.artur.nacikmod.item;

import net.minecraft.world.item.*;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;
import net.minecraft.ChatFormatting;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class KnightSword extends SwordItem {

    public KnightSword() {
        // Базовый урон чуть выше железного, но скорость атаки чуть ниже (тяжелее)
        super(Tiers.IRON, 5, -2.2f, new Item.Properties().rarity(Rarity.COMMON));
    }

    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        if (target.isBlocking()) {
            target.hurt(attacker.level().damageSources().thorns(attacker), 1.0f);
        }

        return super.hurtEnemy(stack, target, attacker);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltipComponents, TooltipFlag isAdvanced) {
        tooltipComponents.add(Component.translatable("item.nacikmod.knight_sword.desc")
                .withStyle(ChatFormatting.GRAY));
    }
}