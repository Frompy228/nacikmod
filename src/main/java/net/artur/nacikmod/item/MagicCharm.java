package net.artur.nacikmod.item;

import net.artur.nacikmod.registry.ModAttributes;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.type.capability.ICurioItem;

import java.util.List;
import java.util.UUID;

public class MagicCharm extends Item implements ICurioItem {
    // Модификаторы для брони
    private static final UUID BONUS_ARMOR_UUID = UUID.fromString("f25c5e74-8f75-4d1d-91d6-3f5a2e8b5c55");
    private static final AttributeModifier BONUS_ARMOR = new AttributeModifier(
            BONUS_ARMOR_UUID,
            "Magic Charm Bonus Armor",
            4.0,
            AttributeModifier.Operation.ADDITION
    );

    // Модификаторы для здоровья
    private static final UUID BONUS_HEALTH_UUID = UUID.fromString("d3f3c7b0-1a1d-4b8e-9f2a-3e5d7c9f0b2a");
    private static final AttributeModifier BONUS_HEALTH = new AttributeModifier(
            BONUS_HEALTH_UUID,
            "Magic Charm Bonus Health",
            10.0, // +5 сердец (1 сердце = 2 здоровья)
            AttributeModifier.Operation.ADDITION
    );

    public MagicCharm() {
        super(new Item.Properties().stacksTo(1).defaultDurability(0));
    }
    @Override
    public void onEquip(SlotContext slotContext, ItemStack prevStack, ItemStack stack) {
        LivingEntity entity = slotContext.entity();
        if (!entity.level().isClientSide) {
            addAttribute(entity, ModAttributes.BONUS_ARMOR.get(), BONUS_ARMOR);
            addAttribute(entity, Attributes.MAX_HEALTH, BONUS_HEALTH);

            // Обновляем текущее здоровье при увеличении максимума
            if(entity.getHealth() > entity.getMaxHealth()) {
                entity.setHealth(entity.getMaxHealth());
            }
        }
    }

    @Override
    public void onUnequip(SlotContext slotContext, ItemStack newStack, ItemStack stack) {
        LivingEntity entity = slotContext.entity();
        if (!entity.level().isClientSide) {
            removeAttribute(entity, ModAttributes.BONUS_ARMOR.get(), BONUS_ARMOR_UUID);
            removeAttribute(entity, Attributes.MAX_HEALTH, BONUS_HEALTH_UUID);

            if (entity.getHealth() > entity.getMaxHealth()) {
                entity.setHealth(entity.getMaxHealth());
            }
        }
    }

    private void addAttribute(LivingEntity entity, Attribute attribute, AttributeModifier modifier) {
        AttributeInstance instance = entity.getAttribute(attribute);
        if (instance != null && !instance.hasModifier(modifier)) {
            instance.addTransientModifier(modifier);
        }
    }

    private void removeAttribute(LivingEntity entity, Attribute attribute, UUID uuid) {
        AttributeInstance instance = entity.getAttribute(attribute);
        if (instance != null && instance.getModifier(uuid) != null) {
            instance.removeModifier(uuid);
        }
    }
    @Override
    public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(net.minecraft.network.chat.Component.translatable("item.nacikmod.magic_charm.desc1"));

    }
}