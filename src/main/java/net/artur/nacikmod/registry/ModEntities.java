package net.artur.nacikmod.registry;

import net.artur.nacikmod.entity.custom.LanserEntity;
import net.artur.nacikmod.entity.projectiles.FireArrowEntity;
import net.artur.nacikmod.NacikMod;
import net.minecraft.resources.ResourceLocation;
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

    public static final RegistryObject<EntityType<FireArrowEntity>> FIRE_ARROW =
            ENTITY_TYPES.register("fire_arrow", () ->
                    EntityType.Builder.<FireArrowEntity>of(FireArrowEntity::new, MobCategory.MISC)
                            .sized(0.25F, 0.25F)
                            .clientTrackingRange(16)
                            .updateInterval(2)
                            .build("fire_arrow")
            );

    public static void register(IEventBus eventBus) {
        ENTITY_TYPES.register(eventBus);
    }
}
