package net.artur.nacikmod.registry;

import net.artur.nacikmod.NacikMod;
import net.artur.nacikmod.lib.SetManaFunction;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public class ModLootFunctions {
    public static final DeferredRegister<LootItemFunctionType> LOOT_FUNCTIONS =
            DeferredRegister.create(Registries.LOOT_FUNCTION_TYPE, NacikMod.MOD_ID);

    public static final RegistryObject<LootItemFunctionType> SET_MANA =
            LOOT_FUNCTIONS.register("set_mana", () -> new LootItemFunctionType(new SetManaFunction.Serializer()));

    public static void register(IEventBus eventBus) {
        LOOT_FUNCTIONS.register(eventBus);
    }
}