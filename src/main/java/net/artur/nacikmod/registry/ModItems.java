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
    public static final RegistryObject<Item> DUBINKA = ITEMS.register("dubinka", () -> new Dubinka());
    public static final RegistryObject<Item> CURSED_SWORD = ITEMS.register("cursed_sword", () -> new CursedSword());
    public static final RegistryObject<Item> MAGIC_BOW = ITEMS.register("magic_bow", () -> new MagicBow());
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
    public static final RegistryObject<Item> FIRE_FLOWER = ITEMS.register("fire_flower", () -> new FireFlower(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> ICE_PRISON = ITEMS.register("ice_prison", () -> new IcePrison(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> BLOOD_SHOOT = ITEMS.register("blood_shoot", () -> new BloodShoot(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> SHINRA_TENSEI = ITEMS.register("shinra_tensei", () -> new ShinraTensei(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> GRAVITY = ITEMS.register("gravity", () -> new Gravity(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> SENSORY_RAIN = ITEMS.register("sensory_rain", () -> new SensoryRain(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> LEONID_HELMET = ITEMS.register("leonid_helmet", () -> new LeonidHelmet(new LeonidArmorMaterial(), new Item.Properties()));
    public static final RegistryObject<Item> LEONID_SHIELD = ITEMS.register("leonid_shield", () -> new LeonidShileld(new Item.Properties()));
    public static final RegistryObject<Item> MANA_BLESSING = ITEMS.register("mana_blessing", () -> new ManaBlessing(new Item.Properties()));
    public static final RegistryObject<Item> POCKET = ITEMS.register("pocket", () -> new Pocket(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> BREAKING_BODY_LIMIT = ITEMS.register("breaking_body_limit", () -> new BreakingBodyLimit(new Item.Properties()));
    public static final RegistryObject<Item> ABSOLUTE_VISION = ITEMS.register("absolute_vision", () -> new AbsoluteVision(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> INTANGIBILITY = ITEMS.register("intangibility", () -> new Intangibility(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> DARK_SPHERE = ITEMS.register("dark_sphere", () -> new DarkSphere(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> EARTH_STEP = ITEMS.register("earth_step", () -> new EarthStep(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> HUNDRED_SEAL = ITEMS.register("hundred_seal", () -> new HundredSeal(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> POISONED_SCYTHE = ITEMS.register("poisoned_scythe", () -> new PoisonedScythe());
    public static final RegistryObject<Item> GOD_HAND = ITEMS.register("god_hand", () -> new GodHand(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> SLASH = ITEMS.register("slash", () -> new Slash(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> DOUBLE_SLASH = ITEMS.register("double_slash", () -> new DoubleSlash(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> WORLD_SLASH = ITEMS.register("world_slash", () -> new WorldSlash(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> ANCIENT_SCROLL = ITEMS.register("ancient_scroll", () -> new AncientScroll(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> BLOOD_WARRIOR = ITEMS.register("blood_warrior", () -> new BloodWarrior(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> ANCIENT_SEAL = ITEMS.register("ancient_seal", () -> new AncientSeal(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> LANSER_SPAWN_EGG = ITEMS.register("lanser_spawn_egg",
            ()-> new ForgeSpawnEggItem(ModEntities.LANSER, 0x5b107e, 0xCF6A84,
                    new Item.Properties()));
    public static final RegistryObject<Item> LEONID_SPAWN_EGG = ITEMS.register("leonid_spawn_egg",
            ()-> new ForgeSpawnEggItem(ModEntities.LEONID, 0xf2bf13, 0xda0000,
                    new Item.Properties()));
    public static final RegistryObject<Item> SPARTAN_SPAWN_EGG = ITEMS.register("spartan_spawn_egg",
            ()-> new ForgeSpawnEggItem(ModEntities.SPARTAN, 0xf2bf13, 0xda0000,
                    new Item.Properties()));
    public static final RegistryObject<Item> BERSERKER_SPAWN_EGG = ITEMS.register("berserker_spawn_egg",
            ()-> new ForgeSpawnEggItem(ModEntities.BERSERK, 0x897765, 0x474d5d,
                    new Item.Properties()));
    public static final RegistryObject<Item> ARCHER_SPAWN_EGG = ITEMS.register("archer_spawn_egg",
            ()-> new ForgeSpawnEggItem(ModEntities.ARCHER, 0xc5cacc, 0xa1524c,
                    new Item.Properties()));
    public static final RegistryObject<Item> MYSTERIOUS_TRADER_SPAWN_EGG = ITEMS.register("mysterious_trader_spawn_egg",
            ()-> new ForgeSpawnEggItem(ModEntities.MYSTERIOUS_TRADER, 0x393939, 0x121212,
                    new Item.Properties()));
    public static final RegistryObject<Item> MYSTERIOUS_TRADER_BATTLE_CLONE_SPAWN_EGG = ITEMS.register("mysterious_trader_battle_clone_spawn_egg",
            ()-> new ForgeSpawnEggItem(ModEntities.MYSTERIOUS_TRADER_BATTLE_CLONE, 0x393939, 0x121212,
                    new Item.Properties()));
    public static final RegistryObject<Item> BLOOD_WARRIOR_SPAWN_EGG = ITEMS.register("blood_warrior_spawn_egg",
            ()-> new ForgeSpawnEggItem(ModEntities.BLOOD_WARRIOR, 0x8B0000, 0xFF0000,
                    new Item.Properties()));
}
