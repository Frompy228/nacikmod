package net.artur.nacikmod.item;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.Level;
import net.artur.nacikmod.entity.custom.BloodWarriorEntity;
import net.artur.nacikmod.registry.ModEntities;
import net.artur.nacikmod.registry.ModItems;
import net.artur.nacikmod.capability.mana.ManaProvider;
import net.artur.nacikmod.util.PlayerCooldowns;
import net.artur.nacikmod.item.ability.BloodCircleManager;
import net.minecraft.world.entity.EquipmentSlot;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class BloodWarrior extends Item {
    private static final int MANA_COST = 100;
    private static final float HEALTH_COST = 15.0f;
    private static final int COOLDOWN_TICKS = 600;

    public BloodWarrior(Properties properties) {
        super(properties.rarity(Rarity.EPIC).stacksTo(1).fireResistant());
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack itemStack = player.getItemInHand(hand);

        if (!level.isClientSide) {
            // Check cooldown using our custom system
            if (PlayerCooldowns.isOnCooldown(player, this)) {
                int left = PlayerCooldowns.getCooldownLeft(player, this);
                player.sendSystemMessage(Component.literal("Item is on cooldown! (" + left / 20 + "s left)")
                        .withStyle(ChatFormatting.RED));
                return InteractionResultHolder.fail(itemStack);
            }
            
            // Check if player has enough mana
            if (!player.getCapability(ManaProvider.MANA_CAPABILITY).map(mana -> mana.getMana() >= MANA_COST).orElse(false)) {
                player.sendSystemMessage(Component.literal("Not enough mana!")
                        .withStyle(ChatFormatting.RED));
                return InteractionResultHolder.fail(itemStack);
            }

            // Check if player has enough health
            if (player.getHealth() <= HEALTH_COST) {
                player.sendSystemMessage(Component.literal("Not enough health!")
                        .withStyle(ChatFormatting.RED));
                return InteractionResultHolder.fail(itemStack);
            }

            // Use health and mana first
            player.hurt(player.damageSources().generic(), HEALTH_COST);
            player.getCapability(ManaProvider.MANA_CAPABILITY).ifPresent(mana -> mana.removeMana(MANA_COST));

            // Проверка: активен ли круг
            boolean isCircleActive = BloodCircleManager.isActive(player);
            int warriorCount = isCircleActive ? 4 : 3;

            for (int i = 0; i < warriorCount; i++) {
                BloodWarriorEntity warrior = ModEntities.BLOOD_WARRIOR.get().create(level);
                if (warrior != null) {
                    double angle = (i * (360.0 / warriorCount)) * Math.PI / 180;
                    double radius = 2.0;
                    double x = player.getX() + Math.cos(angle) * radius;
                    double z = player.getZ() + Math.sin(angle) * radius;

                    warrior.setPos(x, player.getY(), z);
                    warrior.setOwner(player);

                    // ЛОГИКА ВЫДАЧИ КОПЬЯ
                    if (isCircleActive) {
                        // Устанавливаем Blood Spear в правую руку
                        warrior.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(ModItems.BLOOD_SPEAR.get()));
                    }

                    level.addFreshEntity(warrior);
                }
            }

            PlayerCooldowns.setCooldown(player, this, COOLDOWN_TICKS);

            String message = isCircleActive ? "Blood Circle empowers you! 4 warriors with spears summoned!" : "Blood warriors summoned!";
            player.sendSystemMessage(Component.literal(message)
                    .withStyle(isCircleActive ? ChatFormatting.LIGHT_PURPLE : ChatFormatting.GREEN));
        }

        return InteractionResultHolder.success(itemStack);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltipComponents, TooltipFlag isAdvanced) {
        super.appendHoverText(stack, level, tooltipComponents, isAdvanced);

        tooltipComponents.add(Component.translatable("item.nacikmod.blood_warrior.desc1"));
        tooltipComponents.add(Component.translatable("item.nacikmod.blood_warrior.desc2")
                .withStyle(ChatFormatting.DARK_RED));
        tooltipComponents.add(Component.translatable("item.nacikmod.blood_warrior.desc3")
                .withStyle(style -> style.withColor(0x00FFFF)));

        if (level != null && level.isClientSide) {
            Player player = net.artur.nacikmod.util.ItemUtils.getClientPlayer(level);
            if (player != null && BloodCircleManager.isActive(player)) {
                tooltipComponents.add(Component.literal("BLOOD CIRCLE ACTIVE: +1 Warrior & Blood Spears")
                        .withStyle(ChatFormatting.LIGHT_PURPLE));
            }
        }
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
        return true; // Предмет всегда светится
    }
}
