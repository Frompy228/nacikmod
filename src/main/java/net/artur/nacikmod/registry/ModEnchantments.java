package net.artur.nacikmod.registry;

import net.artur.nacikmod.NacikMod;
import net.artur.nacikmod.enchantment.MagicBurnEnchantment;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModEnchantments {

    public static final DeferredRegister<Enchantment> ENCHANTMENTS =
            DeferredRegister.create(ForgeRegistries.ENCHANTMENTS, NacikMod.MOD_ID);

    public static final RegistryObject<Enchantment> MAGIC_BURN =
            ENCHANTMENTS.register("magic_burn",
                    () -> new MagicBurnEnchantment(Enchantment.Rarity.RARE, EquipmentSlot.MAINHAND));

    public static void register(IEventBus eventBus) {
        ENCHANTMENTS.register(eventBus);
    }
}

