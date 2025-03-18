package net.artur.nacikmod.registry;

import net.artur.nacikmod.NacikMod;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.RangedAttribute;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModAttributes {
    public static final DeferredRegister<Attribute> ATTRIBUTES =
            DeferredRegister.create(ForgeRegistries.ATTRIBUTES, NacikMod.MOD_ID);

    public static final RegistryObject<Attribute> MAX_MANA = ATTRIBUTES.register("maxmana",
            () -> new RangedAttribute("attribute.nacikmod.maxmana", 0.0, 0.0, 20000000.0).setSyncable(true));

    public static final RegistryObject<Attribute> MANA = ATTRIBUTES.register("mana",
            () -> new RangedAttribute("attribute.nacikmod.mana", 0.0, 0.0, 20000000.0).setSyncable(true));

    public static final RegistryObject<Attribute> BONUS_ARMOR = ATTRIBUTES.register("bonus_armor",
            () -> new RangedAttribute("attribute.nacikmod.bonus_armor", 0.0, 0.0, 2048.0).setSyncable(true));

}
