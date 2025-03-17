package net.artur.nacikmod.capability.mana;

public interface IMana {
    int getMana(); // Получить текущую ману
    int getMaxMana(); // Получить максимальную ману
    void setMana(int mana); // Установить текущую ману
    void setMaxMana(int maxMana); // Установить максимальную ману
    void addMana(int amount); // Добавить ману
    void consumeMana(int amount); // Потратить ману
}