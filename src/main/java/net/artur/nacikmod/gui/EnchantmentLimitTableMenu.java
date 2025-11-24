package net.artur.nacikmod.gui;

import net.artur.nacikmod.item.MagicCrystal;
import net.artur.nacikmod.registry.ModMenuTypes;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;

import java.util.HashMap;
import java.util.Map;

public class EnchantmentLimitTableMenu extends AbstractContainerMenu {
    private final ContainerLevelAccess access;
    private final Player player;
    private final Level level;
    private static final int MANA_COST = 3000;
    private static final int XP_LEVEL_COST_PER_ENCHANT = 20;

    private int requiredXpLevels = 0;
    private final DataSlot syncedXpLevels = DataSlot.standalone();

    // Добавляем поля для отслеживания выбранных зачарований
    private final Map<Enchantment, Boolean> selectedEnchantments = new HashMap<>();
    private final Map<Enchantment, Integer> availableEnchantments = new HashMap<>();

    // Слоты
    private final Slot enchantedItemSlot;
    private final Slot crystalSlot;
    private final Slot shardSlot;
    private final Slot resultSlot;

    // Контейнеры для слотов
    protected final Container enchantedItemContainer = new SimpleContainer(1) {
        @Override
        public void setChanged() {
            super.setChanged();
            EnchantmentLimitTableMenu.this.slotsChanged(this);
        }
    };

    protected final Container crystalContainer = new SimpleContainer(1) {
        @Override
        public void setChanged() {
            super.setChanged();
            EnchantmentLimitTableMenu.this.slotsChanged(this);
        }
    };

    protected final Container shardContainer = new SimpleContainer(1) {
        @Override
        public void setChanged() {
            super.setChanged();
            EnchantmentLimitTableMenu.this.slotsChanged(this);
        }
    };

    protected final ResultContainer resultContainer = new ResultContainer();

    public EnchantmentLimitTableMenu(int containerId, Inventory inv, FriendlyByteBuf extraData) {
        this(containerId, inv, ContainerLevelAccess.NULL);
    }


    // Устанавливаем состояние выбора зачарования явно
    public void setEnchantmentSelection(Enchantment enchantment, boolean selected) {
        if (availableEnchantments.containsKey(enchantment)) {
            selectedEnchantments.put(enchantment, selected);
            setupResultSlot(); // Пересчёт результата
        }
    }


    public EnchantmentLimitTableMenu(int containerId, Inventory inv, ContainerLevelAccess access) {
        super(ModMenuTypes.ENCHANTMENT_LIMIT_TABLE_MENU.get(), containerId);
        this.access = access;
        this.level = inv.player.level();
        this.player = inv.player;

        this.addDataSlot(syncedXpLevels);

        addPlayerInventory(inv);
        addPlayerHotbar(inv);

        // Слот для предмета с зачарованиями (сверху слева)
        enchantedItemSlot = new Slot(enchantedItemContainer, 0, 8, 9) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                // Запрещаем книги и зачарованные книги
                if (stack.is(Items.BOOK) || stack.is(Items.ENCHANTED_BOOK)) return false;
                return !stack.isEmpty() && !EnchantmentHelper.getEnchantments(stack).isEmpty();
            }
        };

        // Слот для кристалла маны (снизу слева)
        crystalSlot = new Slot(crystalContainer, 0, 8, 32) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return stack.getItem() instanceof MagicCrystal;
            }
        };

        // Слот для ShardArtifact (44 пикселя выше кристалла)
        shardSlot = new Slot(shardContainer, 0, 8, 55) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return stack.getItem() instanceof net.artur.nacikmod.item.ShardArtifact;
            }
        };

        // Выходной слот (справа по центру)
        resultSlot = new Slot(resultContainer, 0, 152, 32) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return false; // Только для вывода
            }

            @Override
            public boolean mayPickup(Player player) {
                // Нельзя забирать результат, если не хватает уровней опыта
                return player.experienceLevel >= requiredXpLevels && super.mayPickup(player);
            }

            @Override
            public void onTake(Player player, ItemStack stack) {
                // Тратим уровни опыта, ману, ShardArtifact и уменьшаем исходный предмет
                ItemStack enchantedItem = enchantedItemSlot.getItem();
                ItemStack crystalItem = crystalSlot.getItem();
                ItemStack shardItem = shardSlot.getItem();

                if (!enchantedItem.isEmpty() && !crystalItem.isEmpty()) {
                    // Списываем уровни опыта
                    if (requiredXpLevels > 0 && player.experienceLevel >= requiredXpLevels) {
                        player.giveExperienceLevels(-requiredXpLevels);
                    }

                    // Тратим ману
                    int storedMana = MagicCrystal.getStoredMana(crystalItem);
                    MagicCrystal.setStoredMana(crystalItem, storedMana - MANA_COST);

                    // Проверяем, нужен ли ShardArtifact для выбранных зачарований
                    boolean needsShard = false;
                    boolean hasSelectedEnchantments = false;
                    for (Map.Entry<Enchantment, Boolean> entry : selectedEnchantments.entrySet()) {
                        if (entry.getValue()) { // Если зачарование выбрано
                            hasSelectedEnchantments = true;
                            Enchantment enchantment = entry.getKey();
                            int currentLevel = availableEnchantments.get(enchantment);
                            int maxLevel = enchantment.getMaxLevel();

                            if (maxLevel > 1 && currentLevel >= maxLevel) {
                                needsShard = true;
                                break;
                            }
                        }
                    }

                    // Если нет выбранных зачарований, не тратим ресурсы
                    if (!hasSelectedEnchantments) {
                        return;
                    }

                    // Тратим ShardArtifact если нужен
                    if (needsShard && !shardItem.isEmpty()) {
                        shardItem.shrink(1);
                        shardSlot.setChanged();
                    }

                    // Уменьшаем количество в исходном слоте
                    enchantedItem.shrink(1);

                    // Обновляем слоты
                    enchantedItemSlot.setChanged();
                    crystalSlot.setChanged();
                }

                super.onTake(player, stack);
            }
        };

        this.addSlot(enchantedItemSlot);
        this.addSlot(crystalSlot);
        this.addSlot(shardSlot);
        this.addSlot(resultSlot);

        // Инициализируем список доступных зачарований
        updateAvailableEnchantments();
    }

    @Override
    public void slotsChanged(Container container) {
        super.slotsChanged(container);
        updateAvailableEnchantments();
        setupResultSlot();
    }

    private void updateAvailableEnchantments() {
        availableEnchantments.clear();
        selectedEnchantments.clear();

        ItemStack enchantedItem = enchantedItemSlot.getItem();

        if (!enchantedItem.isEmpty() && !enchantedItem.is(Items.BOOK) && !enchantedItem.is(Items.ENCHANTED_BOOK)) {
            var enchantments = EnchantmentHelper.getEnchantments(enchantedItem);

            for (var entry : enchantments.entrySet()) {
                var enchantment = entry.getKey();
                int currentLevel = entry.getValue();
                int maxLevel = enchantment.getMaxLevel();

                // Проверяем, можно ли улучшить зачарование
                if (maxLevel > 1 && currentLevel < maxLevel + 2) {
                    availableEnchantments.put(enchantment, currentLevel);
                    selectedEnchantments.put(enchantment, false); // По умолчанию не выбрано
                }
            }
        }
    }

    // Методы для работы с выбранными зачарованиями
    public Map<Enchantment, Integer> getAvailableEnchantments() {
        return new HashMap<>(availableEnchantments);
    }

    public boolean isEnchantmentSelected(Enchantment enchantment) {
        return selectedEnchantments.getOrDefault(enchantment, false);
    }

    public void toggleEnchantmentSelection(Enchantment enchantment) {
        if (availableEnchantments.containsKey(enchantment)) {
            selectedEnchantments.put(enchantment, !selectedEnchantments.getOrDefault(enchantment, false));
            setupResultSlot(); // Пересчитываем результат
        }
    }

    public void selectAllEnchantments() {
        for (Enchantment enchantment : availableEnchantments.keySet()) {
            selectedEnchantments.put(enchantment, true);
        }
        setupResultSlot();
    }

    public void deselectAllEnchantments() {
        for (Enchantment enchantment : availableEnchantments.keySet()) {
            selectedEnchantments.put(enchantment, false);
        }
        setupResultSlot();
    }

    public int getSelectedEnchantmentCount() {
        return (int) selectedEnchantments.values().stream().filter(selected -> selected).count();
    }

    private void setupResultSlot() {
        ItemStack enchantedItem = enchantedItemSlot.getItem();
        ItemStack crystalItem = crystalSlot.getItem();
        ItemStack shardItem = shardSlot.getItem();
        ItemStack resultStack = ItemStack.EMPTY;

        requiredXpLevels = 0;

        if (!enchantedItem.isEmpty() && !crystalItem.isEmpty()) {
            // Проверяем, что в кристалле достаточно маны
            int storedMana = MagicCrystal.getStoredMana(crystalItem);

            if (storedMana >= MANA_COST) {
                // Проверяем, что предмет имеет зачарования и это не книга
                if (enchantedItem.is(Items.BOOK) || enchantedItem.is(Items.ENCHANTED_BOOK)) {
                    this.resultSlot.set(ItemStack.EMPTY);
                    return;
                }

                // Считаем стоимость только для выбранных зачарований
                int upgradableCount = 0;
                for (Map.Entry<Enchantment, Boolean> entry : selectedEnchantments.entrySet()) {
                    if (entry.getValue()) { // Если зачарование выбрано
                        Enchantment enchantment = entry.getKey();
                        int currentLevel = availableEnchantments.get(enchantment);
                        int maxLevel = enchantment.getMaxLevel();

                        if (maxLevel > 1) {
                            if (currentLevel < maxLevel) {
                                upgradableCount++;
                            } else if (currentLevel < maxLevel + 2) {
                                if (!shardItem.isEmpty()) {
                                    upgradableCount++;
                                }
                            }
                        }
                    }
                }

                requiredXpLevels = upgradableCount * XP_LEVEL_COST_PER_ENCHANT;

                if (upgradableCount > 0) {
                    // Дополнительно проверяем уровни опыта: если не хватает — не показываем результат
                    if (player.experienceLevel < requiredXpLevels) {
                        resultStack = ItemStack.EMPTY;
                    } else {
                        // Повышаем уровень только выбранных зачарований
                        ItemStack upgradedItem = enchantedItem.copy();
                        boolean success = upgradeSelectedEnchantments(upgradedItem);

                        if (success) {
                            resultStack = upgradedItem;
                        }
                    }
                } else {
                    // No enchantments can be upgraded
                }
            }
        }

        if (!ItemStack.matches(resultStack, this.resultSlot.getItem())) {
            this.resultSlot.set(resultStack);
        }
        syncedXpLevels.set(requiredXpLevels);

    }

    @Override
    public void broadcastChanges() {
        super.broadcastChanges();
        syncedXpLevels.set(requiredXpLevels); // 🔹 всегда обновляем перед отправкой клиенту
    }


    @Override
    public boolean stillValid(Player player) {
        return this.access.evaluate((level, blockPos) -> {
            return player.distanceToSqr((double) blockPos.getX() + 0.5D, (double) blockPos.getY() + 0.5D, (double) blockPos.getZ() + 0.5D) <= 64.0D;
        }, true);
    }

    @Override
    public ItemStack quickMoveStack(Player playerIn, int index) {
        Slot sourceSlot = slots.get(index);
        if (sourceSlot == null || !sourceSlot.hasItem()) return ItemStack.EMPTY;

        ItemStack sourceStack = sourceSlot.getItem();
        ItemStack copyOfSourceStack = sourceStack.copy();

        // Проверяем, является ли слот одним из слотов инвентаря игрока
        if (index < 36) {
            // Это слот инвентаря игрока, пытаемся переместить в слоты блока
            if (!moveItemStackTo(sourceStack, 36, 39, false)) {
                return ItemStack.EMPTY;
            }
        } else if (index < 39) {
            // Это слот блока, пытаемся переместить в инвентарь игрока
            if (!moveItemStackTo(sourceStack, 0, 36, true)) {
                return ItemStack.EMPTY;
            }
        } else {
            return ItemStack.EMPTY;
        }

        if (sourceStack.getCount() == 0) {
            sourceSlot.set(ItemStack.EMPTY);
        } else {
            sourceSlot.setChanged();
        }

        sourceSlot.onTake(playerIn, sourceStack);
        return copyOfSourceStack;
    }

    private void addPlayerInventory(Inventory playerInventory) {
        for (int i = 0; i < 3; ++i) {
            for (int l = 0; l < 9; ++l) {
                this.addSlot(new Slot(playerInventory, l + i * 9 + 9, 8 + l * 18, 84 + i * 18));
            }
        }
    }

    private void addPlayerHotbar(Inventory playerInventory) {
        for (int i = 0; i < 9; ++i) {
            this.addSlot(new Slot(playerInventory, i, 8 + i * 18, 142));
        }
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        this.access.execute((level, pos) -> {
            this.clearContainer(player, this.enchantedItemContainer);
            this.clearContainer(player, this.crystalContainer);
            this.clearContainer(player, this.shardContainer);
            this.clearContainer(player, this.resultContainer);
        });
    }

    // Методы для работы с контейнерами
    public Container getEnchantedItemContainer() {
        return enchantedItemContainer;
    }

    public Container getCrystalContainer() {
        return crystalContainer;
    }

    public Container getShardContainer() {
        return shardContainer;
    }

    public ResultContainer getResultContainer() {
        return resultContainer;
    }

    public Slot getEnchantedItemSlot() {
        return enchantedItemSlot;
    }

    public Slot getCrystalSlot() {
        return crystalSlot;
    }

    public Slot getShardSlot() {
        return shardSlot;
    }

    public Slot getResultSlot() {
        return resultSlot;
    }

    public int getRequiredXpLevels() {
        return syncedXpLevels.get(); // 🔹 теперь клиент получает актуальное значение
    }

    // Обработка повышения зачарований
    private boolean upgradeEnchantments(ItemStack stack) {
        var enchantments = EnchantmentHelper.getEnchantments(stack);
        if (enchantments.isEmpty()) {
            return false;
        }

        boolean upgraded = false;

        // Сначала очищаем все зачарования
        stack.removeTagKey("Enchantments");

        for (var entry : enchantments.entrySet()) {
            var enchantment = entry.getKey();
            int currentLevel = entry.getValue();
            int maxLevel = enchantment.getMaxLevel();
            int allowedMaxLevel = maxLevel + 2; // Разрешаем +2 от максимума

            // Проверяем, можно ли повысить зачарование
            if (maxLevel == 1) {
                // Зачарования с максимумом 1 нельзя повысить
                stack.enchant(enchantment, currentLevel);
            } else if (currentLevel < maxLevel) {
                // Стандартное улучшение (не нужен ShardArtifact)
                stack.enchant(enchantment, currentLevel + 1);
                upgraded = true;
            } else if (currentLevel < allowedMaxLevel) {
                // Улучшение выше максимума (нужен ShardArtifact)
                // Проверяем, есть ли ShardArtifact в слоте
                if (!shardSlot.getItem().isEmpty()) {
                    stack.enchant(enchantment, currentLevel + 1);
                    upgraded = true;
                } else {
                    // ShardArtifact отсутствует - сохраняем текущий уровень
                    stack.enchant(enchantment, currentLevel);
                }
            } else {
                // Сохраняем текущий уровень
                stack.enchant(enchantment, currentLevel);
            }
        }

        if (upgraded) {
            // Проверяем результат
            var newEnchantments = EnchantmentHelper.getEnchantments(stack);
        }

        return upgraded;
    }

    // Обработка повышения выбранных зачарований
    private boolean upgradeSelectedEnchantments(ItemStack stack) {
        boolean upgraded = false;

        // Получаем исходные зачарования
        var originalEnchantments = EnchantmentHelper.getEnchantments(enchantedItemSlot.getItem());

        // Сначала очищаем все зачарования
        stack.removeTagKey("Enchantments");

        // Обрабатываем выбранные зачарования
        for (Map.Entry<Enchantment, Boolean> entry : selectedEnchantments.entrySet()) {
            if (entry.getValue()) { // Если зачарование выбрано
                Enchantment enchantment = entry.getKey();
                int currentLevel = availableEnchantments.get(enchantment);
                int maxLevel = enchantment.getMaxLevel();
                int allowedMaxLevel = maxLevel + 2; // Разрешаем +2 от максимума

                // Проверяем, можно ли повысить зачарование
                if (maxLevel == 1) {
                    // Зачарования с максимумом 1 нельзя повысить
                    stack.enchant(enchantment, currentLevel);
                } else if (currentLevel < maxLevel) {
                    // Стандартное улучшение (не нужен ShardArtifact)
                    stack.enchant(enchantment, currentLevel + 1);
                    upgraded = true;
                } else if (currentLevel < allowedMaxLevel) {
                    // Улучшение выше максимума (нужен ShardArtifact)
                    // Проверяем, есть ли ShardArtifact в слоте
                    if (!shardSlot.getItem().isEmpty()) {
                        stack.enchant(enchantment, currentLevel + 1);
                        upgraded = true;
                    } else {
                        // ShardArtifact отсутствует - сохраняем текущий уровень
                        stack.enchant(enchantment, currentLevel);
                    }
                } else {
                    // Сохраняем текущий уровень
                    stack.enchant(enchantment, currentLevel);
                }
            }
        }

        // Сохраняем невыбранные зачарования без изменений
        for (Map.Entry<Enchantment, Integer> entry : originalEnchantments.entrySet()) {
            Enchantment enchantment = entry.getKey();
            int currentLevel = entry.getValue();

            // Если зачарование не в списке доступных или не выбрано, сохраняем как есть
            if (!availableEnchantments.containsKey(enchantment) || !selectedEnchantments.getOrDefault(enchantment, false)) {
                stack.enchant(enchantment, currentLevel);
            }
        }

        return upgraded;
    }
}
