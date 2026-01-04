package net.artur.nacikmod.capability.mana;

import net.artur.nacikmod.capability.reward.PlayerRewardsProvider;
import net.artur.nacikmod.registry.ModMessages;
import net.artur.nacikmod.registry.ModEffects;
import net.artur.nacikmod.registry.ModItems;
import net.artur.nacikmod.registry.ModAttributes;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.type.inventory.ICurioStacksHandler;

import java.util.UUID;

@Mod.EventBusSubscriber
public class ManaRegenerationHandler {
    private static final int BASE_REGEN_AMOUNT = 1;
    private static final int REGEN_INTERVAL = 20; // 1 раз в секунду
    private static final int DARK_SPHERE_REGEN = 2;
    private static final int MANA_BLESSING_REGEN = 1;
    private static final UUID TRUE_MAGE_ARMOR = UUID.fromString("464326ce-9581-4d13-8910-883042df1243");
    private static final UUID MANA_15K_ARMOR_UUID = UUID.fromString("a1b2c3d4-e5f6-7890-abcd-ef1234567891");

    /**
     * ОТДЕЛЬНЫЙ ТИК ДЛЯ МОБОВ (Леонид, мобы и т.д.)
     * Обеспечивает работу их способностей и отображение маны над головой
     */
    @SubscribeEvent
    public static void onLivingTick(LivingEvent.LivingTickEvent event) {
        LivingEntity entity = event.getEntity();

        // Нам нужны только мобы (игроки обрабатываются отдельно в onPlayerTick)
        if (entity.level().isClientSide || entity instanceof Player) return;

        if (entity.tickCount % REGEN_INTERVAL == 0) {
            entity.getCapability(ManaProvider.MANA_CAPABILITY).ifPresent(mana -> {
                // 1. Регенерация для мобов (чтобы они могли использовать скиллы)
                if (mana.getMana() < mana.getMaxMana()) {
                    mana.regenerateMana(5); // Базовая регенерация мобов
                }

                // 2. СИНХРОНИЗАЦИЯ для РЕНДЕРА (чтобы Wallhack видел ману Леонида)
                ModMessages.sendEntityManaToNearbyPlayers(entity, mana.getMana(), mana.getMaxMana());
            });
        }
    }

    /**
     * ТИК ДЛЯ ИГРОКОВ
     * Обрабатывает всё: синхронизацию GUI, реген от предметов, награды и статусы
     */
    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        // Выполняем только в конце тика на серверной стороне
        if (event.phase != TickEvent.Phase.END || event.player.level().isClientSide) return;

        Player player = event.player;

        if (player.tickCount % REGEN_INTERVAL == 0) {
            player.getCapability(ManaProvider.MANA_CAPABILITY).ifPresent(mana -> {
                int currentMana = mana.getMana();
                int maxMana = mana.getMaxMana();

                if (player instanceof ServerPlayer serverPlayer) {
                    // 1. Личная синхронизация (для полоски маны на экране)
                    ModMessages.sendManaToClient(serverPlayer, currentMana, maxMana);

                    // 2. Синхронизация статусов (чтобы работали иконки и эффекты магов)
                    ModMessages.sendTrueMageStatusToClient(serverPlayer, mana.isTrueMage());
                    ModMessages.sendVisionBlessingStatusToClient(serverPlayer, mana.hasVisionBlessing());

                    // 3. Синхронизация для других игроков (чтобы другие видели вашу ману в Wallhack)
                    ModMessages.sendPlayerManaToNearbyPlayers(serverPlayer, currentMana, maxMana);
                }

                // Применение атрибутов брони
                applyTrueMageAttribute(player, mana.isTrueMage());
                apply15kManaAttribute(player);

                // Регенерация игрока
                if (currentMana < maxMana) {
                    mana.regenerateMana(getRegenAmount(player));
                }
            });
        }
    }

    private static void applyTrueMageAttribute(Player player, boolean isTrueMage) {
        AttributeInstance armorAttribute = player.getAttribute(ModAttributes.BONUS_ARMOR.get());
        if (armorAttribute != null) {
            if (isTrueMage) {
                if (armorAttribute.getModifier(TRUE_MAGE_ARMOR) == null) {
                    armorAttribute.addPermanentModifier(new AttributeModifier(TRUE_MAGE_ARMOR, "True Mage Armor Bonus", 2.0, AttributeModifier.Operation.ADDITION));
                }
            } else {
                armorAttribute.removeModifier(TRUE_MAGE_ARMOR);
            }
        }
    }

    private static void apply15kManaAttribute(Player player) {
        AttributeInstance armorAttribute = player.getAttribute(ModAttributes.BONUS_ARMOR.get());
        if (armorAttribute != null) {
            player.getCapability(PlayerRewardsProvider.PLAYER_REWARDS_CAPABILITY).ifPresent(rewards -> {
                if (rewards.hasReceived15kManaReward()) {
                    if (armorAttribute.getModifier(MANA_15K_ARMOR_UUID) == null) {
                        armorAttribute.addPermanentModifier(new AttributeModifier(MANA_15K_ARMOR_UUID, "15k Mana Armor Bonus", 1.0, AttributeModifier.Operation.ADDITION));
                    }
                } else {
                    armorAttribute.removeModifier(MANA_15K_ARMOR_UUID);
                }
            });
        }
    }

    private static int getRegenAmount(Player player) {
        final int[] regenAmount = {BASE_REGEN_AMOUNT};

        player.getCapability(PlayerRewardsProvider.PLAYER_REWARDS_CAPABILITY).ifPresent(rewards -> {
            if (rewards.hasReceived24hReward()) regenAmount[0] += 2;
            if (rewards.hasReceived5kManaReward()) regenAmount[0] += 1;
            if (rewards.hasReceived15kManaReward()) regenAmount[0] += 1;
        });

        if (player.hasEffect(ModEffects.EFFECT_MANA_BLESSING.get())) {
            regenAmount[0] += MANA_BLESSING_REGEN;
        }

        player.getCapability(ManaProvider.MANA_CAPABILITY).ifPresent(mana -> {
            if (mana.isTrueMage()) regenAmount[0] += 1;
        });

        CuriosApi.getCuriosInventory(player).ifPresent(handler -> {
            for (ICurioStacksHandler stacksHandler : handler.getCurios().values()) {
                for (int i = 0; i < stacksHandler.getSlots(); i++) {
                    if (stacksHandler.getStacks().getStackInSlot(i).getItem() == ModItems.DARK_SPHERE.get()) {
                        regenAmount[0] += DARK_SPHERE_REGEN;
                        return;
                    }
                }
            }
        });

        return regenAmount[0];
    }
}