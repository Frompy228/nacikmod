package net.artur.nacikmod.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.artur.nacikmod.registry.ModAttributes;
import net.artur.nacikmod.capability.mana.ManaProvider;
import net.minecraftforge.common.ForgeMod;
import net.minecraft.world.effect.MobEffects;
import net.artur.nacikmod.registry.ModEffects;

import java.util.UUID;

public class EffectManaLastMagic extends MobEffect {
    private static final UUID DAMAGE_MODIFIER_UUID = UUID.fromString("1a2b3c4d-5e6f-7a8b-9c0d-1e2f3a4b5c6d");
    private static final UUID ARMOR_MODIFIER_UUID = UUID.fromString("2b3c4d5e-6f7a-8b9c-0d1e-2f3a4b5c6d7e");
    private static final UUID SPEED_MODIFIER_UUID = UUID.fromString("3c4d5e6f-7a8b-9c0d-1e2f-3a4b5c6d7e8f");
    private static final UUID JUMP_MODIFIER_UUID = UUID.fromString("4d5e6f7a-8b9c-0d1e-2f3a-4b5c6d7e8f9a");
    private static final UUID ENTITY_REACH_UUID = UUID.fromString("5e6f7a8b-9c0d-1e2f-3a4b-5c6d7e8f9a0b");
    private static final UUID BLOCK_REACH_UUID = UUID.fromString("6f7a8b9c-0d1e-2f3a-4b5c-6d7e8f9a0b1c");
    private static final UUID MAX_HEALTH_UUID = UUID.fromString("7a8b9c0d-1e2f-3a4b-5c6d-7e8f9a0b1c2d");
    private static final UUID ARMOR_TOUGHNESS_UUID = UUID.fromString("8b9c0d1e-2f3a-4b5c-6d7e-8f9a0b1c2d3e");
    private static final UUID SWIM_SPEED_UUID = UUID.fromString("9c0d1e2f-3a4b-5c6d-7e8f-9a0b1c2d3e4f");

    public EffectManaLastMagic() {
        super(MobEffectCategory.BENEFICIAL, 0x000000);
        
        // Добавляем модификаторы атрибутов в конструкторе
        this.addAttributeModifier(Attributes.ATTACK_DAMAGE, DAMAGE_MODIFIER_UUID.toString(),
                15.0, AttributeModifier.Operation.ADDITION);
        this.addAttributeModifier(ModAttributes.BONUS_ARMOR.get(), ARMOR_MODIFIER_UUID.toString(),
                25.0, AttributeModifier.Operation.ADDITION);
        this.addAttributeModifier(Attributes.MOVEMENT_SPEED, SPEED_MODIFIER_UUID.toString(),
                0.2, AttributeModifier.Operation.ADDITION);
        this.addAttributeModifier(Attributes.JUMP_STRENGTH, JUMP_MODIFIER_UUID.toString(),
                1, AttributeModifier.Operation.ADDITION);
        // Добавляем модификаторы досягаемости
        this.addAttributeModifier(ForgeMod.ENTITY_REACH.get(), ENTITY_REACH_UUID.toString(),
                3.0, AttributeModifier.Operation.ADDITION);
        this.addAttributeModifier(ForgeMod.BLOCK_REACH.get(), BLOCK_REACH_UUID.toString(),
                3.0, AttributeModifier.Operation.ADDITION);
        // Добавляем модификаторы здоровья и брони
        this.addAttributeModifier(Attributes.MAX_HEALTH, MAX_HEALTH_UUID.toString(),
                250.0, AttributeModifier.Operation.ADDITION);
        this.addAttributeModifier(Attributes.ARMOR, ARMOR_MODIFIER_UUID.toString(),
                20.0, AttributeModifier.Operation.ADDITION);
        this.addAttributeModifier(Attributes.ARMOR_TOUGHNESS, ARMOR_TOUGHNESS_UUID.toString(),
                20.0, AttributeModifier.Operation.ADDITION);
        // Добавляем модификатор скорости плавания
        this.addAttributeModifier(ForgeMod.SWIM_SPEED.get(), SWIM_SPEED_UUID.toString(),
                1.0, AttributeModifier.Operation.MULTIPLY_TOTAL);
    }

    @Override
    public void applyEffectTick(LivingEntity entity, int amplifier) {
        if (!entity.level().isClientSide && entity.isAlive() && entity instanceof Player player) {
            player.getCapability(ManaProvider.MANA_CAPABILITY).ifPresent(mana -> {
                mana.addMaxMana(500);
                mana.regenerateMana(1000);
            });

            // Добавляем положительные эффекты без частиц
            player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 40, 1, false, false));
            player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 40, 2, false, false));
            player.addEffect(new MobEffectInstance(MobEffects.JUMP, 40, 1, false, false));
            player.addEffect(new MobEffectInstance(MobEffects.DIG_SPEED, 40, 2, false, false)); 
            player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 40, 3, false, false));
            player.addEffect(new MobEffectInstance(MobEffects.SATURATION, 40, 1, false, false));

            // Удаляем все отрицательные эффекты
            player.getActiveEffects().stream()
                .filter(effect -> !effect.getEffect().isBeneficial())
                .forEach(effect -> player.removeEffect(effect.getEffect()));
        }
    }

    @Override
    public void removeAttributeModifiers(LivingEntity entity, net.minecraft.world.entity.ai.attributes.AttributeMap attributes, int amplifier) {
        super.removeAttributeModifiers(entity, attributes, amplifier);

        if (!entity.level().isClientSide && entity instanceof Player player) {
            player.getCapability(ManaProvider.MANA_CAPABILITY).ifPresent(mana -> {
                mana.setMaxMana(0);
                mana.setMana(0);
            });
            player.kill();
        }
    }

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        return duration % 20 == 0; // Эффект применяется каждую секунду
    }
}
