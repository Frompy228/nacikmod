package net.artur.nacikmod.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.artur.nacikmod.NacikMod;
import net.artur.nacikmod.capability.mana.ManaProvider;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RenderGuiEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;


@OnlyIn(Dist.CLIENT)
@Mod.EventBusSubscriber(modid = NacikMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class ManaOverlayRenderer {

    @SubscribeEvent
    public static void onRenderGui(RenderGuiEvent.Post event) {

        Minecraft minecraft = Minecraft.getInstance();
        Player player = minecraft.player;
        if (player == null) return;

        player.getCapability(ManaProvider.MANA_CAPABILITY).ifPresent(mana -> {
            int currentMana = mana.getMana();
            int maxMana = mana.getMaxMana();
            boolean isTrueMage = mana.isTrueMage();

            // Координаты в левом верхнем углу
            int x = 10;
            int y = 10;

            PoseStack poseStack = event.getGuiGraphics().pose();
            Font font = minecraft.font;

            poseStack.pushPose();
            RenderSystem.enableBlend();

            // Отрисовка текста маны
            event.getGuiGraphics().drawString(font, "Mana: " + currentMana + "/" + maxMana, x, y, 0x00FFFF, false);
            
            // Отображаем статус "True Mage" если он есть
            if (isTrueMage) {
                event.getGuiGraphics().drawString(font, "True Mage", x, y + 12, 0x8b0000, false);
            }

            RenderSystem.disableBlend();
            poseStack.popPose();
        });

    }
}
