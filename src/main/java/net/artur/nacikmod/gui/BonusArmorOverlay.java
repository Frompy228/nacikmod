package net.artur.nacikmod.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import net.artur.nacikmod.NacikMod;
import net.artur.nacikmod.registry.ModAttributes;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;


@OnlyIn(Dist.CLIENT)
@Mod.EventBusSubscriber(modid = NacikMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class BonusArmorOverlay {
    private static final ResourceLocation BONUS_ARMOR_ICON = new ResourceLocation(NacikMod.MOD_ID, "textures/gui/bonus_armor.png");

    @SubscribeEvent
    public static void onRenderGuiOverlay(RenderGuiOverlayEvent.Pre event) {
        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        if (player == null) return;

        int screenWidth = event.getWindow().getGuiScaledWidth();
        int screenHeight = event.getWindow().getGuiScaledHeight();

        // 1. Расчет высоты полосок здоровья
        int healthRows = (int) Math.ceil(player.getMaxHealth() / 20.0);
        int healthHeight = 0;

        if (healthRows <= 2) {
            // Для 1-2 полосок: 10px на каждую
            healthHeight = healthRows * 10;
        } else {
            // Для 3+ полосок: 10 + (n-1)*(10 - (n-2))
            healthHeight = 10 + (healthRows - 1) * (10 - (healthRows - 2));
        }

        // 2. Корректировка базовой позиции
        int baseY = screenHeight - healthHeight - 39;

        double bonusArmor = player.getAttributeValue(ModAttributes.BONUS_ARMOR.get());
        int bonusArmorIcons = (int) (bonusArmor);

        if (bonusArmorIcons > 0) {
            RenderSystem.enableBlend();
            GuiGraphics guiGraphics = event.getGuiGraphics();
            int xStart = screenWidth / 2 - 91;

            // 3. Отрисовка бонусной брони
            for (int i = 0; i < bonusArmorIcons; i++) {
                int row = i / 10;
                int x = xStart + (i % 10) * 8;
                int y = baseY - row * 10; // Фиксированный интервал 10px между рядами
                guiGraphics.blit(BONUS_ARMOR_ICON, x, y, 0, 0, 9, 9, 9, 9);
            }
            RenderSystem.disableBlend();
        }
    }
}
