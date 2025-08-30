package net.artur.nacikmod.gui;

import net.artur.nacikmod.NacikMod;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class EnchantmentLimitTableScreen extends AbstractContainerScreen<EnchantmentLimitTableMenu> {
    private static final ResourceLocation TEXTURE =
            new ResourceLocation(NacikMod.MOD_ID, "textures/gui/enchant_limit_table.png");

    public EnchantmentLimitTableScreen(EnchantmentLimitTableMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageWidth = 176;
        this.imageHeight = 166;
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight) / 2;

        guiGraphics.blit(TEXTURE, x, y, 0, 0, imageWidth, imageHeight);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(guiGraphics);
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        renderTooltip(guiGraphics, mouseX, mouseY);

        // Render XP cost like anvil: only when result present, right-aligned on inventory label row
        if (!menu.getResultSlot().getItem().isEmpty()) {
            int required = menu.getRequiredXpLevels();
            if (required > 0) {
                int playerLv = this.minecraft.player != null ? this.minecraft.player.experienceLevel : 0;
                int color = playerLv >= required ? 0x80FF20 : 0xFF6060; // anvil-like colors
                String text = "Exp Cost: " + required;
                int x = (width - imageWidth) / 2;
                int y = (height - imageHeight) / 2;
                int textX = x + imageWidth - 8 - this.font.width(text);
                int textY = y + this.inventoryLabelY; // align with inventory label baseline
                guiGraphics.drawString(this.font, text, textX, textY, color, true); // shadow like anvil
            }
        }
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        // Vanilla-like labels without shadow
        guiGraphics.drawString(this.font, this.title, this.titleLabelX, this.titleLabelY, 0x404040, false);
        guiGraphics.drawString(this.font, this.playerInventoryTitle, this.inventoryLabelX, this.inventoryLabelY, 0x404040, false);
    }
}
