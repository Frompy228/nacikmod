package net.artur.nacikmod.enchantment;

import net.artur.nacikmod.registry.ModAttributes;
import net.artur.nacikmod.capability.mana.ManaProvider;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import org.jetbrains.annotations.NotNull;

public class MagicBurnEnchantment extends Enchantment {

    public MagicBurnEnchantment(Rarity rarity, EquipmentSlot... slots) {
        super(rarity, EnchantmentCategory.WEAPON, slots);
    }

    @Override
    public int getMaxLevel() {
        return 1;
    }

    @Override
    public int getMinCost(int level) {
        return 20;
    }

    @Override
    public int getMaxCost(int level) {
        return 40;
    }

    @Override
    public boolean isDiscoverable() {
        return false;
    }

    @Override
    public boolean isTradeable() {
        return false;
    }

    @Override
    public boolean isAllowedOnBooks() {
        return false;
    }

    @Override
    public void doPostAttack(@NotNull LivingEntity attacker, @NotNull net.minecraft.world.entity.Entity target, int level) {
        if (!(target instanceof LivingEntity livingTarget) || attacker.level().isClientSide) {
            return;
        }

        float attackMultiplier = getAttackStrengthMultiplier(attacker);
        float baseDamage = estimateBaseDamage(attacker) * attackMultiplier;
        float extraDamage = computeBonusDamage(livingTarget);
        if (attackMultiplier < 1.0F) {
            extraDamage *= attackMultiplier;
        }

        if (extraDamage > 0.0F) {
            DamageSource source = attacker instanceof Player player
                    ? attacker.damageSources().playerAttack(player)
                    : attacker.damageSources().mobAttack(attacker);
            livingTarget.hurt(source, extraDamage);
        }

        float damageReference = Math.max(baseDamage + extraDamage, 0.0F);
        burnMana(livingTarget, damageReference);
    }

    private float computeBonusDamage(LivingEntity target) {
        AttributeInstance attribute = target.getAttribute(ModAttributes.BONUS_ARMOR.get());
        if (attribute == null) {
            return 0.0F;
        }
        double bonusArmor = attribute.getValue();
        return (float) (bonusArmor * 0.5D);
    }

    private float getAttackStrengthMultiplier(LivingEntity attacker) {
        if (attacker instanceof Player player) {
            float strength = player.getAttackStrengthScale(0.0F);
            return 0.2F + strength * strength * 0.8F;
        }
        return 1.0F;
    }

    private float estimateBaseDamage(LivingEntity attacker) {
        return attacker instanceof Player
                ? (float) attacker.getAttributeValue(Attributes.ATTACK_DAMAGE)
                : 4.0F;
    }

    private void burnMana(LivingEntity target, float damageReference) {
        int manaToBurn = Mth.clamp((int) Math.ceil(damageReference * 5.0F), 0, Integer.MAX_VALUE);
        if (manaToBurn <= 0) {
            return;
        }
        target.getCapability(ManaProvider.MANA_CAPABILITY).ifPresent(mana -> {
            int burned = Math.min(mana.getMana(), manaToBurn);
            if (burned > 0) {
                mana.removeMana(burned);
            }
        });
    }
}

