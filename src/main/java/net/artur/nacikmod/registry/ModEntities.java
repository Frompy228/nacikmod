package net.artur.nacikmod.registry;


import net.artur.nacikmod.entity.custom.LanserEntity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.artur.nacikmod.NacikMod;
import net.minecraftforge.registries.RegistryObject;


public class ModEntities {
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES =
            DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, NacikMod.MOD_ID);


    public static final RegistryObject<EntityType<LanserEntity>> LANSER =
            ENTITY_TYPES.register("lanser", () -> EntityType.Builder.of(LanserEntity::new, MobCategory.MONSTER).sized(0.6f,1.8f).build("lanser"));
    public static void register(IEventBus eventBus) {
        ENTITY_TYPES.register(eventBus);
    }

}



