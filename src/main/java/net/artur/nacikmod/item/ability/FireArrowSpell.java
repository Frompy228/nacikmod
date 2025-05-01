package net.artur.nacikmod.item.ability;

import net.artur.nacikmod.capability.mana.IMana;
import net.artur.nacikmod.capability.mana.ManaProvider;
import net.artur.nacikmod.entity.projectiles.FireArrowEntity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

public class FireArrowSpell {
    private static final int BASE_MANA_COST = 100;
    private static final float BASE_DAMAGE = 5.0f;
    private static final float DAMAGE_SCALING = 2.5f;

    public static boolean cast(LivingEntity caster, Level level) {
        if (!level.isClientSide && caster instanceof net.minecraft.world.entity.player.Player player) {
            IMana mana = player.getCapability(ManaProvider.MANA_CAPABILITY).orElse(null);
            if (mana != null) {
                int maxMana = mana.getMaxMana();
                int manaCost = BASE_MANA_COST;
                
                // Check if player has enough mana
                if (mana.getMana() >= manaCost) {
                    // Calculate damage based on max mana
                    float damage = BASE_DAMAGE + (maxMana / 100.0f) * DAMAGE_SCALING;
                    
                    // Create and shoot the arrow
                    FireArrowEntity fireArrow = new FireArrowEntity(level, caster);
                    fireArrow.setDamage(damage);
                    
                    // Calculate direction based on where the player is looking
                    double x = caster.getLookAngle().x;
                    double y = caster.getLookAngle().y;
                    double z = caster.getLookAngle().z;
                    
                    // Shoot the arrow
                    fireArrow.shoot(x, y, z, 1.0f, 0.0f);
                    level.addFreshEntity(fireArrow);
                    
                    // Remove mana cost
                    mana.removeMana(manaCost);
                    return true;
                }
            }
        }
        return false;
    }
}