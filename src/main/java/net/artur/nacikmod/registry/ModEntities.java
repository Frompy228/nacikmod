package net.artur.nacikmod.registry;

import net.artur.nacikmod.entity.custom.*;
import net.artur.nacikmod.entity.projectiles.*;
import net.artur.nacikmod.entity.projectiles.ShamakEntity;
import net.artur.nacikmod.NacikMod;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModEntities {
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES =
            DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, NacikMod.MOD_ID);

    public static final RegistryObject<EntityType<LanserEntity>> LANSER =
            ENTITY_TYPES.register("lanser", () ->
                    EntityType.Builder.of(LanserEntity::new, MobCategory.MONSTER)
                            .sized(0.6f, 1.8f)
                            .build("lanser")
            );

    public static final RegistryObject<EntityType<LeonidEntity>> LEONID =
            ENTITY_TYPES.register("leonid", () ->
                    EntityType.Builder.of(LeonidEntity::new, MobCategory.MONSTER)
                            .sized(0.6f, 1.8f)
                            .build("leonid")
            );

    public static final RegistryObject<EntityType<SpartanEntity>> SPARTAN =
            ENTITY_TYPES.register("spartan", () ->
                    EntityType.Builder.of(SpartanEntity::new, MobCategory.MONSTER)
                            .sized(0.6f, 1.8f)
                            .build("spartan")
            );

    public static final RegistryObject<EntityType<BerserkerEntity>> BERSERK =
            ENTITY_TYPES.register("berserker", () ->
                    EntityType.Builder.of(BerserkerEntity::new, MobCategory.MONSTER)
                            .sized(1.2f, 3.6f)
                            .build("berserker")
            );

    public static final RegistryObject<EntityType<FireArrowEntity>> FIRE_ARROW =
            ENTITY_TYPES.register("fire_arrow", () ->
                    EntityType.Builder.<FireArrowEntity>of(FireArrowEntity::new, MobCategory.MISC)
                            .sized(0.25F, 0.25F)
                            .clientTrackingRange(16)
                            .updateInterval(2)
                            .build("fire_arrow")
            );

    public static final RegistryObject<EntityType<ManaSwordProjectile>> MANA_SWORD_PROJECTILE =
            ENTITY_TYPES.register("mana_sword_projectile",
                    () -> EntityType.Builder.<ManaSwordProjectile>of((type, level) -> new ManaSwordProjectile(type, level), MobCategory.MISC)
                            .sized(0.8F, 0.7F)
                            .clientTrackingRange(8)
                            .updateInterval(20)
                            .build("mana_sword_projectile"));

    public static final RegistryObject<EntityType<BloodShootProjectile>> BLOOD_SHOOT_PROJECTILE =
            ENTITY_TYPES.register("blood_shoot_projectile",
                    () -> EntityType.Builder.<BloodShootProjectile>of((type, level) -> new BloodShootProjectile(type, level), MobCategory.MISC)
                            .sized(0.8F, 0.7F)
                            .clientTrackingRange(8)
                            .updateInterval(20)
                            .build("blood_shoot_projectile"));

    public static final RegistryObject<EntityType<ManaArrowProjectile>> MANA_ARROW =
            ENTITY_TYPES.register("mana_arrow",
                    () -> EntityType.Builder.<ManaArrowProjectile>of((type, level) -> new ManaArrowProjectile(type, level), MobCategory.MISC)
                            .sized(0.5F, 0.5F)
                            .clientTrackingRange(8)
                            .updateInterval(20)
                            .build("mana_arrow"));

    public static final RegistryObject<EntityType<FireCloudEntity>> FIRE_CLOUD =
            ENTITY_TYPES.register("fire_cloud",
                    () -> EntityType.Builder.<FireCloudEntity>of((type, level) -> new FireCloudEntity(type, level), MobCategory.MISC)
                            .sized(4.0F, 2.0F) // Уменьшенный размер хитбокса (было 6.0F, 3.0F)
                            .clientTrackingRange(16)
                            .updateInterval(2)
                            .build("fire_cloud"));

    public static final RegistryObject<EntityType<IceSpikeProjectile>> ICE_SPIKE_PROJECTILE =
            ENTITY_TYPES.register("ice_spike_projectile",
                    () -> EntityType.Builder.<IceSpikeProjectile>of((type, level) -> new IceSpikeProjectile(type, level), MobCategory.MISC)
                            .sized(1F, 1F)
                            .clientTrackingRange(8)
                            .updateInterval(20)
                            .build("ice_spike_projectile"));

    public static final RegistryObject<EntityType<ArcherEntity>> ARCHER =
            ENTITY_TYPES.register("archer", () ->
                    EntityType.Builder.of(ArcherEntity::new, MobCategory.MONSTER)
                            .sized(0.6f, 1.8f)
                            .build("archer")
            );

    public static final RegistryObject<EntityType<MysteriousTraderEntity>> MYSTERIOUS_TRADER =
            ENTITY_TYPES.register("mysterious_trader", () ->
                    EntityType.Builder.of(MysteriousTraderEntity::new, MobCategory.CREATURE)
                            .sized(0.6f, 1.8f)
                            .build("mysterious_trader")
            );

    public static final RegistryObject<EntityType<MysteriousTraderBattleCloneEntity>> MYSTERIOUS_TRADER_BATTLE_CLONE =
            ENTITY_TYPES.register("mysterious_trader_battle_clone", () ->
                    EntityType.Builder.of(MysteriousTraderBattleCloneEntity::new, MobCategory.MONSTER)
                            .sized(0.6f, 1.8f)
                            .build("mysterious_trader_battle_clone")
            );

    public static final RegistryObject<EntityType<BloodWarriorEntity>> BLOOD_WARRIOR =
            ENTITY_TYPES.register("blood_warrior", () ->
                    EntityType.Builder.of(BloodWarriorEntity::new, MobCategory.MONSTER)
                            .sized(0.6f, 1.8f)
                            .build("blood_warrior")
            );

    public static final RegistryObject<EntityType<SlashProjectile>> SLASH_PROJECTILE =
            ENTITY_TYPES.register("slash_projectile",
                    () -> EntityType.Builder.<SlashProjectile>of((type, level) -> new SlashProjectile(type, level), MobCategory.MISC)
                            .sized(3.0F, 0.2F)
                            .clientTrackingRange(8)
                            .updateInterval(20)
                            .build("slash_projectile"));

    public static final RegistryObject<EntityType<DoubleSlashProjectile>> DOUBLE_SLASH_PROJECTILE =
            ENTITY_TYPES.register("double_slash_projectile",
                    () -> EntityType.Builder.<DoubleSlashProjectile>of((type, level) -> new DoubleSlashProjectile(type, level), MobCategory.MISC)
                            .sized(3.0F, 4F)
                            .clientTrackingRange(8)
                            .updateInterval(20)
                            .build("double_slash_projectile"));

    public static final RegistryObject<EntityType<SuppressingGate>> SUPPRESSING_GATE =
            ENTITY_TYPES.register("suppressing_gate",
                    () -> EntityType.Builder.<SuppressingGate>of((type, level) -> new SuppressingGate(type, level), MobCategory.MISC)
                            .sized(4.0F, 4.0F)
                            .clientTrackingRange(16)
                            .updateInterval(2)
                            .build("suppressing_gate"));

    public static final RegistryObject<EntityType<FireHailEntity>> FIRE_HAIL =
            ENTITY_TYPES.register("fire_hail",
                    () -> EntityType.Builder.<FireHailEntity>of((type, level) -> new net.artur.nacikmod.entity.projectiles.FireHailEntity(type, level), MobCategory.MISC)
                            .sized(1.0F, 1.0F)
                            .clientTrackingRange(16)
                            .updateInterval(2)
                            .build("fire_hail"));

    public static final RegistryObject<EntityType<FireWallEntity>> FIRE_WALL =
            ENTITY_TYPES.register("fire_wall",
                    () -> EntityType.Builder.<FireWallEntity>of(FireWallEntity::new, MobCategory.MISC)
                            .sized(3.0F, 4.0F)
                            .clientTrackingRange(32)
                            .updateInterval(10)
                            .build("fire_wall"));

    public static final RegistryObject<EntityType<FireWallDamageZone>> FIRE_WALL_DAMAGE_ZONE =
            ENTITY_TYPES.register("fire_wall_damage_zone",
                    () -> EntityType.Builder.<FireWallDamageZone>of(FireWallDamageZone::new, MobCategory.MISC)
                            .sized(3.0F, 4.0F)
                            .clientTrackingRange(32)
                            .updateInterval(10)
                            .build("fire_wall_damage_zone"));

    public static final RegistryObject<EntityType<AssassinEntity>> ASSASSIN =
            ENTITY_TYPES.register("assassin", () ->
                    EntityType.Builder.of(AssassinEntity::new, MobCategory.MONSTER)
                            .sized(0.6f, 1.8f)
                            .build("assassin")
            );

    public static final RegistryObject<EntityType<RedBerserkerEntity>> RED_BERSERK =
            ENTITY_TYPES.register("red_berserker", () ->
                    EntityType.Builder.of(RedBerserkerEntity::new, MobCategory.MONSTER)
                            .sized(1.2f, 3.6f)
                            .build("red_berserker")
            );

    public static final RegistryObject<EntityType<ShamakEntity>> SHAMAK =
            ENTITY_TYPES.register("shamak",
                    () -> EntityType.Builder.<ShamakEntity>of((type, level) -> new ShamakEntity(type, level), MobCategory.MISC)
                            .sized(9.0F, 5.0F) // Размер 9x5x9
                            .clientTrackingRange(16)
                            .updateInterval(2)
                            .build("shamak"));

    public static final RegistryObject<EntityType<GraalEntity>> GRAIL =
            ENTITY_TYPES.register("grail", () ->
                    EntityType.Builder.of(GraalEntity::new, MobCategory.MONSTER)
                            .sized(10.0f, 10.0f) // Размер увеличен в 20 раз (было 1.0f, 2.0f)
                            .build("grail")
            );

    public static final RegistryObject<EntityType<InquisitorEntity>> INQUISITOR =
            ENTITY_TYPES.register("inquisitor", () ->
                    EntityType.Builder.of(InquisitorEntity::new, MobCategory.MONSTER)
                            .sized(0.6f, 1.8f)
                            .build("inquisitor")
            );

    public static final RegistryObject<EntityType<FireballEntity>> FIREBALL =
            ENTITY_TYPES.register("fireball",
                    () -> EntityType.Builder.<FireballEntity>of((type, level) -> new FireballEntity(type, level), MobCategory.MISC)
                            .sized(2.4F, 2.4F) // Увеличен в 3 раза (было 0.8F)
                            .clientTrackingRange(16)
                            .updateInterval(2)
                            .build("fireball"));

    public static final RegistryObject<EntityType<CrossProjectile>> CROSS_PROJECTILE =
            ENTITY_TYPES.register("cross_projectile",
                    () -> EntityType.Builder.<CrossProjectile>of((type, level) -> new CrossProjectile(type, level), MobCategory.MISC)
                            .sized(2.5F, 3.5F) // Приблизительный размер креста
                            .clientTrackingRange(16)
                            .updateInterval(2)
                            .build("cross_projectile"));

    public static void register(IEventBus eventBus) {
        ENTITY_TYPES.register(eventBus);
    }
}