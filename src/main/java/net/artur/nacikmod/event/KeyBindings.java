package net.artur.nacikmod.event;

import com.mojang.blaze3d.platform.InputConstants;
import net.artur.nacikmod.NacikMod;
import net.artur.nacikmod.registry.ModItems;
import net.minecraft.client.Minecraft;
import net.minecraft.client.KeyMapping;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.settings.KeyConflictContext;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.type.inventory.ICurioStacksHandler;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.glfw.GLFW;

@Mod.EventBusSubscriber(modid = "nacikmod", bus = Mod.EventBusSubscriber.Bus.MOD)
public final class KeyBindings {
    public static final KeyBindings INSTANSE = new KeyBindings();
    private KeyBindings(){

    }
    public final KeyMapping ability = new KeyMapping("key." + NacikMod.MOD_ID + ".ability_key", KeyConflictContext.IN_GAME, InputConstants.getKey(InputConstants.KEY_P,-1),KeyMapping.CATEGORY_GAMEPLAY);



}
