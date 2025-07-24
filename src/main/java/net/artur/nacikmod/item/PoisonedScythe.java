package net.artur.nacikmod.item;

import net.artur.nacikmod.registry.ModItems;
import net.artur.nacikmod.registry.ModEffects;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.*;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.common.ForgeMod;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import com.google.common.collect.Multimap;
import com.google.common.collect.ImmutableMultimap;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;
import java.util.List;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import java.util.Iterator;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;

public class PoisonedScythe extends AxeItem {
    private static final UUID ENTITY_REACH_MODIFIER_ID = UUID.fromString("b2e7e1c2-7e2a-4e2a-9e2a-2e2a2e2a2e2a");
    private final Multimap<Attribute, AttributeModifier> attributeModifiers;
    private static final int STRONG_POISON_DURATION = 200; // 10 секунд (20 тиков = 1 сек)
    private static final int MANA_COST = 350;
    private static final String POISONED_ENTITIES_TAG = "PoisonedEntities";

    public PoisonedScythe() {
        super(new CustomTier(), 8.0f, -3.1f, new Item.Properties().rarity(ShardArtifact.RED));
        ImmutableMultimap.Builder<Attribute, AttributeModifier> builder = ImmutableMultimap.builder();
        builder.put(ForgeMod.ENTITY_REACH.get(),
                new AttributeModifier(ENTITY_REACH_MODIFIER_ID, "Entity reach", 0.75, AttributeModifier.Operation.ADDITION));
        builder.put(Attributes.ATTACK_DAMAGE,
                new AttributeModifier(BASE_ATTACK_DAMAGE_UUID, "Weapon damage", 13.0, AttributeModifier.Operation.ADDITION));
        builder.put(Attributes.ATTACK_SPEED,
                new AttributeModifier(BASE_ATTACK_SPEED_UUID, "Weapon speed", -3.1, AttributeModifier.Operation.ADDITION));
        this.attributeModifiers = builder.build();
    }

    @Override
    public Multimap<Attribute, AttributeModifier> getDefaultAttributeModifiers(EquipmentSlot slot) {
        if (slot == EquipmentSlot.MAINHAND) {
            return attributeModifiers;
        }
        return super.getDefaultAttributeModifiers(slot);
    }

    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        if (attacker instanceof Player player) {
            if (player.getAttackStrengthScale(0.5f) >= 0.9f) {
                MobEffectInstance existing = target.getEffect(ModEffects.STRONG_POISON.get());
                int amplifier = existing != null ? existing.getAmplifier() + 1 : 0;
                target.addEffect(new MobEffectInstance(ModEffects.STRONG_POISON.get(), STRONG_POISON_DURATION, amplifier));
                // --- Добавляем UUID цели в NBT предмета (CompoundTag) ---
                CompoundTag tag = stack.getOrCreateTag();
                ListTag list = tag.getList(POISONED_ENTITIES_TAG, Tag.TAG_COMPOUND);
                UUID uuid = target.getUUID();
                boolean alreadyPresent = false;
                for (int i = 0; i < list.size(); i++) {
                    CompoundTag entityTag = list.getCompound(i);
                    if (entityTag.hasUUID("uuid") && entityTag.getUUID("uuid").equals(uuid)) {
                        alreadyPresent = true;
                        break;
                    }
                }
                if (!alreadyPresent) {
                    CompoundTag entityTag = new CompoundTag();
                    entityTag.putUUID("uuid", uuid);
                    list.add(entityTag);
                    tag.put(POISONED_ENTITIES_TAG, list);
                }
            }
        }
        return super.hurtEnemy(stack, target, attacker);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!level.isClientSide) {
            CompoundTag tag = stack.getOrCreateTag();
            ListTag list = tag.getList(POISONED_ENTITIES_TAG, Tag.TAG_COMPOUND);
            // Очищаем список и считаем реально поражённых
            int affected = 0;
            for (int i = 0; i < list.size(); ) {
                CompoundTag entityTag = list.getCompound(i);
                if (!entityTag.hasUUID("uuid")) {
                    list.remove(i);
                    continue;
                }
                UUID uuid = entityTag.getUUID("uuid");
                LivingEntity target = null;
                if (player instanceof net.minecraft.server.level.ServerPlayer serverPlayer) {
                    for (ServerLevel serverLevel : serverPlayer.getServer().getAllLevels()) {
                        Entity entity = serverLevel.getEntity(uuid);
                        if (entity instanceof LivingEntity living && living.isAlive() && living.hasEffect(ModEffects.STRONG_POISON.get())) {
                            target = living;
                            break;
                        }
                    }
                } else if (level instanceof ServerLevel serverLevel) {
                    Entity entity = serverLevel.getEntity(uuid);
                    if (entity instanceof LivingEntity living && living.isAlive() && living.hasEffect(ModEffects.STRONG_POISON.get())) {
                        target = living;
                    }
                }
                if (target != null) {
                    affected++;
                    i++;
                } else {
                    list.remove(i);
                }
            }
            tag.put(POISONED_ENTITIES_TAG, list);
            if (affected == 0) {
                player.sendSystemMessage(Component.literal("No poisoned targets!").withStyle(net.minecraft.ChatFormatting.RED));
                return InteractionResultHolder.fail(stack);
            }
            // Проверяем наличие маны
            if (!player.getCapability(net.artur.nacikmod.capability.mana.ManaProvider.MANA_CAPABILITY).map(mana -> mana.getMana() >= MANA_COST).orElse(false)) {
                player.sendSystemMessage(Component.literal("Not enough mana!").withStyle(net.minecraft.ChatFormatting.RED));
                return InteractionResultHolder.fail(stack);
            }
            // Тратим ману
            player.getCapability(net.artur.nacikmod.capability.mana.ManaProvider.MANA_CAPABILITY).ifPresent(mana -> mana.removeMana(MANA_COST));
            // Повторно проходим по списку и наносим урон
            for (int i = 0; i < list.size(); ) {
                CompoundTag entityTag = list.getCompound(i);
                UUID uuid = entityTag.getUUID("uuid");
                LivingEntity target = null;
                if (player instanceof net.minecraft.server.level.ServerPlayer serverPlayer) {
                    for (ServerLevel serverLevel : serverPlayer.getServer().getAllLevels()) {
                        Entity entity = serverLevel.getEntity(uuid);
                        if (entity instanceof LivingEntity living && living.isAlive()) {
                            target = living;
                            break;
                        }
                    }
                } else if (level instanceof ServerLevel serverLevel) {
                    Entity entity = serverLevel.getEntity(uuid);
                    if (entity instanceof LivingEntity living && living.isAlive()) {
                        target = living;
                    }
                }
                if (target != null) {
                    MobEffectInstance effect = target.getEffect(ModEffects.STRONG_POISON.get());
                    if (effect != null) {
                        int amplifier = effect.getAmplifier();
                        target.removeEffect(ModEffects.STRONG_POISON.get());
                        float damage = (amplifier + 1) * 10.0f;
                        target.hurt(level.damageSources().playerAttack(player), damage);
                    }
                    i++;
                } else {
                    list.remove(i);
                }
            }
            tag.put(POISONED_ENTITIES_TAG, list);
            player.getCooldowns().addCooldown(this, 100);
            player.sendSystemMessage(Component.literal("Affected entities: " + affected).withStyle(net.minecraft.ChatFormatting.GREEN));
        }
        return InteractionResultHolder.success(stack);
    }

    private static class CustomTier implements Tier {
        @Override
        public int getUses() { return 1950; } // Прочность
        @Override
        public float getSpeed() { return 2.0f; } // Скорость копания
        @Override
        public float getAttackDamageBonus() { return 6.0f; } // Бонус урона
        @Override
        public int getLevel() { return 4; } // Уровень (например, как алмаз)
        @Override
        public int getEnchantmentValue() { return 20; } // Зачаровываемость
        @Override
        public Ingredient getRepairIngredient() {
            return Ingredient.of(ModItems.SHARD_OF_ARTIFACT.get()); // или другой ваш предмет
        }
    }
    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltipComponents, TooltipFlag isAdvanced) {
        super.appendHoverText(stack, level, tooltipComponents, isAdvanced);

        tooltipComponents.add(Component.translatable("item.nacikmod.poisoned_scythe.desc1"));
        tooltipComponents.add(Component.translatable("item.nacikmod.poisoned_scythe.desc2")
                .withStyle(style -> style.withColor(0x00FFFF)));
    }
}

