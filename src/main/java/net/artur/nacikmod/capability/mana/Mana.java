package net.artur.nacikmod.capability.mana;

public class Mana implements IMana {
    private int mana;
    private int maxMana;
    private boolean isTrueMage;

    public Mana() {
        this.mana = 100;  // Начальная мана
        this.maxMana = 100;
        this.isTrueMage = false;
    }

    @Override
    public int getMana() {
        return mana;
    }

    @Override
    public void setMana(int amount) {
        this.mana = amount; // Ограничение от 0 до maxMana
    }

    @Override
    public void addMana(int amount) {
        setMana(this.mana + amount);
    }

    @Override
    public void removeMana(int amount) {
        setMana(this.mana - amount);
    }

    @Override
    public int getMaxMana() {
        return maxMana;
    }

    @Override
    public void setMaxMana(int amount) {
        this.maxMana = Math.max(1, amount); // Минимальное значение 1
        setMana(mana); // Обновляем текущую ману, чтобы не превышала максимум
    }

    @Override
    public void addMaxMana(int amount) {
        setMaxMana(this.maxMana + amount);
    }


    public void regenerateMana(int amount) {
        if (this.mana < this.maxMana) {
            this.mana = Math.min(this.mana + amount, this.maxMana);
        }
    }
    
    // Реализация методов для статуса "Истинный маг"
    @Override
    public boolean isTrueMage() {
        return isTrueMage;
    }
    
    @Override
    public void setTrueMage(boolean isTrueMage) {
        this.isTrueMage = isTrueMage;
    }
}
