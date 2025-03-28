package net.artur.nacikmod.registry;

import net.artur.nacikmod.item.*;
import net.minecraft.world.item.Item;
import net.minecraftforge.common.ForgeSpawnEggItem;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.eventbus.api.IEventBus;
import net.artur.nacikmod.NacikMod; // Убедись, что это имя твоего основного класса мода!

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class ModItems {
    // Создаём DeferredRegister для предметов
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, NacikMod.MOD_ID);
    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
    // Регистрируем меч
    public static final RegistryObject<Item> LANS_OF_NACII = ITEMS.register("lans_of_nacii", LansOfNaciiItem::new);
    public static final RegistryObject<Item> MAGIC_CIRCUIT = ITEMS.register("magic_circuit", () -> new MagicCircuit(new Item.Properties()));
    public static final RegistryObject<Item> LANS_OF_PROTECTION = ITEMS.register("lans_of_protection", LansOfProtectionItem::new);
    public static final RegistryObject<Item> MAGIC_ARMOR = ITEMS.register("magic_armor", MagicArmor::new);
    public static final RegistryObject<Item> RING_OF_TIME = ITEMS.register("ring_of_time", () -> new RingOfTime(new Item.Properties()));
    public static final RegistryObject<Item> LANSER_SPAWN_EGG = ITEMS.register("lanser_spawn_egg",
            ()-> new ForgeSpawnEggItem(ModEntities.LANSER, 0x5b107e, 0xCF6A84,
                    new Item.Properties()));

}
