package net.artur.nacikmod.event;

import net.artur.nacikmod.item.LeonidHelmet;
import net.artur.nacikmod.registry.ModAttributes;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraftforge.event.entity.living.LivingEquipmentChangeEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.UUID;

@Mod.EventBusSubscriber
public class LeonidHelmetHandler {
    private static final Logger LOGGER = LogManager.getLogger();
    private static final UUID STRENGTH_MODIFIER_UUID = UUID.fromString("8e1f5ea4-7c38-4d85-9d6d-9f9b9b9b9b9b");
    private static final UUID ARMOR_MODIFIER_UUID = UUID.fromString("8e1f5ea4-7c38-4d85-9d6d-9f9b9b9b9b9c");
    
    private static final AttributeModifier STRENGTH_MODIFIER = new AttributeModifier(
        STRENGTH_MODIFIER_UUID,
        "Leonid Helmet Strength",
        2.0D,
        AttributeModifier.Operation.ADDITION
    );

    private static final AttributeModifier ARMOR_MODIFIER = new AttributeModifier(
        ARMOR_MODIFIER_UUID,
        "Leonid Helmet Bonus Armor",
        4.0D,
        AttributeModifier.Operation.ADDITION
    );

    @SubscribeEvent
    public static void onEquipmentChange(LivingEquipmentChangeEvent event) {
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }

        if (event.getSlot() != EquipmentSlot.HEAD) {
            return;
        }

        if (event.getTo().getItem() instanceof LeonidHelmet) {
            var strengthAttribute = player.getAttribute(Attributes.ATTACK_DAMAGE);
            if (strengthAttribute != null && !strengthAttribute.hasModifier(STRENGTH_MODIFIER)) {
                strengthAttribute.addTransientModifier(STRENGTH_MODIFIER);
            }

            var armorAttribute = player.getAttribute(ModAttributes.BONUS_ARMOR.get());
            if (armorAttribute != null && !armorAttribute.hasModifier(ARMOR_MODIFIER)) {
                armorAttribute.addTransientModifier(ARMOR_MODIFIER);
            }
        } else if (event.getFrom().getItem() instanceof LeonidHelmet) {
            var strengthAttribute = player.getAttribute(Attributes.ATTACK_DAMAGE);
            if (strengthAttribute != null) {
                strengthAttribute.removeModifier(STRENGTH_MODIFIER);
            }

            var armorAttribute = player.getAttribute(ModAttributes.BONUS_ARMOR.get());
            if (armorAttribute != null) {
                armorAttribute.removeModifier(ARMOR_MODIFIER);
            }
        }
    }
} 