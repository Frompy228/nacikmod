package net.artur.nacikmod.item.ability;

import net.artur.nacikmod.capability.mana.IMana;
import net.artur.nacikmod.capability.mana.ManaProvider;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.util.LazyOptional;

import java.util.UUID;

public class HiraishinAbility {
    private static final int TELEPORT_MANA_COST = 500;
    private static final int SLOT_COUNT = 3;
    private static final String MARKED_ENTITIES_TAG = "MarkedEntities";
    private static final String SELECTED_SLOT_TAG = "SelectedSlot";
    
    // Effect durations in ticks (20 ticks = 1 second)
    private static final int BLINDNESS_DURATION = 40; // 2 seconds
    private static final int WITHER_DURATION = 100;   // 5 seconds
    private static final int INVISIBILITY_DURATION = 40; // 2 seconds

    public static class MarkedEntity {
        private final UUID entityUUID;
        private final String entityName;

        public MarkedEntity(LivingEntity entity) {
            this.entityUUID = entity.getUUID();
            this.entityName = entity instanceof Player player ?
                    player.getGameProfile().getName() :
                    entity.getName().getString();
        }

        public MarkedEntity(UUID uuid, String name) {
            this.entityUUID = uuid;
            this.entityName = name;
        }

        public UUID getEntityUUID() { return entityUUID; }
        public String getEntityName() { return entityName; }
    }

    public static void markEntity(LivingEntity target, Player player, ItemStack stack) {
        if (!(player instanceof ServerPlayer)) return;
        
        CompoundTag tag = stack.getOrCreateTag();
        int slot = getSelectedSlot(tag);
        
        CompoundTag entityTag = new CompoundTag();
        entityTag.putUUID("uuid", target.getUUID());
        entityTag.putString("name", target instanceof Player targetPlayer ?
                targetPlayer.getGameProfile().getName() :
                target.getName().getString());
        
        ListTag slots = tag.getList(MARKED_ENTITIES_TAG, Tag.TAG_COMPOUND);
        while (slots.size() <= slot) {
            slots.add(new CompoundTag());
        }
        slots.set(slot, entityTag);
        tag.put(MARKED_ENTITIES_TAG, slots);
        
        player.sendSystemMessage(Component.literal("Marked " + target.getName().getString() + " in slot " + (slot + 1))
                .withStyle(ChatFormatting.GREEN));
    }

    private static void displayMarkedEntitiesList(Player player, ItemStack stack) {
        if (!(player instanceof ServerPlayer serverPlayer)) return;
        
        CompoundTag tag = stack.getTag();
        if (tag == null) return;
        
        int selectedSlot = getSelectedSlot(tag);
        ListTag slots = tag.getList(MARKED_ENTITIES_TAG, Tag.TAG_COMPOUND);
        boolean needsUpdate = false;
        
        player.sendSystemMessage(Component.literal("Marked entities:")
                .withStyle(ChatFormatting.GREEN));

        for (int i = 0; i < SLOT_COUNT; i++) {
            String prefix = i == selectedSlot ? "> " : "  ";
            String name = "Empty";
            
            if (i < slots.size()) {
                CompoundTag entityTag = slots.getCompound(i);
                if (entityTag.contains("uuid") && entityTag.contains("name")) {
                    UUID entityUUID = entityTag.getUUID("uuid");
                    boolean entityExists = false;
                    
                    // Check if entity exists in any dimension
                    for (ServerLevel level : serverPlayer.getServer().getAllLevels()) {
                        Entity entity = level.getEntity(entityUUID);
                        if (entity != null && entity instanceof LivingEntity livingEntity && livingEntity.isAlive()) {
                            entityExists = true;
                            break;
                        }
                    }
                    
                    if (entityExists) {
                        name = entityTag.getString("name");
                    } else {
                        // Entity no longer exists, clear the slot
                        slots.set(i, new CompoundTag());
                        needsUpdate = true;
                    }
                }
            }
            
            player.sendSystemMessage(Component.literal(prefix + (i + 1) + ". " + name)
                    .withStyle(i == selectedSlot ? ChatFormatting.YELLOW : ChatFormatting.WHITE));
        }
        
        // Update the NBT if any slots were cleared
        if (needsUpdate) {
            tag.put(MARKED_ENTITIES_TAG, slots);
        }
    }

    private static int getSelectedSlot(CompoundTag tag) {
        return tag.contains(SELECTED_SLOT_TAG) ? tag.getInt(SELECTED_SLOT_TAG) : 0;
    }

    private static void setSelectedSlot(CompoundTag tag, int slot) {
        if (slot >= 0 && slot < SLOT_COUNT) {
            tag.putInt(SELECTED_SLOT_TAG, slot);
        }
    }

    private static MarkedEntity getMarkedEntity(CompoundTag tag, int slot) {
        if (tag == null || slot < 0 || slot >= SLOT_COUNT) return null;
        
        ListTag slots = tag.getList(MARKED_ENTITIES_TAG, Tag.TAG_COMPOUND);
        if (slot >= slots.size()) return null;
        
        CompoundTag entityTag = slots.getCompound(slot);
        if (!entityTag.contains("uuid") || !entityTag.contains("name")) return null;
        
        return new MarkedEntity(
            entityTag.getUUID("uuid"),
            entityTag.getString("name")
        );
    }

    private static void clearSlot(CompoundTag tag, int slot) {
        if (tag == null || slot < 0 || slot >= SLOT_COUNT) return;
        
        ListTag slots = tag.getList(MARKED_ENTITIES_TAG, Tag.TAG_COMPOUND);
        if (slot < slots.size()) {
            slots.set(slot, new CompoundTag());
            tag.put(MARKED_ENTITIES_TAG, slots);
        }
    }

    public static InteractionResultHolder<ItemStack> useAbility(Level level, Player player, InteractionHand hand) {
        if (!level.isClientSide && player instanceof ServerPlayer serverPlayer) {
            ItemStack stack = player.getItemInHand(hand);
            CompoundTag tag = stack.getOrCreateTag();

            // If shift is held, switch between slots
            if (player.isShiftKeyDown()) {
                int currentSlot = getSelectedSlot(tag);
                currentSlot = (currentSlot + 1) % SLOT_COUNT;
                setSelectedSlot(tag, currentSlot);
                displayMarkedEntitiesList(player, stack);
                return InteractionResultHolder.success(stack);
            }

            // Check mana for teleportation
            LazyOptional<IMana> manaCap = player.getCapability(ManaProvider.MANA_CAPABILITY);
            if (!manaCap.isPresent()) {
                player.sendSystemMessage(Component.literal("Mana capability not found!")
                        .withStyle(ChatFormatting.RED));
                return InteractionResultHolder.fail(stack);
            }

            IMana mana = manaCap.orElseThrow(IllegalStateException::new);
            if (mana.getMana() < TELEPORT_MANA_COST) {
                player.sendSystemMessage(Component.literal("Not enough mana!")
                        .withStyle(ChatFormatting.RED));
                return InteractionResultHolder.fail(stack);
            }

            // Get marked entity from selected slot
            MarkedEntity targetEntity = getMarkedEntity(tag, getSelectedSlot(tag));
            if (targetEntity == null) {
                player.sendSystemMessage(Component.literal("No entity marked in selected slot!")
                        .withStyle(ChatFormatting.RED));
                return InteractionResultHolder.fail(stack);
            }

            // Find the entity in all dimensions
            Entity entity = null;
            ServerLevel targetLevel = null;
            
            for (ServerLevel serverLevel : serverPlayer.getServer().getAllLevels()) {
                Entity foundEntity = serverLevel.getEntity(targetEntity.getEntityUUID());
                if (foundEntity != null && foundEntity instanceof LivingEntity livingEntity && livingEntity.isAlive()) {
                    entity = foundEntity;
                    targetLevel = serverLevel;
                    break;
                }
            }

            if (entity == null) {
                player.sendSystemMessage(Component.literal("Marked entity no longer exists!")
                        .withStyle(ChatFormatting.RED));
                clearSlot(tag, getSelectedSlot(tag));
                return InteractionResultHolder.fail(stack);
            }

            // Teleport to the entity's current position
            double x = player.getX();
            double y = player.getY();
            double z = player.getZ();

            // Check if we need to change dimensions
            if (targetLevel != serverPlayer.level()) {
                serverPlayer.teleportTo(
                        targetLevel,
                        entity.getX(),
                        entity.getY(),
                        entity.getZ(),
                        serverPlayer.getYRot(),
                        serverPlayer.getXRot()
                );
            } else {
                serverPlayer.teleportTo(
                        entity.getX(),
                        entity.getY(),
                        entity.getZ()
                );
            }

            level.playSound(null, x, y, z, SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 1.0F, 1.0F);
            level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 1.0F, 1.0F);

            // Apply effects
            if (entity instanceof LivingEntity target) {
                // Apply blindness and wither to the target
                target.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, BLINDNESS_DURATION, 2, false, false));
                target.addEffect(new MobEffectInstance(MobEffects.WITHER, WITHER_DURATION, 10, false, false));
            }
            
            // Apply invisibility to the player
            player.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY, INVISIBILITY_DURATION, 0, false, false));

            // Clear the slot after teleportation
            clearSlot(tag, getSelectedSlot(tag));
            mana.setMana(mana.getMana() - TELEPORT_MANA_COST);

            stack.hurt(1, level.getRandom(), null);
            return InteractionResultHolder.success(stack);
        }
        return InteractionResultHolder.pass(player.getItemInHand(hand));
    }
}