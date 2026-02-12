package net.artur.nacikmod.capability.mana;

public interface IMana {
    int getMana();
    void setMana(int amount);
    void addMana(int amount);
    void removeMana(int amount);

    int getMaxMana();
    void setMaxMana(int amount);
    void addMaxMana(int amount);
    void regenerateMana(int amount);

    // Статус "Истинный маг"
    boolean isTrueMage();
    void setTrueMage(boolean isTrueMage);

    boolean hasVisionBlessing();
    void setVisionBlessing(boolean hasVisionBlessing);
    boolean isKodaiActive();
    void setKodaiActive(boolean active);
    
    // Blood Bone
    boolean isBloodBoneActive();
    void setBloodBoneActive(boolean active);
}
