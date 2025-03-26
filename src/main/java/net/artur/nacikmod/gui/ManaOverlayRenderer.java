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
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;


@OnlyIn(Dist.CLIENT)
@Mod.EventBusSubscriber(modid = NacikMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT) // Добавлено value = Dist.CLIENT
public class ManaOverlayRenderer {

    @SubscribeEvent
    public static void onRenderGuiOverlay(RenderGuiOverlayEvent.Pre event) {

        Minecraft minecraft = Minecraft.getInstance();
        Player player = minecraft.player;
        if (player == null) return;

        player.getCapability(ManaProvider.MANA_CAPABILITY).ifPresent(mana -> {
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
