package net.artur.nacikmod.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.artur.nacikmod.NacikMod;
import net.artur.nacikmod.registry.ModAttributes; // Класс с зарегистрированными атрибутами маны
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = NacikMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ManaOverlayRenderer {

    @SubscribeEvent
    public static void onRenderGuiOverlay(RenderGuiOverlayEvent.Pre event) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null) return;

        // Получаем атрибуты маны и максимальной маны из игрока
        AttributeInstance manaAttribute = minecraft.player.getAttribute(ModAttributes.MANA.get());
        AttributeInstance maxManaAttribute = minecraft.player.getAttribute(ModAttributes.MAX_MANA.get());
        if (manaAttribute == null || maxManaAttribute == null) return;

        int currentMana = (int) manaAttribute.getValue();
        int maxMana = (int) maxManaAttribute.getValue();

        // Задаём координаты для отрисовки оверлея
        int x = 10;
        int y = 10;

        PoseStack poseStack = event.getGuiGraphics().pose();
        Font font = minecraft.font;

        poseStack.pushPose();
        RenderSystem.enableBlend();

        // Отрисовка текста с данными о мане
        event.getGuiGraphics().drawString(font, "Mana: " + currentMana + "/" + maxMana, x, y, 0x00FFFF, false);

        RenderSystem.disableBlend();
        poseStack.popPose();
    }
}
