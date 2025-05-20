package net.artur.nacikmod.item;

import net.artur.nacikmod.registry.ModItems;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraftforge.common.ForgeMod;
import com.google.common.collect.Multimap;
import com.google.common.collect.ImmutableMultimap;
import java.util.UUID;
import net.minecraft.world.item.crafting.Ingredient;

public class Spear extends SwordItem {
    private static final UUID ENTITY_REACH_MODIFIER_ID = UUID.fromString("9f2b5c42-38ff-4eb1-a79c-2c2b59a2efdf");
    private final Multimap<Attribute, AttributeModifier> attributeModifiers;

    public Spear() {
        super(Tiers.IRON, 6, -2.0f, new Item.Properties().fireResistant());

        // Create attribute modifiers for entity reach, damage and attack speed
        ImmutableMultimap.Builder<Attribute, AttributeModifier> builder = ImmutableMultimap.builder();
        builder.put(ForgeMod.ENTITY_REACH.get(),
                new AttributeModifier(ENTITY_REACH_MODIFIER_ID, "Entity reach", 0.75, AttributeModifier.Operation.ADDITION));
        builder.put(Attributes.ATTACK_DAMAGE,
                new AttributeModifier(BASE_ATTACK_DAMAGE_UUID, "Weapon damage", 5.0, AttributeModifier.Operation.ADDITION));
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

    private static class CustomTier implements Tier {
        @Override
        public int getUses() {
            return 2500;
        }

        @Override
        public float getSpeed() {
            return 2.0f;
        }

        @Override
        public float getAttackDamageBonus() {
            return 6.0f;
        }

        @Override
        public int getLevel() {
            return 3;
        }

        @Override
        public int getEnchantmentValue() {
            return 15;
        }

        @Override
        public Ingredient getRepairIngredient() {
            return Ingredient.of(ModItems.SHARD_OF_ARTIFACT.get());
        }
    }
}

