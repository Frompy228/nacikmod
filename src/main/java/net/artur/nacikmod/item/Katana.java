package net.artur.nacikmod.item;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import net.minecraftforge.common.ForgeMod;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.item.Item;

import java.util.UUID;

public class Katana extends SwordItem {
    private static final UUID REACH_MODIFIER_ID = UUID.fromString("b1b9fce5-5c52-4c4a-9d8e-56b4c634a9e2");
    private final Multimap<Attribute, AttributeModifier> attributeModifiers;

    public Katana() {
        super(Tiers.IRON, 5 - 4, -2.3f, new Item.Properties().stacksTo(1).durability(250));

        // создаём кастомные атрибуты
        ImmutableMultimap.Builder<Attribute, AttributeModifier> builder = ImmutableMultimap.builder();

        // +5 урона
        builder.put(Attributes.ATTACK_DAMAGE,
                new AttributeModifier(BASE_ATTACK_DAMAGE_UUID, "Weapon modifier", 4.0, AttributeModifier.Operation.ADDITION));

        // 1.7 скорости => эквивалент -2.3f
        builder.put(Attributes.ATTACK_SPEED,
                new AttributeModifier(BASE_ATTACK_SPEED_UUID, "Weapon modifier", -2.3f, AttributeModifier.Operation.ADDITION));

        // +0.7 блока дальности атаки
        builder.put(ForgeMod.ENTITY_REACH.get(),
                new AttributeModifier(REACH_MODIFIER_ID, "Weapon reach", 0.15, AttributeModifier.Operation.ADDITION));

        this.attributeModifiers = builder.build();
    }

    @Override
    public Multimap<Attribute, AttributeModifier> getDefaultAttributeModifiers(EquipmentSlot slot) {
        if (slot == EquipmentSlot.MAINHAND) {
            return this.attributeModifiers;
        }
        return super.getDefaultAttributeModifiers(slot);
    }
}
