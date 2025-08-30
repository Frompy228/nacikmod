package net.artur.nacikmod.registry;

import net.artur.nacikmod.NacikMod;
import net.artur.nacikmod.gui.EnchantmentLimitTableMenu;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.network.IContainerFactory;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.function.Supplier;

public class ModMenuTypes {
    private static final DeferredRegister<MenuType<?>> MENUS = 
            DeferredRegister.create(ForgeRegistries.MENU_TYPES, NacikMod.MOD_ID);

    public static void register(IEventBus eventBus) {
        MENUS.register(eventBus);
    }

    private static <T extends AbstractContainerMenu> Supplier<MenuType<T>> registerMenuType(IContainerFactory<T> factory, String name) {
        return MENUS.register(name, () -> IForgeMenuType.create(factory));
    }

    public static final Supplier<MenuType<EnchantmentLimitTableMenu>> ENCHANTMENT_LIMIT_TABLE_MENU = 
            registerMenuType(EnchantmentLimitTableMenu::new, "enchantment_limit_table_menu");
}
