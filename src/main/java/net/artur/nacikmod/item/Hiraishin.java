package net.artur.nacikmod.item;

import net.artur.nacikmod.item.ability.HiraishinAbility;
import net.artur.nacikmod.registry.ModItems;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.TooltipFlag;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;

public class Hiraishin extends SwordItem {
    private static final String OWNER_TAG = "Owner";
    private static final String OWNER_NAME_TAG = "OwnerName";

    public Hiraishin() {
        super(new CustomTier(), 3, -2F, new Properties().rarity(ShardArtifact.RED));
    }


    @Override
    public void onCraftedBy(ItemStack stack, Level level, Player player) {
        super.onCraftedBy(stack, level, player);
        setOwner(stack, player);
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, net.minecraft.world.entity.Entity entity, int slotId, boolean isSelected) {
        super.inventoryTick(stack, level, entity, slotId, isSelected);
        if (!level.isClientSide && entity instanceof Player player) {
            if (!hasOwner(stack)) {
                setOwner(stack, player);
            }
        }
    }

    public static void setOwner(ItemStack stack, Player player) {
        CompoundTag tag = stack.getOrCreateTag();
        tag.putUUID(OWNER_TAG, player.getUUID());
        tag.putString(OWNER_NAME_TAG, player.getGameProfile().getName());
    }

    public static boolean hasOwner(ItemStack stack) {
        return stack.hasTag() && stack.getTag().contains(OWNER_TAG);
    }

    public static boolean isOwner(ItemStack stack, Player player) {
        if (!hasOwner(stack)) return false;
        return stack.getTag().getUUID(OWNER_TAG).equals(player.getUUID());
    }

    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        if (attacker instanceof Player player) {
            if (!isOwner(stack, player)) {
                player.sendSystemMessage(Component.literal("This Hiraishin belongs to " + stack.getTag().getString(OWNER_NAME_TAG))
                        .withStyle(ChatFormatting.RED));
                return false;
            }
            HiraishinAbility.markEntity(target, player, stack);
        }
        return super.hurtEnemy(stack, target, attacker);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!isOwner(stack, player)) {
            player.sendSystemMessage(Component.literal("This Hiraishin belongs to " + stack.getTag().getString(OWNER_NAME_TAG))
                    .withStyle(ChatFormatting.RED));
            return InteractionResultHolder.fail(stack);
        }
        return HiraishinAbility.useAbility(level, player, hand);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltipComponents, TooltipFlag isAdvanced) {
        super.appendHoverText(stack, level, tooltipComponents, isAdvanced);

        if (hasOwner(stack)) {
            tooltipComponents.add(Component.translatable("item.nacikmod.hiraishin.desc5", stack.getTag().getString(OWNER_NAME_TAG))
                    .withStyle(ChatFormatting.GOLD));
        }

        tooltipComponents.add(Component.translatable("item.nacikmod.hiraishin.desc1"));
        tooltipComponents.add(Component.translatable("item.nacikmod.hiraishin.desc2")
                .withStyle(style -> style.withColor(0x00FFFF)));

        tooltipComponents.add(Component.translatable("item.nacikmod.hiraishin.desc3")
                .withStyle(ChatFormatting.GRAY));
        tooltipComponents.add(Component.translatable("item.nacikmod.hiraishin.desc4")
                .withStyle(ChatFormatting.GRAY));
    }


    private static class CustomTier implements Tier {
        @Override
        public int getUses() {
            return 1950; // Прочность меча
        }

        @Override
        public float getSpeed() {
            return 1.5f; // Скорость атаки
        }

        @Override
        public float getAttackDamageBonus() {
            return 8.0f; // Базовый урон
        }

        @Override
        public int getLevel() {
            return 4; // Уровень прочности (как у алмаза)
        }

        @Override
        public int getEnchantmentValue() {
            return 25; // Насколько хорошо зачаровывается
        }

        @Override
        public Ingredient getRepairIngredient() {
            return Ingredient.of(ModItems.SHARD_OF_ARTIFACT.get()); // Ремонт артефактом
        }
    }
}
