package net.artur.nacikmod.datagen;

import net.artur.nacikmod.NacikMod;
import net.artur.nacikmod.registry.ModBlocks;
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
                    .setWeight(140)
                    .apply(SetItemCountFunction.setCount(UniformGenerator.between(1, 2)));

            LootItem.Builder<?> magicArmor = LootItem.lootTableItem(ModItems.MAGIC_ARMOR.get())
                    .setWeight(8)
                    .apply(SetItemCountFunction.setCount(UniformGenerator.between(1, 1)));

            LootItem.Builder<?> magicCharm = LootItem.lootTableItem(ModItems.MAGIC_CHARM.get())
                    .setWeight(10)
                    .apply(SetItemCountFunction.setCount(UniformGenerator.between(1, 1)));

            LootItem.Builder<?> manaCrystal = LootItem.lootTableItem(ModItems.MANA_CRYSTAL.get())
                    .setWeight(6)
                    .apply(SetItemCountFunction.setCount(UniformGenerator.between(1, 1)));

            LootItem.Builder<?> magicSeal = LootItem.lootTableItem(ModItems.MAGIC_SEAL.get())
                    .setWeight(13)
                    .apply(SetItemCountFunction.setCount(UniformGenerator.between(1, 1)));

            LootItem.Builder<?> ringOfTime = LootItem.lootTableItem(ModItems.RING_OF_TIME.get())
                    .setWeight(1)
                    .apply(SetItemCountFunction.setCount(UniformGenerator.between(1, 1)));
            
            LootItem.Builder<?> lastMagic = LootItem.lootTableItem(ModItems.LAST_MAGIC.get())
                    .setWeight(1)
                    .apply(SetItemCountFunction.setCount(UniformGenerator.between(1, 1)));

            LootItem.Builder<?> bloodShoot = LootItem.lootTableItem(ModItems.BLOOD_SHOOT.get())
                    .setWeight(7)
                    .apply(SetItemCountFunction.setCount(UniformGenerator.between(1, 1)));

            LootItem.Builder<?> bloodWarrior = LootItem.lootTableItem(ModItems.BLOOD_WARRIOR.get())
                    .setWeight(7)
                    .apply(SetItemCountFunction.setCount(UniformGenerator.between(1, 1)));

            LootItem.Builder<?> hiraishinWithoutSeal = LootItem.lootTableItem(ModItems.HIRAISHIN_WITHOUT_SEAL.get())
                    .setWeight(2)
                    .apply(SetItemCountFunction.setCount(UniformGenerator.between(1, 1)));

            LootItem.Builder<?> gravity = LootItem.lootTableItem(ModItems.GRAVITY.get())
                    .setWeight(1)
                    .apply(SetItemCountFunction.setCount(UniformGenerator.between(1, 1)));

            LootItem.Builder<?> darkSphere = LootItem.lootTableItem(ModItems.DARK_SPHERE.get())
                    .setWeight(2)
                    .apply(SetItemCountFunction.setCount(UniformGenerator.between(1, 1)));

            LootItem.Builder<?> sensoryRain = LootItem.lootTableItem(ModItems.SENSORY_RAIN.get())
                    .setWeight(1)
                    .apply(SetItemCountFunction.setCount(UniformGenerator.between(1, 1)));

            LootItem.Builder<?> pocket = LootItem.lootTableItem(ModItems.POCKET.get())
                    .setWeight(1)
                    .apply(SetItemCountFunction.setCount(UniformGenerator.between(1, 1)));

            LootItem.Builder<?> absoluteVision = LootItem.lootTableItem(ModItems.ABSOLUTE_VISION.get())
                    .setWeight(1)
                    .apply(SetItemCountFunction.setCount(UniformGenerator.between(1, 1)));

            LootItem.Builder<?> breakingBodyLimit = LootItem.lootTableItem(ModItems.BREAKING_BODY_LIMIT.get())
                    .setWeight(1)
                    .apply(SetItemCountFunction.setCount(UniformGenerator.between(1, 1)));

            LootItem.Builder<?> intangibility = LootItem.lootTableItem(ModItems.INTANGIBILITY.get())
                    .setWeight(1)
                    .apply(SetItemCountFunction.setCount(UniformGenerator.between(1, 1)));

            LootItem.Builder<?> sealOfReturn = LootItem.lootTableItem(ModItems.SEAL_OF_RETURN.get())
                    .setWeight(5)
                    .apply(SetItemCountFunction.setCount(UniformGenerator.between(1, 1)));

            LootItem.Builder<?> fireTrap = LootItem.lootTableItem(ModBlocks.FIRE_TRAP.get())
                    .setWeight(6)
                    .apply(SetItemCountFunction.setCount(UniformGenerator.between(1, 1)));

            LootItem.Builder<?> magicHealing = LootItem.lootTableItem(ModItems.MAGIC_HEALING.get())
                    .setWeight(2)
                    .apply(SetItemCountFunction.setCount(UniformGenerator.between(1, 1)));

            LootItem.Builder<?> release = LootItem.lootTableItem(ModItems.RELEASE.get())
                    .setWeight(2)
                    .apply(SetItemCountFunction.setCount(UniformGenerator.between(1, 1)));

            LootItem.Builder<?> simpleDomain = LootItem.lootTableItem(ModItems.SIMPLE_DOMAIN.get())
                    .setWeight(2)
                    .apply(SetItemCountFunction.setCount(UniformGenerator.between(1, 1)));

            LootItem.Builder<?> barrierSeal = LootItem.lootTableItem(ModItems.BARRIER_SEAL.get())
                    .setWeight(2)
                    .apply(SetItemCountFunction.setCount(UniformGenerator.between(1, 1)));

            LootItem.Builder<?> barrierWall = LootItem.lootTableItem(ModItems.BARRIER_WALL.get())
                    .setWeight(2)
                    .apply(SetItemCountFunction.setCount(UniformGenerator.between(1, 1)));


            // Создаём пул лута
            LootPool pool = LootPool.lootPool()
                    .name("nacikmod_loot_pool")
                    .setRolls(UniformGenerator.between(1, 2))
                    .when(LootItemRandomChanceCondition.randomChance(0.09f))
                    .add(magicCircuit)
                    .add(magicArmor)
                    .add(ringOfTime)
                    .add(magicCharm)
                    .add(magicSeal)
                    .add(manaCrystal)
                    .add(lastMagic)
                    .add(bloodShoot)
                    .add(hiraishinWithoutSeal)
                    .add(gravity)
                    .add(darkSphere)
                    .add(sensoryRain)
                    .add(pocket)
                    .add(absoluteVision)
                    .add(breakingBodyLimit)
                    .add(intangibility)
                    .add(bloodWarrior)
                    .add(sealOfReturn)
                    .add(fireTrap)
                    .add(magicHealing)
                    .add(release)
                    .add(simpleDomain)
                    .add(barrierSeal)
                    .add(barrierWall)
                    .build();


            // Добавляем пул в таблицу лута
            event.getTable().addPool(pool);
        }
    }
}

