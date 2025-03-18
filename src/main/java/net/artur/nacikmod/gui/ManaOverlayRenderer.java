package net.artur.nacikmod.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.artur.nacikmod.capability.mana.IMana;
import net.artur.nacikmod.capability.mana.ManaCapability;
import net.artur.nacikmod.NacikMod;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = NacikMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ManaOverlayRenderer {

    @SubscribeEvent
    public static void onRenderGuiOverlay(RenderGuiOverlayEvent.Pre event) {

        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null) return;
        minecraft.player.getCapability(ManaCapability.MANA_CAPABILITY).ifPresent(mana -> {
            int currentMana = mana.getMana();
            int maxMana = mana.getMaxMana();

            // Координаты в левом верхнем углу
            int x = 10;
            int y = 10;

            PoseStack poseStack = event.getGuiGraphics().pose();
            Font font = minecraft.font;

            poseStack.pushPose();
            RenderSystem.enableBlend();

            // Отрисовка текста
            event.getGuiGraphics().drawString(font, "Mana: " + currentMana + "/" + maxMana, x, y, 0x00FFFF, false);
            RenderSystem.disableBlend();
            poseStack.popPose();


        });

    }
}
