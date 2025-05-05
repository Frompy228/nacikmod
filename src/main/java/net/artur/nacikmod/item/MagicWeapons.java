package net.artur.nacikmod.item;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.artur.nacikmod.capability.mana.ManaProvider;
import net.artur.nacikmod.entity.projectiles.ManaSwordProjectile;
import net.artur.nacikmod.registry.ModEntities;
import net.artur.nacikmod.registry.ModItems;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class MagicWeapons extends Item {
    private static final int MANA_COST = 100;
    private static final int MANA_PER_DAMAGE = 250;
    private static final int PROJECTILE_MANA_COST = 100;

    public MagicWeapons(Properties properties) {
        super(properties);
    }

    private int calculateManaDamage(Player player) {
        return player.getCapability(ManaProvider.MANA_CAPABILITY)
                .map(mana -> mana.getMaxMana() / MANA_PER_DAMAGE)
                .orElse(0);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack itemStack = player.getItemInHand(hand);
        boolean isShiftKeyDown = player.isShiftKeyDown();

        if (!level.isClientSide) {
            if (isShiftKeyDown) {
                // Режим создания меча
                if (!player.getCapability(ManaProvider.MANA_CAPABILITY).map(mana -> mana.getMana() >= MANA_COST).orElse(false)) {
                    player.sendSystemMessage(Component.literal("Not enough mana!")
                            .withStyle(ChatFormatting.RED));
                    return InteractionResultHolder.fail(itemStack);
                }

                // Используем ману
                player.getCapability(ManaProvider.MANA_CAPABILITY).ifPresent(mana -> mana.removeMana(MANA_COST));

                // Создаем меч
                ItemStack manaSword = new ItemStack(ModItems.MANA_SWORD.get());

                // Вычисляем дополнительный урон на основе максимальной маны
                int manaDamage = calculateManaDamage(player);

                // Сохраняем дополнительный урон в NBT
                manaSword.getOrCreateTag().putInt("ManaDamage", manaDamage);

                // Добавляем зачарование "Небесная кара" I уровня
                manaSword.enchant(Enchantments.SMITE, 1);

                if (!player.getInventory().add(manaSword)) {
                    player.drop(manaSword, false);
                }

                player.sendSystemMessage(Component.literal("Mana Sword Created")
                        .withStyle(ChatFormatting.GREEN));

                // Устанавливаем кулдаун
                player.getCooldowns().addCooldown(this, 20);
            } else {
                // Режим стрельбы
                if (!player.getCapability(ManaProvider.MANA_CAPABILITY).map(mana -> mana.getMana() >= PROJECTILE_MANA_COST).orElse(false)) {
                    player.sendSystemMessage(Component.literal("Not enough mana!")
                            .withStyle(ChatFormatting.RED));
                    return InteractionResultHolder.fail(itemStack);
                }

                // Используем ману
                player.getCapability(ManaProvider.MANA_CAPABILITY).ifPresent(mana -> mana.removeMana(PROJECTILE_MANA_COST));

                // Вычисляем дополнительный урон на основе максимальной маны
                int manaDamage = calculateManaDamage(player);

                // Создаем и запускаем снаряд
                ManaSwordProjectile projectile = new ManaSwordProjectile(level, player, (float)manaDamage);
                projectile.setPos(player.getX(), player.getEyeY() - 0.1, player.getZ());
                projectile.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, 1.5F, 1.0F);
                level.addFreshEntity(projectile);

                // Устанавливаем кулдаун
                player.getCooldowns().addCooldown(this, 20);
            }
        }

        return InteractionResultHolder.success(itemStack);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltipComponents, TooltipFlag isAdvanced) {
        super.appendHoverText(stack, level, tooltipComponents, isAdvanced);

        tooltipComponents.add(Component.translatable("item.nacikmod.magic_weapons.desc1"));

        if (level != null && level.isClientSide) {
            for (Player player : level.players()) {
                if (player.isLocalPlayer()) {
                    int manaDamage = calculateManaDamage(player);
                    tooltipComponents.add(Component.translatable("item.nacikmod.magic_weapons.desc5", manaDamage)
                            .withStyle(ChatFormatting.BLUE));
                    break;
                }
            }
        }

        tooltipComponents.add(Component.translatable("item.nacikmod.magic_weapons.desc2")
                .withStyle(style -> style.withColor(0x00FFFF)));
        tooltipComponents.add(Component.translatable("item.nacikmod.magic_weapons.desc3")
                .withStyle(ChatFormatting.GRAY));
        tooltipComponents.add(Component.translatable("item.nacikmod.magic_weapons.desc4")
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
}
