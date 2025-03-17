package net.artur.nacikmod.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import net.artur.nacikmod.NacikMod;
import net.artur.nacikmod.registry.ModEffects;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = NacikMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class EffectOverlayRenderer {

    private static final ResourceLocation LOVE_EFFECT_OVERLAY = new ResourceLocation(NacikMod.MOD_ID, "textures/gui/love_effect_overlay.png");
    private static final ResourceLocation TIME_SLOW_EFFECT_OVERLAY = new ResourceLocation(NacikMod.MOD_ID, "textures/gui/time_slow_effect_overlay.png");

    @SubscribeEvent
    public static void onRenderOverlay(RenderGuiOverlayEvent.Post event) {
        if (event.getOverlay() != VanillaGuiOverlay.AIR_LEVEL.type()) return; // Проверяем, что рендер идет в нужный момент

        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player == null) return;

        if (player.hasEffect(ModEffects.TIME_SLOW.get())) {
            RenderSystem.enableBlend();
            mc.getTextureManager().bindForSetup(TIME_SLOW_EFFECT_OVERLAY);
            GuiGraphics guiGraphics = event.getGuiGraphics();

            int screenWidth = mc.getWindow().getGuiScaledWidth();
            int screenHeight = mc.getWindow().getGuiScaledHeight();

            // Масштабируем изображение, например, до 50% ширины и высоты экрана
            int overlayWidth = screenWidth;  // Изменяем размер, 50% от ширины экрана
            int overlayHeight = screenHeight;  // Изменяем размер, 50% от высоты экрана

            int x = (screenWidth - overlayWidth); // Центрируем по горизонтали
            int y = (screenHeight - overlayHeight); // Центрируем по вертикали

            guiGraphics.blit(TIME_SLOW_EFFECT_OVERLAY, x, y, 0, 0, overlayWidth, overlayHeight, overlayWidth, overlayHeight);

            RenderSystem.disableBlend();
        }

        // Проверяем, активен ли эффект "любовь"
        if (player.hasEffect(ModEffects.LOVE.get())) {
            RenderSystem.enableBlend();
            mc.getTextureManager().bindForSetup(LOVE_EFFECT_OVERLAY);
            GuiGraphics guiGraphics = event.getGuiGraphics();

            int screenWidth = mc.getWindow().getGuiScaledWidth();
            int screenHeight = mc.getWindow().getGuiScaledHeight();

            // Масштабируем изображение, например, до 50% ширины и высоты экрана
            int overlayWidth = screenWidth ;  // Изменяем размер, 50% от ширины экрана
            int overlayHeight = screenHeight ;  // Изменяем размер, 50% от высоты экрана

            int x = (screenWidth - overlayWidth) ; // Центрируем по горизонтали
            int y = (screenHeight - overlayHeight) ; // Центрируем по вертикали

            guiGraphics.blit(LOVE_EFFECT_OVERLAY, x, y, 0, 0, overlayWidth, overlayHeight, overlayWidth, overlayHeight);

            RenderSystem.disableBlend();
        }
    }
}
