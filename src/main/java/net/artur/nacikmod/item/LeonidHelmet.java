package net.artur.nacikmod.item;

import net.artur.nacikmod.client.renderer.LeonidHelmetRenderer;
import net.artur.nacikmod.registry.ModEffects;
import net.artur.nacikmod.registry.ModSounds;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.*;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import net.minecraft.world.phys.AABB;
import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.common.extensions.IForgeItem;

import java.util.List;
import java.util.function.Consumer;

public class LeonidHelmet extends ArmorItem implements IForgeItem {
    private static final String COOLDOWN_TAG = "RoarCooldown";
    private static final int COOLDOWN_TICKS = 1200; // 60 seconds * 20 ticks

    public LeonidHelmet(ArmorMaterial material, Item.Properties properties) {
        super(material, Type.HELMET, new Item.Properties().fireResistant().rarity(ShardArtifact.RED));
    }

    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new LeonidHelmetRenderer());
    }

    @Override
    public String getArmorTexture(ItemStack stack, net.minecraft.world.entity.Entity entity, EquipmentSlot slot, String type) {
        return "nacikmod:textures/models/armor/leonid_helmet.png";
    }

    @Override
    public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(net.minecraft.network.chat.Component.translatable("item.nacikmod.leonid_helmet.desc1"));
    }

    @Override
    public void onArmorTick(ItemStack stack, Level level, net.minecraft.world.entity.player.Player player) {
        if (!level.isClientSide) {
            CompoundTag tag = stack.getOrCreateTag();
            int cooldown = tag.getInt(COOLDOWN_TAG);
            
            if (cooldown > 0) {
                tag.putInt(COOLDOWN_TAG, cooldown - 1);
            } else {
                float healthPercentage = player.getHealth() / player.getMaxHealth();
                
                if (healthPercentage <= 0.4f) {
                    // Get all entities in 5 block radius
                    AABB area = new AABB(
                        player.getX() - 5, player.getY() - 5, player.getZ() - 5,
                        player.getX() + 5, player.getY() + 5, player.getZ() + 5
                    );
                    
                    List<LivingEntity> entities = level.getEntitiesOfClass(LivingEntity.class, area);
                    
                    for (LivingEntity target : entities) {
                        if (target != player) {
                            target.addEffect(new MobEffectInstance(ModEffects.ROAR.get(), 45, 0,false,false));
                        }
                    }
                    
                    // Play roar sound
                    level.playSound(null, player.getX(), player.getY(), player.getZ(),
                        ModSounds.ROAR.get(), SoundSource.PLAYERS, 1.0f, 1.0f);
                    
                    // Set cooldown
                    tag.putInt(COOLDOWN_TAG, COOLDOWN_TICKS);
                }
            }
        }
    }
}