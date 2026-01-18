package net.artur.nacikmod.item;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import net.minecraft.world.entity.player.Player;
import net.artur.nacikmod.capability.mana.ManaProvider;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import com.google.common.collect.Multimap;
import com.google.common.collect.ImmutableMultimap;
import net.minecraft.world.entity.EquipmentSlot;

import java.util.List;
import java.util.UUID;

public class ManaSword extends SwordItem {
    private static final int MAX_DURABILITY = 2;
    private static final float BASE_DAMAGE = 1f;
    private static final float ATTACK_SPEED = -2.4f;
    private static final String DAMAGE_TAG = "ManaSwordDamage";

    public ManaSword(Tier tier, Properties properties) {
        super(tier, (int)BASE_DAMAGE, ATTACK_SPEED, properties.durability(MAX_DURABILITY));
    }

    @Override
    public boolean isDamageable(ItemStack stack) {
        return true;
    }

    @Override
    public int getMaxDamage(ItemStack stack) {
        return MAX_DURABILITY;
    }

    @Override
    public Multimap<Attribute, AttributeModifier> getAttributeModifiers(EquipmentSlot slot, ItemStack stack) {
        if (slot == EquipmentSlot.MAINHAND) {
            ImmutableMultimap.Builder<Attribute, AttributeModifier> builder = ImmutableMultimap.builder();
            
            // Получаем урон из NBT
            float damage = stack.hasTag() && stack.getTag().contains(DAMAGE_TAG) ? 
                stack.getTag().getFloat(DAMAGE_TAG) : BASE_DAMAGE;
            
            // Устанавливаем урон меча
            builder.put(Attributes.ATTACK_DAMAGE, 
                new AttributeModifier(BASE_ATTACK_DAMAGE_UUID, "Weapon damage", damage, AttributeModifier.Operation.ADDITION));
            
            // Базовая скорость атаки
            builder.put(Attributes.ATTACK_SPEED, 
                new AttributeModifier(BASE_ATTACK_SPEED_UUID, "Weapon speed", ATTACK_SPEED, AttributeModifier.Operation.ADDITION));
            
            return builder.build();
        }
        return super.getAttributeModifiers(slot, stack);
    }

    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        if (attacker instanceof Player player) {
            // Decrease durability
            stack.hurtAndBreak(1, attacker, (entity) -> {});
            return true;
        }
        return false;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltipComponents, TooltipFlag isAdvanced) {
        super.appendHoverText(stack, level, tooltipComponents, isAdvanced);

        tooltipComponents.add(Component.translatable("item.nacikmod.mana_sword.desc1"));
    }

    @Override
    public void onCraftedBy(ItemStack stack, Level level, Player player) {
        super.onCraftedBy(stack, level, player);
        // Set full durability
        stack.setDamageValue(0);
    }

    @Override
    public boolean canApplyAtEnchantingTable(ItemStack stack, Enchantment enchantment) {
        // Only allow Smite enchantment which is added on creation
        return enchantment == Enchantments.SMITE;
    }

    @Override
    public boolean isEnchantable(ItemStack stack) {
        return false; // Disable enchanting through books
    }

    @Override
    public boolean isRepairable(ItemStack stack) {
        return false; // Disable repair
    }

    @Override
    public boolean isValidRepairItem(ItemStack toRepair, ItemStack repair) {
        return false; // Disable repair through anvil
    }

    @Override
    public boolean canBeDepleted() {
        return true; // Allow durability decrease
    }

    @Override
    public boolean isBookEnchantable(ItemStack stack, ItemStack book) {
        return false; // Disable enchanting through books
    }
}
