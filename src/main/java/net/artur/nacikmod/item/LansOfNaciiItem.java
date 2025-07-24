package net.artur.nacikmod.item;

import net.artur.nacikmod.registry.ModItems;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.*;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.ForgeMod;
import com.google.common.collect.Multimap;
import com.google.common.collect.ImmutableMultimap;
import net.artur.nacikmod.registry.ModEffects;

import java.util.List;
import java.util.UUID;
import net.minecraft.world.InteractionHand;
import org.jetbrains.annotations.Nullable;


public class LansOfNaciiItem extends SwordItem {
    private static final UUID ENTITY_REACH_MODIFIER_ID = UUID.fromString("9f2b5c42-38ff-4eb1-a79c-2c2b59a2efdf");
    private final Multimap<Attribute, AttributeModifier> attributeModifiers;

    public LansOfNaciiItem() {
        super(new CustomTier(), 7, -2.9f, new Item.Properties().fireResistant().rarity(ShardArtifact.RED));

        // Создаём список атрибутов (увеличиваем ДАЛЬНОСТЬ удара по сущностям)
        ImmutableMultimap.Builder<Attribute, AttributeModifier> builder = ImmutableMultimap.builder();
        builder.put(ForgeMod.ENTITY_REACH.get(),
                new AttributeModifier(ENTITY_REACH_MODIFIER_ID, "Entity reach", 1.0, AttributeModifier.Operation.ADDITION)); // +2 блока дальности
        builder.put(Attributes.ATTACK_DAMAGE,
                new AttributeModifier(BASE_ATTACK_DAMAGE_UUID, "Weapon damage", 24.0, AttributeModifier.Operation.ADDITION));
        builder.put(Attributes.ATTACK_SPEED,
                new AttributeModifier(BASE_ATTACK_SPEED_UUID, "Weapon speed", -3.0, AttributeModifier.Operation.ADDITION));

        this.attributeModifiers = builder.build();
    }

    @Override
    public Multimap<Attribute, AttributeModifier> getDefaultAttributeModifiers(EquipmentSlot slot) {
        if (slot == EquipmentSlot.MAINHAND) {
            return attributeModifiers;
        }
        return super.getDefaultAttributeModifiers(slot);
    }

    // Накладываем эффекты при атаке и чередуем оружие
    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        if (!target.level().isClientSide) {
            boolean success = super.hurtEnemy(stack, target, attacker);
            if (!success) return false;

            double damageDealt = target.getMaxHealth() - target.getHealth(); // Разница ХП после удара
            int amplifier = (int) Math.floor(damageDealt / 3); // Усиление эффекта

            if (amplifier > 0) {
                target.addEffect(new MobEffectInstance(ModEffects.HEALTH_REDUCTION.get(), 20000, amplifier));
            }
            target.addEffect(new MobEffectInstance(ModEffects.NO_REGEN.get(), 180, 0));

            if (attacker instanceof Player player) {
                ItemStack mainHandItem = player.getMainHandItem();
                ItemStack offHandItem = player.getOffhandItem();
                boolean isMainHand = mainHandItem == stack;
                boolean isOffHand = offHandItem == stack;

                // Проверяем, есть ли в другой руке LansOfProtection
                boolean hasLansOfProtection = (isMainHand && offHandItem.getItem() instanceof LansOfProtectionItem) ||
                        (isOffHand && mainHandItem.getItem() instanceof LansOfProtectionItem);

                if (hasLansOfProtection) {
                    // Выполняем анимацию удара
                    if (attacker instanceof ServerPlayer serverPlayer) {
                        serverPlayer.swing(isMainHand ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND, true);
                    }

                    // Меняем местами оружие в руках
                    player.setItemInHand(InteractionHand.MAIN_HAND, offHandItem);
                    player.setItemInHand(InteractionHand.OFF_HAND, mainHandItem);

                    // Меняем основную руку
                    player.setMainArm(player.getMainArm() == HumanoidArm.RIGHT ? HumanoidArm.LEFT : HumanoidArm.RIGHT);
                }
            }
        }
        return true;
    }

    private static class CustomTier implements Tier {
        @Override
        public int getUses() {
            return 2300; // Прочность меча
        }

        @Override
        public float getSpeed() {
            return 2.0f; // Скорость атаки
        }

        @Override
        public float getAttackDamageBonus() {
            return 17.0f; // Урон
        }

        @Override
        public int getLevel() {
            return 5; // Уровень прочности (как у незерита)
        }

        @Override
        public int getEnchantmentValue() {
            return 25; // Насколько хорошо зачаровывается
        }

        @Override
        public Ingredient getRepairIngredient() {
            return Ingredient.of(ModItems.SHARD_OF_ARTIFACT.get()); // Ремонт незеритом
        }
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltipComponents, TooltipFlag isAdvanced) {
        super.appendHoverText(stack, level, tooltipComponents, isAdvanced);

        tooltipComponents.add(Component.translatable("item.nacikmod.lans_of_nacii.desc1"));
    }
}
