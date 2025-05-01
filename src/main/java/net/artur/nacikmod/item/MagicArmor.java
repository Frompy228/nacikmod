package net.artur.nacikmod.item;

import com.mojang.blaze3d.shaders.Effect;
import net.artur.nacikmod.registry.ModAttributes;
import net.artur.nacikmod.registry.ModEffects;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffect;
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

public class MagicArmor extends Item implements ICurioItem {
    private static final UUID BONUS_ARMOR_UUID = UUID.fromString("f25c5e74-8f75-4d1d-91d6-3f5a2e8b5c97");
    private static final AttributeModifier BONUS_ARMOR = new AttributeModifier(
            BONUS_ARMOR_UUID, "Magic Armor Bonus Armor", 10.0, AttributeModifier.Operation.ADDITION
    );

    public MagicArmor() {
        super(new Item.Properties().stacksTo(1).defaultDurability(0));
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
        if (entity != null) {
            // Удаляем эффект, если он уже есть
            if (entity.hasEffect(ModEffects.LOVE.get())) {
                entity.removeEffect(ModEffects.LOVE.get());
            }
            if (entity.hasEffect(MobEffects.POISON)){
                entity.removeEffect(MobEffects.POISON);
            }

        }
    }
    
    public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(net.minecraft.network.chat.Component.translatable("item.nacikmod.magic_armor.desc1"));

    }
}
