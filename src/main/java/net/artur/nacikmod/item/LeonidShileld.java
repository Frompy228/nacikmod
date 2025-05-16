package net.artur.nacikmod.item;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ShieldItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.InteractionHand;
import net.minecraftforge.event.entity.living.ShieldBlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import java.util.UUID;

@Mod.EventBusSubscriber
public class LeonidShileld extends ShieldItem {
    private static final UUID SPEED_MODIFIER_UUID = UUID.fromString("8e1f5ea1-2c3a-4d5e-9f6a-7b8c9d0e1f2a");
    
    public LeonidShileld(Item.Properties properties) {
        super(properties.durability(336).rarity(ShardArtifact.RED));
    }

    @Override
    public boolean isValidRepairItem(ItemStack toRepair, ItemStack repair) {
        return repair.getItem() instanceof ShieldItem;
    }

    @SubscribeEvent
    public static void onShieldBlock(ShieldBlockEvent event) {
        LivingEntity entity = event.getEntity();
        if (entity instanceof Player player) {
            // Проверяем щит в обеих руках
            if (player.getMainHandItem().getItem() instanceof LeonidShileld || 
                player.getOffhandItem().getItem() instanceof LeonidShileld) {
                
                LivingEntity attacker = event.getDamageSource().getEntity() instanceof LivingEntity ? 
                    (LivingEntity) event.getDamageSource().getEntity() : null;
                
                if (attacker != null) {
                    float reflectedDamage = event.getBlockedDamage() * 0.4f;
                    attacker.hurt(player.damageSources().playerAttack(player), reflectedDamage);
                }
            }
        }
    }
}
