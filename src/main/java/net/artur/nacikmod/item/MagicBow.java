package net.artur.nacikmod.item;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.artur.nacikmod.entity.projectiles.ManaArrowProjectile;
import net.artur.nacikmod.registry.ModEntities;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.TooltipFlag;
import net.artur.nacikmod.capability.mana.ManaProvider;
import org.jetbrains.annotations.Nullable;
import java.util.List;
import java.util.function.Predicate;

public class MagicBow extends BowItem {
    public static final int MAX_DRAW_DURATION = 14;
    private static final int MANA_COST = 5;

    public MagicBow() {
        super(new Item.Properties().stacksTo(1));
    }

    @Override
    public int getUseDuration(ItemStack stack) {
        return 72000;
    }

    @Override
    public boolean canApplyAtEnchantingTable(ItemStack stack, Enchantment enchantment) {
        return enchantment != Enchantments.INFINITY_ARROWS && 
               (enchantment == Enchantments.MULTISHOT || super.canApplyAtEnchantingTable(stack, enchantment));
    }

    @Override
    public Predicate<ItemStack> getAllSupportedProjectiles() {
        return (stack) -> true;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltipComponents, TooltipFlag isAdvanced) {
        super.appendHoverText(stack, level, tooltipComponents, isAdvanced);
        
        tooltipComponents.add(Component.translatable("item.nacikmod.magic_bow.desc1"));
        tooltipComponents.add(Component.translatable("item.nacikmod.magic_bow.desc2", MANA_COST)
                .withStyle(style -> style.withColor(0x00FFFF)));
    }

    @Override
    public void releaseUsing(ItemStack stack, Level level, LivingEntity entity, int timeLeft) {
        if (entity instanceof Player player) {
            int charge = this.getUseDuration(stack) - timeLeft;
            charge = Mth.clamp(charge, 0, MAX_DRAW_DURATION);
            float power = getPowerForTime(charge);

            if (power >= 0.1F) {
                if (!level.isClientSide) {
                    if (player.getCapability(ManaProvider.MANA_CAPABILITY).map(mana -> mana.getMana() >= MANA_COST).orElse(false)) {
                        // Проверяем наличие зачарования Multishot
                        boolean hasMultishot = stack.getEnchantmentLevel(Enchantments.MULTISHOT) > 0;
                        int arrowsToShoot = hasMultishot ? 3 : 1;
                        
                        // Создаем стрелы
                        for (int i = 0; i < arrowsToShoot; i++) {
                            ManaArrowProjectile manaArrow = new ManaArrowProjectile(level, player);
                            
                            // Рассчитываем скорость с учетом силы натяжения
                            float velocity = power * 2.8F;
                            
                            // Применяем зачарования
                            int powerLevel = stack.getEnchantmentLevel(Enchantments.POWER_ARROWS);
                            int punchLevel = stack.getEnchantmentLevel(Enchantments.PUNCH_ARROWS);
                            int flameLevel = stack.getEnchantmentLevel(Enchantments.FLAMING_ARROWS);
                            if (powerLevel > 0) {
                                manaArrow.setPowerLevel(powerLevel);
                            }
                            if (punchLevel > 0) {
                                manaArrow.setPunchLevel(punchLevel);
                            }
                            if (flameLevel > 0) {
                                manaArrow.setFlame(true);
                            }
                            
                            // Для Multishot корректируем угол выстрела
                            float yRot = player.getYRot();
                            float xRot = player.getXRot();
                            
                            if (hasMultishot && i > 0) {
                                // Смещаем угол для дополнительных стрел
                                float spread = 10.0F; // Угол разброса в градусах
                                if (i == 1) {
                                    yRot += spread;
                                } else {
                                    yRot -= spread;
                                }
                            }
                            
                            // Стреляем
                            manaArrow.shootFromRotation(player, xRot, yRot, 0.0F, velocity, 1.0F);
                            level.addFreshEntity(manaArrow);
                        }

                        // Тратим ману
                        player.getCapability(ManaProvider.MANA_CAPABILITY).ifPresent(mana -> mana.removeMana(MANA_COST));

                        stack.hurtAndBreak(1, player, (p) -> p.broadcastBreakEvent(player.getUsedItemHand()));
                        
                        level.playSound(null, player.getX(), player.getY(), player.getZ(),
                                SoundEvents.ARROW_SHOOT, SoundSource.PLAYERS, 1.0F, 1.0F / (level.getRandom().nextFloat() * 0.4F + 1.2F) + power * 0.5F);
                    } else {
                        player.sendSystemMessage(Component.literal("Not enough mana!")
                                .withStyle(ChatFormatting.RED));
                    }
                }
            }
        }
    }

    public static float getPowerForTime(int charge) {
        float f = (float) charge / MAX_DRAW_DURATION;
        f = (f * f + f * 2.4F) / 3.4F; // Подобранные коэффициенты
        if (f > 1.1F) {
            f = 1.1F;
        }
        return f;
    }
}
