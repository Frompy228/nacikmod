package net.artur.nacikmod.capability.mana;

public class Mana implements IMana {
    private int mana;
    private int maxMana;

    // Конструктор без значений по умолчанию
    public Mana() {
        // Нет инициализации значений, они будут заданы позже
    }

    // Конструктор с максимальной маной
    public Mana(int maxMana) {
        this.maxMana = maxMana;
        this.mana = maxMana;  // Изначально можно установить ману равной максимальной
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
        this.mana = mana;
    }

    @Override
    public void setMaxMana(int maxMana) {
        this.maxMana = maxMana;

    }

    @Override
    public void addMana(int amount) {
        if (mana<maxMana){
        setMana(this.mana + amount); // Добавляем ману, но с проверкой на максимальное значение
    }}

    @Override
    public void consumeMana(int amount) {
        this.mana = Math.max(this.mana - amount, 0); // Уменьшаем ману, но не меньше 0
    }
}
