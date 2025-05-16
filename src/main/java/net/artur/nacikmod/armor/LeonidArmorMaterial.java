package net.artur.nacikmod.armor;

import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.ArmorItem.Type;

public class LeonidArmorMaterial implements ArmorMaterial {
    private static final int[] BASE_DURABILITY = new int[] {13, 15, 16, 11};
    private static final int[] PROTECTION_VALUES = new int[] {0, 0, 0, 7}; // [BOOTS, LEGGINGS, CHESTPLATE, HELMET]

    @Override
    public int getDurabilityForType(Type type) {
        return BASE_DURABILITY[type.getSlot().getIndex()] * 50;
    }

    @Override
    public int getDefenseForType(Type type) {
        return PROTECTION_VALUES[type.getSlot().getIndex()];
    }

    @Override
    public int getEnchantmentValue() {
        return 25;
    }

    @Override
    public SoundEvent getEquipSound() {
        return SoundEvents.ARMOR_EQUIP_DIAMOND;
    }

    @Override
    public Ingredient getRepairIngredient() {
        return Ingredient.EMPTY;
    }

    @Override
    public String getName() {
        return "leonid";
    }

    @Override
    public float getToughness() {
        return 5.0F;
    }

    @Override
    public float getKnockbackResistance() {
        return 0.1F;
    }
} 