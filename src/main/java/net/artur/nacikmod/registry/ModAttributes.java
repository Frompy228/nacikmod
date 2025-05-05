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



    public static final RegistryObject<Attribute> BONUS_ARMOR = ATTRIBUTES.register("bonus_armor",
            () -> new RangedAttribute("attribute.name.nacikmod.bonus_armor", 0.0D, 0.0D, 1024.0D).setSyncable(true));
}
