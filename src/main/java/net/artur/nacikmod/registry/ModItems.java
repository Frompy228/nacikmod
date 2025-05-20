package net.artur.nacikmod.registry;

import net.artur.nacikmod.armor.LeonidArmorMaterial;
import net.artur.nacikmod.item.LeonidHelmet;
import net.artur.nacikmod.item.*;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Tiers;
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
    public static final RegistryObject<Item> HIRAISHIN = ITEMS.register("hiraishin", Hiraishin::new);
    public static final RegistryObject<Item> HIRAISHIN_WITHOUT_SEAL = ITEMS.register("hiraishin_without_seal", HiraishinWithoutSeal::new);
    public static final RegistryObject<Item> SPEAR = ITEMS.register("spear", Spear::new);
    public static final RegistryObject<Item> MAGIC_ARMOR = ITEMS.register("magic_armor", MagicArmor::new);
    public static final RegistryObject<Item> MAGIC_CHARM = ITEMS.register("magic_charm", MagicCharm::new);
    public static final RegistryObject<Item> MANA_CRYSTAL = ITEMS.register("mana_crystal", MagicCrystal::new);
    public static final RegistryObject<Item> MAGIC_SEAL = ITEMS.register("magic_seal", () -> new MagicSeal(new Item.Properties()));
    public static final RegistryObject<Item> SHARD_OF_ARTIFACT = ITEMS.register("shard_artifact", () -> new ShardArtifact(new Item.Properties()));
    public static final RegistryObject<Item> RING_OF_TIME = ITEMS.register("ring_of_time", () -> new RingOfTime(new Item.Properties()));
    public static final RegistryObject<Item> FIRE_STAFF = ITEMS.register("fire_staff", () -> new FireStaff(new Item.Properties()));
    public static final RegistryObject<Item> MAGIC_HEALING = ITEMS.register("magic_healing", () -> new MagicHealing(new Item.Properties()));
    public static final RegistryObject<Item> RELEASE = ITEMS.register("release", () -> new Release(new Item.Properties()));
    public static final RegistryObject<Item> LAST_MAGIC = ITEMS.register("last_magic", () -> new LastMagic(new Item.Properties()));
    public static final RegistryObject<Item> MANA_SWORD = ITEMS.register("mana_sword", () -> new ManaSword(Tiers.WOOD, new Item.Properties()));
    public static final RegistryObject<Item> MAGIC_WEAPONS = ITEMS.register("magic_weapons", () -> new MagicWeapons(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> BLOOD_SHOOT = ITEMS.register("blood_shoot", () -> new BloodShoot(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> SHINRA_TENSEI = ITEMS.register("shinra_tensei", () -> new ShinraTensei(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> GRAVITY = ITEMS.register("gravity", () -> new Gravity(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> LEONID_HELMET = ITEMS.register("leonid_helmet", () -> new LeonidHelmet(new LeonidArmorMaterial(), new Item.Properties()));
    public static final RegistryObject<Item> LEONID_SHIELD = ITEMS.register("leonid_shield", () -> new LeonidShileld(new Item.Properties()));
    public static final RegistryObject<Item> LANSER_SPAWN_EGG = ITEMS.register("lanser_spawn_egg",
            ()-> new ForgeSpawnEggItem(ModEntities.LANSER, 0x5b107e, 0xCF6A84,
                    new Item.Properties()));
    public static final RegistryObject<Item> LEONID_SPAWN_EGG = ITEMS.register("leonid_spawn_egg",
            ()-> new ForgeSpawnEggItem(ModEntities.LEONID, 0xf2bf13, 0xda0000,
                    new Item.Properties()));
    public static final RegistryObject<Item> SPARTAN_SPAWN_EGG = ITEMS.register("spartan_spawn_egg",
            ()-> new ForgeSpawnEggItem(ModEntities.SPARTAN, 0xf2bf13, 0xda0000,
                    new Item.Properties()));
}
