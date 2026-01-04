package net.artur.nacikmod.gui;

import net.artur.nacikmod.NacikMod;
import net.artur.nacikmod.network.EnchantmentSelectionPacket;
import net.artur.nacikmod.registry.ModMessages;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.enchantment.Enchantment;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

public class EnchantmentLimitTableScreen extends AbstractContainerScreen<EnchantmentLimitTableMenu> {
    private static final ResourceLocation TEXTURE =
            new ResourceLocation(NacikMod.MOD_ID, "textures/gui/enchant_limit_table.png");
    private static final ResourceLocation MANA_CRYSTAL_ICON =
            new ResourceLocation(NacikMod.MOD_ID, "textures/gui/mana_crystal_empty_gui.png");
    private static final ResourceLocation SHARD_ARTIFACT_ICON =
            new ResourceLocation(NacikMod.MOD_ID, "textures/gui/shard_artifact_gui.png");

    // Параметры анимации для shard artifact
    private static final int SHARD_ANIMATION_FRAMETIME = 20; // Тиков на кадр (из .mcmeta)
    private static final int SHARD_ANIMATION_FRAME_COUNT = 4; // Количество кадров в текстуре
    private static final int SHARD_ANIMATION_FRAME_HEIGHT = 16; // Высота одного кадра

    private List<EnchantmentButton> enchantmentButtons = new ArrayList<>();
    private int scrollOffset = 0;
    private boolean isSelectAllMode = false; // true = выбрано все, false = ничего не выбрано
    private int lastEnchantmentCount = 0; // Для отслеживания изменений
    private Map<Enchantment, Boolean> localSelectionState = new HashMap<>(); // Локальное состояние выбора

    public EnchantmentLimitTableScreen(EnchantmentLimitTableMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageWidth = 176;
        this.imageHeight = 166;
    }

    @Override
    protected void init() {
        super.init();
        createEnchantmentButtons();
        createControlButtons();
    }

    private void createEnchantmentButtons() {
        enchantmentButtons.clear();

        Map<Enchantment, Integer> availableEnchantments = menu.getAvailableEnchantments();

        // те же смещения, что в renderBg
        int offsetX = 4;
        int offsetY = -8;

        int buttonY = 16 + offsetY; // начинаем от listTop
        int maxButtons = 4;

        int buttonIndex = 0;
        for (Map.Entry<Enchantment, Integer> entry : availableEnchantments.entrySet()) {
            if (buttonIndex >= scrollOffset && buttonIndex < scrollOffset + maxButtons) {
                Enchantment enchantment = entry.getKey();
                int level = entry.getValue();

                EnchantmentButton button = new EnchantmentButton(
                        leftPos + 25 + offsetX, // добавляем offsetX
                        topPos + buttonY,       // добавляем offsetY
                        110,
                        18,
                        enchantment,
                        level,
                        button1 -> toggleEnchantment(enchantment)
                );

                enchantmentButtons.add(button);
                addRenderableWidget(button);

                buttonY += 16; // отступ между кнопками
            }
            buttonIndex++;
        }
    }


    private void updateEnchantmentButtons() {
        // Удаляем старые кнопки
        for (EnchantmentButton button : enchantmentButtons) {
            removeWidget(button);
        }
        enchantmentButtons.clear();

        // Создаем новые кнопки
        createEnchantmentButtons();
    }

    private void createControlButtons() {
        // Кнопка "Выбрать все/Снять выбор" (переключаемая)
        Button toggleAllButton = Button.builder(
                Component.literal("Select all"),
                button -> {
                    if (isSelectAllMode) {
                        menu.deselectAllEnchantments();
                        isSelectAllMode = false;
                        button.setMessage(Component.literal("Select all"));
                        // Обновляем локальное состояние
                        for (Enchantment enchantment : localSelectionState.keySet()) {
                            localSelectionState.put(enchantment, false);
                        }
                    } else {
                        menu.selectAllEnchantments();
                        isSelectAllMode = true;
                        button.setMessage(Component.literal("Remove"));
                        // Обновляем локальное состояние
                        for (Enchantment enchantment : localSelectionState.keySet()) {
                            localSelectionState.put(enchantment, true);
                        }
                    }
                    updateButtonStates();
                }
        ).bounds(leftPos + 175, topPos + 0, 110, 18).build(); // Сдвигаем кнопку вправо

        addRenderableWidget(toggleAllButton);
    }

    private void updateButtonStates() {
        // Обновляем состояние всех кнопок
        for (EnchantmentButton button : enchantmentButtons) {
            button.updateSelectionState();
        }
    }

    private void toggleEnchantment(Enchantment enchantment) {
        // Немедленно обновляем локальное состояние
        boolean currentState = localSelectionState.getOrDefault(enchantment, false);
        boolean newState = !currentState;
        localSelectionState.put(enchantment, newState);

        // Отправляем пакет на сервер с правильным состоянием
        ModMessages.INSTANCE.sendToServer(new EnchantmentSelectionPacket(enchantment, newState));

        // Обновляем состояние кнопок немедленно
        updateButtonStates();
    }

    // Обработка прокрутки колесиком мыши
    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollDelta) {
        Map<Enchantment, Integer> availableEnchantments = menu.getAvailableEnchantments();
        int maxButtons = 4; // Обновляем для 4 кнопок
        int totalButtons = availableEnchantments.size();

        if (totalButtons > maxButtons) {
            if (scrollDelta > 0 && scrollOffset > 0) {
                // Прокрутка вверх
                scrollOffset--;
                updateEnchantmentButtons();
            } else if (scrollDelta < 0 && scrollOffset < totalButtons - maxButtons) {
                // Прокрутка вниз
                scrollOffset++;
                updateEnchantmentButtons();
            }
        }

        return super.mouseScrolled(mouseX, mouseY, scrollDelta);
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight) / 2;

        guiGraphics.blit(TEXTURE, x, y, 0, 0, imageWidth, imageHeight);

        // Смещения
        int offsetX = 8;  // вправо
        int offsetY = -8; // вверх

        // Верхняя и нижняя границы списка
        int listTop = topPos + 16 + offsetY;
        int listBottom = topPos + 80 + offsetY;

        // Фон списка
        guiGraphics.fill(leftPos + 20 + offsetX, listTop, leftPos + 140 + offsetX, listBottom, 0x80000000);

        // Рендерим индикатор прокрутки
        Map<Enchantment, Integer> availableEnchantments = menu.getAvailableEnchantments();
        int maxButtons = 4;
        if (availableEnchantments.size() > maxButtons) {
            int scrollBarHeight = listBottom - listTop;
            int scrollBarY = listTop;
            int scrollBarX = leftPos + 140 + offsetX;

            // Фон полосы прокрутки
            guiGraphics.fill(scrollBarX, scrollBarY, scrollBarX + 2, scrollBarY + scrollBarHeight, 0x40FFFFFF);

            // Ползунок
            int sliderHeight = Math.max(10, (scrollBarHeight * maxButtons) / availableEnchantments.size());
            int sliderY = scrollBarY + (scrollBarHeight - sliderHeight) * scrollOffset / Math.max(1, availableEnchantments.size() - maxButtons);
            guiGraphics.fill(scrollBarX, sliderY, scrollBarX + 2, sliderY + sliderHeight, 0x80FFFFFF);
        }
    }


    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(guiGraphics);
        super.render(guiGraphics, mouseX, mouseY, partialTick);

        // Обновляем кнопки зачарований только при необходимости
        updateEnchantmentButtonsIfNeeded();

        // Рендерим иконки для пустых слотов
        renderSlotIcons(guiGraphics, partialTick);

        renderTooltip(guiGraphics, mouseX, mouseY);

        // Рендерим стоимость XP
        int required = menu.getRequiredXpLevels();
        if (required > 0) {
            int playerLv = this.minecraft.player != null ? this.minecraft.player.experienceLevel : 0;
            int color = playerLv >= required ? 0x80FF20 : 0xFF6060; // зелёный если хватает, красный если нет
            String text = "Exp Cost: " + required;
            int textX = leftPos + imageWidth - 8 - this.font.width(text);
            int textY = topPos + this.inventoryLabelY;
            guiGraphics.drawString(this.font, text, textX, textY, color, true);
        }

    }


    private void updateEnchantmentButtonsIfNeeded() {
        // Проверяем, нужно ли обновить кнопки
        Map<Enchantment, Integer> currentEnchantments = menu.getAvailableEnchantments();
        int currentCount = currentEnchantments.size();

        // Обновляем только если количество зачарований изменилось
        if (currentCount != lastEnchantmentCount) {
            lastEnchantmentCount = currentCount;
            scrollOffset = 0; // Сбрасываем прокрутку при изменении предмета

            // Синхронизируем локальное состояние с серверным
            localSelectionState.clear();
            for (Enchantment enchantment : currentEnchantments.keySet()) {
                localSelectionState.put(enchantment, menu.isEnchantmentSelected(enchantment));
            }

            updateEnchantmentButtons();
        }
    }

    private void syncLocalStateWithServer() {
        Map<Enchantment, Integer> currentEnchantments = menu.getAvailableEnchantments();
        for (Enchantment enchantment : currentEnchantments.keySet()) {
            boolean serverState = menu.isEnchantmentSelected(enchantment);
            boolean localState = localSelectionState.getOrDefault(enchantment, false);

            if (serverState != localState) {
                localSelectionState.put(enchantment, serverState);
            }
        }
    }

    /**
     * Рендерит иконки для пустых слотов (mana crystal и shard artifact)
     * Для shard artifact используется анимированная текстура с ручным выбором кадра
     */
    private void renderSlotIcons(GuiGraphics guiGraphics, float partialTick) {
        // Иконка для слота кристалла маны (позиция: 8, 32)
        if (menu.getCrystalSlot().getItem().isEmpty()) {
            int crystalSlotX = leftPos + 8;
            int crystalSlotY = topPos + 32;
            guiGraphics.blit(MANA_CRYSTAL_ICON, crystalSlotX, crystalSlotY, 0, 0, 16, 16, 16, 16);
        }

        // Иконка для слота shard artifact (позиция: 8, 55) с анимацией
        if (menu.getShardSlot().getItem().isEmpty()) {
            int shardSlotX = leftPos + 8;
            int shardSlotY = topPos + 55;

            if (this.minecraft != null && this.minecraft.level != null) {
                long gameTime = this.minecraft.level.getGameTime();


                float animationTime = (gameTime % (SHARD_ANIMATION_FRAMETIME * SHARD_ANIMATION_FRAME_COUNT)) + partialTick;
                int currentFrame = (int) (animationTime / SHARD_ANIMATION_FRAMETIME) % SHARD_ANIMATION_FRAME_COUNT;


                int textureV = currentFrame * SHARD_ANIMATION_FRAME_HEIGHT;

                int totalTextureHeight = SHARD_ANIMATION_FRAME_COUNT * SHARD_ANIMATION_FRAME_HEIGHT;


                guiGraphics.blit(SHARD_ARTIFACT_ICON, shardSlotX, shardSlotY,
                        0, textureV, 16, 16, 16, totalTextureHeight);
            } else {
                // Fallback: показываем первый кадр
                guiGraphics.blit(SHARD_ARTIFACT_ICON, shardSlotX, shardSlotY, 0, 0, 16, 16, 16, 16);
            }
        }
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        // Vanilla-like labels without shadow
        guiGraphics.drawString(this.font, this.title, this.titleLabelX, this.titleLabelY, 0x404040, false);
        guiGraphics.drawString(this.font, this.playerInventoryTitle, this.inventoryLabelX, this.inventoryLabelY, 0x404040, false);
    }

    private class EnchantmentButton extends Button {
        private final Enchantment enchantment;
        private final int level;

        public EnchantmentButton(int x, int y, int width, int height, Enchantment enchantment, int level, OnPress onPress) {
            super(x, y, width, height, Component.empty(), onPress, DEFAULT_NARRATION);
            this.enchantment = enchantment;
            this.level = level;
        }

        public void updateSelectionState() {
            // Обновляем состояние кнопки
        }

        @Override
        public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
            // Проверяем, выбрано ли зачарование (используем локальное состояние)
            boolean isSelected = localSelectionState.getOrDefault(enchantment, false);

            // Рендерим кнопку с разными цветами для выбранного/невыбранного состояния
            int color;
            if (isSelected) {
                color = this.isHoveredOrFocused() ? 0x44AA44 : 0x228822; // Зеленый для выбранного
            } else {
                color = this.isHoveredOrFocused() ? 0x666666 : 0x444444; // Серый для невыбранного
            }

            guiGraphics.fill(this.getX(), this.getY(), this.getX() + this.width, this.getY() + this.height, color);

            // Рендерим текст зачарования с правильным названием
            String enchantmentName = getEnchantmentDisplayName(enchantment);
            String text = enchantmentName + " " + level;
            int textColor;
            if (isSelected) {
                textColor = 0xFFFFFF; // Белый для выбранного
            } else {
                textColor = this.isHoveredOrFocused() ? 0xFFFFFF : 0xCCCCCC; // Обычные цвета для невыбранного
            }

            guiGraphics.drawString(font, text, this.getX() + 2, this.getY() + 5, textColor, false); // Сдвигаем текст выше для меньшей высоты

            // Добавляем индикатор выбора
            if (isSelected) {
                guiGraphics.drawString(font, "✓", this.getX() + this.width - 10, this.getY() + 5, 0x00FF00, false);
            }
        }

        private String getEnchantmentDisplayName(Enchantment enchantment) {
            // Получаем локализованное название зачарования
            String key = enchantment.getDescriptionId();
            if (key.startsWith("enchantment.minecraft.")) {
                String name = key.substring("enchantment.minecraft.".length());
                // Преобразуем первую букву в заглавную и заменяем подчеркивания на пробелы
                if (!name.isEmpty()) {
                    name = name.substring(0, 1).toUpperCase() + name.substring(1);
                    name = name.replace("_", " ");
                }
                return name;
            }
            return enchantment.getDescriptionId();
        }
    }
}
