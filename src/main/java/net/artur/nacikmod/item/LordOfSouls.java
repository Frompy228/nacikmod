package net.artur.nacikmod.item;

import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.Level;
import net.artur.nacikmod.capability.mana.ManaProvider;
import net.artur.nacikmod.entity.ai.LordOfSoulsSummonedEntityAI;
import net.artur.nacikmod.util.PlayerCooldowns;
import net.artur.nacikmod.capability.lord_of_souls_summoned_entities.LordOfSoulsSummonedEntityProvider;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;

public class LordOfSouls extends Item {
    private static final String ACTIVE_TAG = "active";
    private static final String SOULS_COUNT_TAG = "souls_count";
    private static final String SOULS_DATA_TAG = "souls_data";
    private static final String ENTITY_TYPE_TAG = "entity_type";
    private static final String ENTITY_UUID_TAG = "entity_uuid";
    private static final String EQUIPMENT_TAG = "equipment";
    private static final String LAST_KILLED_ENTITY_TAG = "last_killed_entity";
    public static final int MANA_COST = 5000;
    private static final int SOULS_PER_KILL = 1;
    private static final int MAX_SOULS = 10;
    private static final int SUMMON_COOLDOWN_TICKS = 20; // 1000 seconds = 20000 ticks
    private static final int SUMMONED_ENTITY_LIFETIME = 6000; // 5 minutes = 6000 ticks

    public LordOfSouls(Properties properties) {
        super(properties.rarity(AncientSeal.ANCIENT_GOLD).stacksTo(1).fireResistant());
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack itemStack = player.getItemInHand(hand);
        
        if (!level.isClientSide) {
            if (player.isShiftKeyDown()) {
                // Check cooldown for summoning
                if (PlayerCooldowns.isOnCooldown(player, this)) {
                    int left = PlayerCooldowns.getCooldownLeft(player, this);
                    player.sendSystemMessage(Component.literal("Summoning is on cooldown! (" + left / 20 + "s left)")
                            .withStyle(ChatFormatting.RED));
                    return InteractionResultHolder.fail(itemStack);
                }
                
                // Release all souls and spawn entities
                releaseAllSouls(level, player, itemStack);
                
                // Set cooldown after successful summoning
                PlayerCooldowns.setCooldown(player, this, SUMMON_COOLDOWN_TICKS);
            } else {
                // Toggle activation
                boolean isActive = isActive(itemStack);
                setActive(itemStack, !isActive);
                
                if (!isActive) {
                    player.sendSystemMessage(Component.literal("Soul absorption activated!")
                            .withStyle(ChatFormatting.GREEN));
                } else {
                    player.sendSystemMessage(Component.literal("Soul absorption deactivated!")
                            .withStyle(ChatFormatting.RED));
                }
            }
            
            // Set cooldown for activation toggle
            player.getCooldowns().addCooldown(this, 20);
        }
        
        return InteractionResultHolder.success(itemStack);
    }

    public static boolean isActive(ItemStack stack) {
        return stack.hasTag() && stack.getTag().getBoolean(ACTIVE_TAG);
    }

    public static void setActive(ItemStack stack, boolean active) {
        stack.getOrCreateTag().putBoolean(ACTIVE_TAG, active);
    }

    public static int getSoulsCount(ItemStack stack) {
        return stack.hasTag() ? stack.getTag().getInt(SOULS_COUNT_TAG) : 0;
    }

    public static void addSoul(ItemStack stack, Entity killedEntity) {
        CompoundTag tag = stack.getOrCreateTag();
        int currentSouls = tag.getInt(SOULS_COUNT_TAG);
        
        // Check if we can add more souls
        if (currentSouls >= MAX_SOULS) {
            return;
        }
        
        tag.putInt(SOULS_COUNT_TAG, currentSouls + SOULS_PER_KILL);
        
        // Store information about the killed entity
        CompoundTag entityInfo = new CompoundTag();
        entityInfo.putString(ENTITY_TYPE_TAG, EntityType.getKey(killedEntity.getType()).toString());
        entityInfo.putUUID(ENTITY_UUID_TAG, killedEntity.getUUID());
        
        // Store equipment if it's a LivingEntity
        if (killedEntity instanceof LivingEntity livingEntity) {
            CompoundTag equipment = new CompoundTag();
            for (EquipmentSlot slot : EquipmentSlot.values()) {
                ItemStack equipmentItem = livingEntity.getItemBySlot(slot);
                if (!equipmentItem.isEmpty()) {
                    equipment.put(slot.getName(), equipmentItem.save(new CompoundTag()));
                }
            }
            if (!equipment.isEmpty()) {
                entityInfo.put(EQUIPMENT_TAG, equipment);
            }
        }
        
        // Store as last killed entity
        tag.put(LAST_KILLED_ENTITY_TAG, entityInfo);
        
        // Add to souls data list
        ListTag soulsData = tag.getList(SOULS_DATA_TAG, 10);
        soulsData.add(entityInfo);
        tag.put(SOULS_DATA_TAG, soulsData);
    }

    public static void removeSoul(ItemStack stack) {
        CompoundTag tag = stack.getOrCreateTag();
        int currentSouls = tag.getInt(SOULS_COUNT_TAG);
        if (currentSouls > 0) {
            tag.putInt(SOULS_COUNT_TAG, currentSouls - 1);
            
            // Remove last killed entity info
            tag.remove(LAST_KILLED_ENTITY_TAG);
            
            // Remove last soul from data list
            ListTag soulsData = tag.getList(SOULS_DATA_TAG, 10);
            if (!soulsData.isEmpty()) {
                soulsData.remove(soulsData.size() - 1);
                tag.put(SOULS_DATA_TAG, soulsData);
            }
        }
    }

    private void releaseAllSouls(Level level, Player player, ItemStack stack) {
        CompoundTag tag = stack.getOrCreateTag();
        ListTag soulsData = tag.getList(SOULS_DATA_TAG, 10);
        
        if (soulsData.isEmpty()) {
            player.sendSystemMessage(Component.literal("No souls to release!")
                    .withStyle(ChatFormatting.YELLOW));
            return;
        }
        
        int spawnedCount = 0;
        for (int i = 0; i < soulsData.size(); i++) {
            CompoundTag entityInfo = soulsData.getCompound(i);
            String entityTypeStr = entityInfo.getString(ENTITY_TYPE_TAG);
            
            try {
                EntityType<?> entityType = EntityType.byString(entityTypeStr).orElse(null);
                if (entityType != null) {
                    Entity entity = entityType.create(level);
                    if (entity instanceof LivingEntity livingEntity) {
                        // Set position near player
                        entity.setPos(player.getX() + (level.random.nextDouble() - 0.5) * 10,
                                    player.getY() + 1,
                                    player.getZ() + (level.random.nextDouble() - 0.5) * 10);
                        
                        // Restore equipment
                        if (entityInfo.contains(EQUIPMENT_TAG)) {
                            CompoundTag equipment = entityInfo.getCompound(EQUIPMENT_TAG);
                            for (EquipmentSlot slot : EquipmentSlot.values()) {
                                if (equipment.contains(slot.getName())) {
                                    ItemStack equipmentItem = ItemStack.of(equipment.getCompound(slot.getName()));
                                    livingEntity.setItemSlot(slot, equipmentItem);
                                }
                            }
                        }
                        
                        // Add custom AI for summoned entities
                        if (livingEntity instanceof Mob mob) {
                            // Clear existing target goals to prevent conflicts
                            mob.targetSelector.removeAllGoals(goal -> true);
                            // Add our custom AI
                            mob.targetSelector.addGoal(1, new LordOfSoulsSummonedEntityAI(mob, player.getUUID()));
                            
                            // Set custom properties for summoned entities
                            mob.setPersistenceRequired(); // Prevent despawning
                            mob.setInvulnerable(false); // Can be killed but won't drop loot (handled by event)
                            
                                                    // Add lifetime data for automatic removal
                        var entityData = livingEntity.getPersistentData();
                        entityData.putInt("lord_of_souls_lifetime", SUMMONED_ENTITY_LIFETIME);
                        entityData.putUUID("lord_of_souls_owner", player.getUUID()); // Backup owner UUID in mob's NBT
                        }
                        
                        // Add to level
                        level.addFreshEntity(entity);
                        
                        // Track the summoned entity using capability
                        player.getCapability(LordOfSoulsSummonedEntityProvider.LORD_OF_SOULS_SUMMONED_ENTITY_CAPABILITY)
                                .ifPresent(cap -> cap.addSummonedEntity(entity.getUUID(), player.getUUID()));
                        
                        spawnedCount++;
                    }
                }
            } catch (Exception e) {
                // Skip invalid entity types
            }
        }
        
        // Clear all souls
        tag.putInt(SOULS_COUNT_TAG, 0);
        tag.put(SOULS_DATA_TAG, new ListTag());
        tag.remove(LAST_KILLED_ENTITY_TAG);
        
        player.sendSystemMessage(Component.literal("Released " + spawnedCount + " souls!")
                .withStyle(ChatFormatting.GREEN));
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltipComponents, TooltipFlag isAdvanced) {
        super.appendHoverText(stack, level, tooltipComponents, isAdvanced);

        int soulsCount = getSoulsCount(stack);
        boolean isActive = isActive(stack);

        tooltipComponents.add(Component.translatable("item.nacikmod.lord_of_souls.desc1"));
        tooltipComponents.add(Component.translatable("item.nacikmod.lord_of_souls.desc2", soulsCount)
                .withStyle(ChatFormatting.DARK_PURPLE));

        
        tooltipComponents.add(Component.translatable("item.nacikmod.lord_of_souls.desc3", MANA_COST)
                .withStyle(style -> style.withColor(0x00FFFF)));

        if (isActive) {
            tooltipComponents.add(Component.translatable("item.active")
                    .withStyle(ChatFormatting.GREEN));
        } else {
            tooltipComponents.add(Component.translatable("item.inactive")
                    .withStyle(ChatFormatting.RED));
        }

        tooltipComponents.add(Component.translatable("item.nacikmod.lord_of_souls.desc4")
                .withStyle(ChatFormatting.GRAY));

        tooltipComponents.add(Component.translatable("item.nacikmod.lord_of_souls.desc5")
                .withStyle(ChatFormatting.GRAY));
    }

    @Override
    public boolean canApplyAtEnchantingTable(ItemStack stack, Enchantment enchantment) {
        return false;
    }

    @Override
    public boolean isEnchantable(ItemStack stack) {
        return false;
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return isActive(stack);
    }
}
