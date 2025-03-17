package net.artur.nacikmod.capability.mana;

public class Mana implements IMana {
    private int mana;
    private int maxMana;

    public Mana(int maxMana) {
        this.maxMana = maxMana;
        this.mana = maxMana; // Начинаем с полной маны
    }

    @Override
    public int getMana() {
        return mana;
    }

    @Override
    public int getMaxMana() {
        return maxMana;
    }

    @Override
    public void setMana(int mana) {
        this.mana = Math.min(mana, maxMana); // Не превышаем максимум
    }

    @Override
    public void setMaxMana(int maxMana) {
        this.maxMana = maxMana;
        if (mana > maxMana) {
            mana = maxMana; // Убеждаемся, что текущая мана не превышает максимум
        }
    }

    @Override
    public void addMana(int amount) {
        setMana(this.mana + amount);
    }

    @Override
    public void consumeMana(int amount) {
        this.mana = Math.max(this.mana - amount, 0); // Не опускаемся ниже нуля
    }
}