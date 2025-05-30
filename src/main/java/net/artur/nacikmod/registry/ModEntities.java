package net.artur.nacikmod.registry;

import net.artur.nacikmod.entity.custom.LanserEntity;
import net.artur.nacikmod.entity.custom.LeonidEntity;
import net.artur.nacikmod.entity.custom.SpartanEntity;
import net.artur.nacikmod.entity.custom.BerserkerEntity;
import net.artur.nacikmod.entity.projectiles.BloodShootProjectile;
import net.artur.nacikmod.entity.projectiles.FireArrowEntity;
import net.artur.nacikmod.entity.projectiles.ManaSwordProjectile;
import net.artur.nacikmod.entity.projectiles.ManaArrowProjectile;
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

    public static void register(IEventBus eventBus) {
        ENTITY_TYPES.register(eventBus);
    }
}
