package net.artur.nacikmod.capability.mana;

public interface IMana {
    int getMana();          // Получить текущее количество маны
    void setMana(int mana); // Установить ману
    void addMana(int amount);  // Увеличить ману
    void consumeMana(int amount); // Потратить ману
    int getMaxMana();       // Получить максимальную ману

    void setMaxMana(int maxMana); // Установить максимальную ману
}

