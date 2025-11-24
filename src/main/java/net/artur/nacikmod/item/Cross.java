package net.artur.nacikmod.item;

import net.artur.nacikmod.registry.ModAttributes;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.type.capability.ICurioItem;

import java.util.List;
import java.util.UUID;

public class Cross extends Item implements ICurioItem {
    private static final UUID BONUS_ARMOR_UUID = UUID.fromString("a1b2c3d4-e5f6-7890-abcd-ef1234567890");
    private static final AttributeModifier BONUS_ARMOR = new AttributeModifier(
            BONUS_ARMOR_UUID, "Cross Bonus Armor", 10.0, AttributeModifier.Operation.ADDITION
    );

    public Cross(Properties p_41383_) {
        super(p_41383_.stacksTo(1));
    }

    @Override
    public void onEquip(SlotContext slotContext, ItemStack prevStack, ItemStack stack) {
        LivingEntity entity = slotContext.entity();
        if (!entity.level().isClientSide) {
            AttributeInstance attribute = entity.getAttribute(ModAttributes.BONUS_ARMOR.get());
            if (attribute != null && attribute.getModifier(BONUS_ARMOR_UUID) == null) {
                attribute.addTransientModifier(BONUS_ARMOR);
            }
        }
    }

    @Override
    public void onUnequip(SlotContext slotContext, ItemStack newStack, ItemStack stack) {
        LivingEntity entity = slotContext.entity();
        if (!entity.level().isClientSide) {
            AttributeInstance attribute = entity.getAttribute(ModAttributes.BONUS_ARMOR.get());
            if (attribute != null && attribute.getModifier(BONUS_ARMOR_UUID) != null) {
                attribute.removeModifier(BONUS_ARMOR_UUID);
            }
        }
    }

    @Override
    public void curioTick(SlotContext slotContext, ItemStack stack) {
        LivingEntity entity = slotContext.entity();

        if (!entity.level().isClientSide) {
            // Применяем эффект регенерации только если его нет или осталось мало времени
            // Это позволяет эффекту успевать тикать (применять исцеление)
            MobEffectInstance existingEffect = entity.getEffect(MobEffects.REGENERATION);
            if (existingEffect == null || existingEffect.getDuration() < 40) {
                // Применяем эффект только если его нет или осталось меньше 2 секунд
            entity.addEffect(new MobEffectInstance(
                    MobEffects.REGENERATION,
                        100,  // длительность 5 секунд (100 тиков) - достаточно для нескольких тиков
                    1,
                    false,
                    false,
                    false
            ));
            }
        }
    }



    @Override
    public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.nacikmod.cross.desc1"));
    }
}
