package net.artur.nacikmod.item;

import net.artur.nacikmod.NacikMod;
import net.artur.nacikmod.capability.mana.ManaProvider;
import net.artur.nacikmod.item.ability.BloodBoneAbility;
import net.artur.nacikmod.item.ability.BloodCircleManager;
import net.artur.nacikmod.util.ItemUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.jetbrains.annotations.Nullable;

import java.util.List;

@Mod.EventBusSubscriber(modid = NacikMod.MOD_ID)
public class BloodBone extends Item implements ItemUtils.ITogglableMagicItem {

    private static final String ACTIVE_TAG = "active";

    // Константы
    private static final float DAMAGE_REDUCTION_NORMAL = 0.15f;
    private static final float DAMAGE_REDUCTION_CIRCLE = 0.25f;
    private static final int MANA_PER_DAMAGE = 10;

    // Эффекты
    private static final int BLOOD_POISONING_DURATION_NORMAL = 100;
    private static final int BLOOD_POISONING_DURATION_CIRCLE = 140;
    private static final int BLOOD_EXPLOSION_DURATION = 60;
    private static final int BLOOD_EXPLOSION_AMPLIFIER = 3;

    public BloodBone(Properties properties) {
        super(properties.rarity(Rarity.EPIC).stacksTo(1).fireResistant());
    }

    @Override
    public String getActiveTag() {
        return ACTIVE_TAG;
    }

    @Override
    public void deactivate(Player player, ItemStack stack) {
        BloodBoneAbility.stopBloodBone(player);
        stack.getOrCreateTag().putBoolean(getActiveTag(), false);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack item = player.getItemInHand(hand);

        if (!level.isClientSide) {
            boolean isActive = item.hasTag() && item.getTag().getBoolean(ACTIVE_TAG);

            if (isActive) {
                BloodBoneAbility.stopBloodBone(player);
                item.getOrCreateTag().putBoolean(ACTIVE_TAG, false);
                player.sendSystemMessage(Component.literal("Blood Bone deactivated!").withStyle(ChatFormatting.RED));
            } else {
                boolean hasMana = player.getCapability(ManaProvider.MANA_CAPABILITY)
                        .map(m -> m.getMana() > 0)
                        .orElse(false);

                if (!hasMana) {
                    player.sendSystemMessage(Component.literal("Not enough mana!").withStyle(ChatFormatting.RED));
                    return InteractionResultHolder.fail(item);
                }

                BloodBoneAbility.startBloodBone(player);
                item.getOrCreateTag().putBoolean(ACTIVE_TAG, true);
                player.sendSystemMessage(Component.literal("Blood Bone activated!").withStyle(ChatFormatting.GREEN));
            }
            player.getCooldowns().addCooldown(this, 20);
        }

        return InteractionResultHolder.success(item);
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        if (!(event.player instanceof net.minecraft.server.level.ServerPlayer player)) return;

        if (!BloodBoneAbility.isBloodBoneActive(player)) return;

        ItemStack activeItem = ItemUtils.findActiveItem(player, BloodBone.class);
        if (activeItem == null) {
            BloodBoneAbility.stopBloodBone(player);
            return;
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void onPlayerDamage(LivingDamageEvent event) {
        if (event.getEntity().level().isClientSide) return;
        if (!(event.getEntity() instanceof Player player)) return;

        if (!BloodBoneAbility.isBloodBoneActive(player)) return;

        float originalDamage = event.getAmount();

        boolean inBloodCircle = BloodCircleManager.isActive(player);
        float damageReduction = inBloodCircle ? DAMAGE_REDUCTION_CIRCLE : DAMAGE_REDUCTION_NORMAL;

        float absorbedDamage = originalDamage * damageReduction;
        int manaCost = (int) (absorbedDamage * MANA_PER_DAMAGE);

        final float[] actualAbsorbedDamage = {absorbedDamage};

        player.getCapability(ManaProvider.MANA_CAPABILITY).ifPresent(mana -> {
            if (mana.getMana() >= manaCost) {
                mana.removeMana(manaCost);
            } else {
                int availableMana = mana.getMana();
                actualAbsorbedDamage[0] = (float) availableMana / MANA_PER_DAMAGE;
                mana.setMana(0);
            }
        });

        event.setAmount(originalDamage - actualAbsorbedDamage[0]);
    }

    @SubscribeEvent
    public static void onPlayerAttack(LivingHurtEvent event) {
        if (event.getEntity().level().isClientSide) return;
        if (!(event.getSource().getEntity() instanceof Player player)) return;

        if (!BloodBoneAbility.isBloodBoneActive(player)) return;

        LivingEntity target = event.getEntity();
        boolean inBloodCircle = BloodCircleManager.isActive(player);

        int poisoningDuration = inBloodCircle ? BLOOD_POISONING_DURATION_CIRCLE : BLOOD_POISONING_DURATION_NORMAL;

        target.addEffect(new MobEffectInstance(
                net.artur.nacikmod.registry.ModEffects.EFFECT_BLOOD_POISONING.get(),
                poisoningDuration, 0, false, true, true
        ));

        if (inBloodCircle) {
            if (!target.hasEffect(net.artur.nacikmod.registry.ModEffects.BLOOD_EXPLOSION.get())) {
                target.addEffect(new MobEffectInstance(
                        net.artur.nacikmod.registry.ModEffects.BLOOD_EXPLOSION.get(),
                        BLOOD_EXPLOSION_DURATION, BLOOD_EXPLOSION_AMPLIFIER, false, true, true
                ));
            }
        }
    }

    // --- ОБНОВЛЕННЫЙ МЕТОД ОПИСАНИЯ ---
    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag isAdvanced) {
        tooltip.add(Component.translatable("item.nacikmod.blood_rib.desc1"));

        if (level != null && level.isClientSide) {
            Player player = ItemUtils.getClientPlayer(level);
            if (player != null) {
                // Проверяем активность круга
                boolean inCircle = BloodCircleManager.isActive(player);

                // Динамическое отображение защиты:
                // Если круг активен, строка про 25% горит ярко, а про 15% тускнеет (и наоборот)
                tooltip.add(Component.translatable("item.nacikmod.blood_rib.desc2", MANA_PER_DAMAGE)
                        .withStyle(style -> style.withColor(0x00FFFF)));


                // ДОБАВЛЕННЫЙ БЛОК: Фиолетовое описание бонусов круга (как в BloodShoot)
                if (inCircle) {
                    tooltip.add(Component.empty()); // Отступ
                    tooltip.add(Component.literal("BLOOD CIRCLE ACTIVE:").withStyle(ChatFormatting.LIGHT_PURPLE));
                    tooltip.add(Component.literal(" + Protection increased to 25%").withStyle(ChatFormatting.LIGHT_PURPLE));
                    tooltip.add(Component.literal(" + Poison Duration: 7s").withStyle(ChatFormatting.LIGHT_PURPLE));
                    tooltip.add(Component.literal(" + Applies Blood Explosion").withStyle(ChatFormatting.LIGHT_PURPLE));
                }

            }
        }

        // Статус Активно/Неактивно
        boolean active = stack.hasTag() && stack.getTag().getBoolean(ACTIVE_TAG);
        tooltip.add(Component.translatable(active ? "item.active" : "item.inactive")
                .withStyle(active ? ChatFormatting.GREEN : ChatFormatting.RED));
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return stack.hasTag() && stack.getTag().getBoolean(ACTIVE_TAG);
    }
}