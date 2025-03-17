package net.artur.nacikmod.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import net.artur.nacikmod.NacikMod;
import net.artur.nacikmod.registry.ModAttributes;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = NacikMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class BonusArmorOverlay {
    private static final ResourceLocation BONUS_ARMOR_ICON = new ResourceLocation(NacikMod.MOD_ID, "textures/gui/bonus_armor.png");

    @SubscribeEvent
    public static void onRenderGuiOverlay(RenderGuiOverlayEvent.Pre event) {
        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        if (player == null) return;

        int screenWidth = event.getWindow().getGuiScaledWidth();
        int screenHeight = event.getWindow().getGuiScaledHeight();
        int healthRows = (int) Math.ceil(player.getMaxHealth() / 20.0); // Количество полосок ХП
        int healthBarHeight = 10; // Высота одной полоски ХП
        int baseY = screenHeight - healthRows * healthBarHeight - 39; // Расположение относительно ХП

        double bonusArmor = player.getAttributeValue(ModAttributes.BONUS_ARMOR.get());
        int bonusArmorIcons = (int) (bonusArmor / 2);

        if (bonusArmorIcons > 0) {
            RenderSystem.enableBlend();
            GuiGraphics guiGraphics = event.getGuiGraphics();
            int xStart = screenWidth / 2 - 91;
            for (int i = 0; i < bonusArmorIcons; i++) {
                int x = xStart + (i % 10) * 8;
                int y = baseY - (i / 10) * 10; // Поднимаем выше, если больше 10 иконок
                guiGraphics.blit(BONUS_ARMOR_ICON, x, y, 0, 0, 9, 9, 9, 9);
            }
            RenderSystem.disableBlend();
        }
    }
}
