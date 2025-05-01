package net.artur.nacikmod.item;

import net.minecraft.world.entity.HumanoidArm;
import net.artur.nacikmod.registry.ModEffects;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.*;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.common.ForgeMod;
import com.google.common.collect.Multimap;
import com.google.common.collect.ImmutableMultimap;
import java.util.UUID;

public class LansOfProtectionItem extends SwordItem {
    private static final UUID ATTACK_RANGE_MODIFIER_ID = UUID.fromString("9f2b5c42-38ff-4eb1-a79c-2c2b59a2efdf");
    private final Multimap<Attribute, AttributeModifier> attributeModifiers;

    public LansOfProtectionItem() {
        super(new CustomTier(), 7, -3f, new Item.Properties().fireResistant());

        // Создаём атрибуты (увеличиваем дальность атаки)
        ImmutableMultimap.Builder<Attribute, AttributeModifier> builder = ImmutableMultimap.builder();
        builder.put(ForgeMod.ENTITY_REACH.get(),
                new AttributeModifier(ATTACK_RANGE_MODIFIER_ID, "Attack range", 1.0, AttributeModifier.Operation.ADDITION));
        builder.put(Attributes.ATTACK_DAMAGE,
                new AttributeModifier(BASE_ATTACK_DAMAGE_UUID, "Weapon damage", 24.0, AttributeModifier.Operation.ADDITION));
        builder.put(Attributes.ATTACK_SPEED,
                new AttributeModifier(BASE_ATTACK_SPEED_UUID, "Weapon speed", -3.0, AttributeModifier.Operation.ADDITION));

        this.attributeModifiers = builder.build();
    }

    @Override
    public Multimap<Attribute, AttributeModifier> getDefaultAttributeModifiers(EquipmentSlot slot) {
        if (slot == EquipmentSlot.MAINHAND) {
            return attributeModifiers;
        }
        return super.getDefaultAttributeModifiers(slot);
    }

    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        if (!target.level().isClientSide) {
            boolean success = super.hurtEnemy(stack, target, attacker);
            if (!success) return false;

            double baseDamage = attacker.getAttributeValue(Attributes.ATTACK_DAMAGE) + 1;
            double armor = target.getArmorValue();
            double bonusDamage = baseDamage * (armor / 45.0);
            double totalDamage = baseDamage + bonusDamage;
            target.hurt(attacker.damageSources().playerAttack((Player) attacker), (float) totalDamage);

            // Проверяем, есть ли уже эффект снижения брони
            MobEffectInstance existingEffect = target.getEffect(ModEffects.ARMOR_REDUCTION.get());
            int newAmplifier = (int) Math.floor(totalDamage / 9);
            if (existingEffect != null) {
                newAmplifier += existingEffect.getAmplifier();
            }
            target.addEffect(new MobEffectInstance(ModEffects.ARMOR_REDUCTION.get(), 240, newAmplifier));

            if (attacker instanceof Player player) {
                ItemStack mainHandItem = player.getMainHandItem();
                ItemStack offHandItem = player.getOffhandItem();
                boolean isMainHand = mainHandItem == stack;
                boolean isOffHand = offHandItem == stack;

                // Проверяем, есть ли в другой руке LansOfNacii
                boolean hasLansOfNacii = (isMainHand && offHandItem.getItem() instanceof LansOfNaciiItem) ||
                        (isOffHand && mainHandItem.getItem() instanceof LansOfNaciiItem);

                if (hasLansOfNacii) {
                    // Выполняем атаку и анимацию удара
                    InteractionHand attackHand = isMainHand ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND;
                    if (attacker instanceof ServerPlayer serverPlayer) {
                        serverPlayer.swing(attackHand, true);
                    }

                    // Меняем местами оружие в руках
                    player.setItemInHand(InteractionHand.MAIN_HAND, offHandItem);
                    player.setItemInHand(InteractionHand.OFF_HAND, mainHandItem);

                    // Меняем основную руку
                    player.setMainArm(player.getMainArm() == HumanoidArm.RIGHT ? HumanoidArm.LEFT : HumanoidArm.RIGHT);
                }
            }
        }
        return true;
    }

    private static class CustomTier implements Tier {
        @Override
        public int getUses() { return 2500; }
        @Override
        public float getSpeed() { return 2.0f; }
        @Override
        public float getAttackDamageBonus() { return 17.0f; }
        @Override
        public int getLevel() { return 5; }
        @Override
        public int getEnchantmentValue() { return 25; }
        @Override
        public Ingredient getRepairIngredient() {
            return Ingredient.of(Items.NETHERITE_INGOT); // Ремонт незеритом
        }
    }
}
