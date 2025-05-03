package net.artur.nacikmod.loot;

import net.artur.nacikmod.NacikMod;
import net.artur.nacikmod.registry.ModItems;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator;
import net.minecraftforge.event.LootTableLoadEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceCondition;

@Mod.EventBusSubscriber(modid = NacikMod.MOD_ID)
public class LootTableHandler {

    @SubscribeEvent
    public static void onLootTableLoad(LootTableLoadEvent event) {
        ResourceLocation tableName = event.getName();

        // Добавляем предметы во все сундуки
        if (tableName.getPath().startsWith("chests/")) {

            LootItem.Builder<?> magicCircuit = LootItem.lootTableItem(ModItems.MAGIC_CIRCUIT.get())
                    .setWeight(30)
                    .apply(SetItemCountFunction.setCount(UniformGenerator.between(1, 2)));

            LootItem.Builder<?> magicArmor = LootItem.lootTableItem(ModItems.MAGIC_ARMOR.get())
                    .setWeight(5)
                    .apply(SetItemCountFunction.setCount(UniformGenerator.between(1, 1)));

            LootItem.Builder<?> magicCharm = LootItem.lootTableItem(ModItems.MAGIC_CHARM.get())
                    .setWeight(7)
                    .apply(SetItemCountFunction.setCount(UniformGenerator.between(1, 1)));

            LootItem.Builder<?> manaCrystal = LootItem.lootTableItem(ModItems.MANA_CRYSTAL.get())
                    .setWeight(3)
                    .apply(SetItemCountFunction.setCount(UniformGenerator.between(1, 1)));

            LootItem.Builder<?> magicSeal = LootItem.lootTableItem(ModItems.MAGIC_SEAL.get())
                    .setWeight(9)
                    .apply(SetItemCountFunction.setCount(UniformGenerator.between(1, 1)));

            LootItem.Builder<?> ringOfTime = LootItem.lootTableItem(ModItems.RING_OF_TIME.get())
                    .setWeight(2)
                    .apply(SetItemCountFunction.setCount(UniformGenerator.between(1, 1)));
            LootItem.Builder<?> lastMagic = LootItem.lootTableItem(ModItems.LAST_MAGIC.get())
                    .setWeight(1)
                    .apply(SetItemCountFunction.setCount(UniformGenerator.between(1, 1)));

            // Создаём пул лута
            LootPool pool = LootPool.lootPool()
                    .name("nacikmod_loot_pool")
                    .setRolls(UniformGenerator.between(0, 2))
                    .when(LootItemRandomChanceCondition.randomChance(0.1f))
                    .add(magicCircuit)
                    .add(magicArmor)
                    .add(ringOfTime)
                    .add(magicCharm)
                    .add(magicSeal)
                    .add(manaCrystal)
                    .add(lastMagic)
                    .build();


            // Добавляем пул в таблицу лута
            event.getTable().addPool(pool);
        }
    }
}

