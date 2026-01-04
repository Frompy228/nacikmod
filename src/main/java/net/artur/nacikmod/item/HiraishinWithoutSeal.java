package net.artur.nacikmod.item;

import net.artur.nacikmod.registry.ModItems;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.item.crafting.Ingredient;

public class HiraishinWithoutSeal extends SwordItem {
    public HiraishinWithoutSeal() {
        super(new HiraishinWithoutSeal.CustomTier(), 3, -2F, new Item.Properties());
    }

    private static class CustomTier implements Tier {
        @Override
        public int getUses() {
            return 2000; // Прочность меча
        }

        @Override
        public float getSpeed() {
            return 1.5f; // Скорость атаки
        }

        @Override
        public float getAttackDamageBonus() {
            return 8.0f; // Базовый урон
        }

        @Override
        public int getLevel() {
            return 4; // Уровень прочности (как у алмаза)
        }

        @Override
        public int getEnchantmentValue() {
            return 25; // Насколько хорошо зачаровывается
        }

        @Override
        public Ingredient getRepairIngredient() {
            return Ingredient.of(ModItems.SHARD_OF_ARTIFACT.get()); // Ремонт артефактом
        }
    }
}
