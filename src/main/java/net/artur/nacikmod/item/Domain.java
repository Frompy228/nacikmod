package net.artur.nacikmod.item;

import net.artur.nacikmod.NacikMod;
import net.artur.nacikmod.capability.mana.ManaProvider;
import net.artur.nacikmod.item.ability.DomainAbility;
import net.artur.nacikmod.registry.ModEffects;
import net.artur.nacikmod.util.ItemUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.jetbrains.annotations.Nullable;

import java.util.*;

@Mod.EventBusSubscriber(modid = NacikMod.MOD_ID)
public class Domain extends Item implements ItemUtils.ITogglableMagicItem {

    private static final int MANA_COST_PER_SECOND = 10;
    private static final String ACTIVE_TAG = "active";

    public Domain(Properties properties) {
        super(new Item.Properties().stacksTo(1).fireResistant());
    }

    // --- Реализация интерфейса ITogglableMagicItem ---
    @Override
    public String getActiveTag() { return ACTIVE_TAG; }

    @Override
    public void deactivate(Player player, ItemStack stack) {
        DomainAbility.stopDomain(player);
        stack.getOrCreateTag().putBoolean(getActiveTag(), false);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack item = player.getItemInHand(hand);

        if (!level.isClientSide) {
            boolean isActive = item.hasTag() && item.getTag().getBoolean(ACTIVE_TAG);

            if (isActive) {
                DomainAbility.stopDomain(player);
                player.sendSystemMessage(Component.literal("Domain deactivated!").withStyle(ChatFormatting.RED));
            } else {
                if (!player.getCapability(ManaProvider.MANA_CAPABILITY)
                        .map(m -> m.getMana() >= MANA_COST_PER_SECOND)
                        .orElse(false)) {
                    player.sendSystemMessage(Component.literal("Not enough mana!").withStyle(ChatFormatting.RED));
                    return InteractionResultHolder.fail(item);
                }

                DomainAbility.startDomain(player);
                player.sendSystemMessage(Component.literal("Domain activated!").withStyle(ChatFormatting.GREEN));
            }

            player.getCooldowns().addCooldown(this, 20);
        }

        return InteractionResultHolder.success(item);
    }


    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        if (!(event.player instanceof net.minecraft.server.level.ServerPlayer player)) return;

        UUID uuid = player.getUUID();

        if (!DomainAbility.activeDomainPlayers.contains(uuid)) return;

        // ИСПОЛЬЗУЕМ УТИЛИТУ: Поиск предмета по всему инвентарю и курсору
        ItemStack activeItem = ItemUtils.findActiveItem(player, Domain.class);

        if (activeItem == null) {
            DomainAbility.stopDomain(player);
            return;
        }

        if (player.tickCount % 20 == 0) {
            player.getCapability(ManaProvider.MANA_CAPABILITY).ifPresent(mana -> {
                if (mana.getMana() < MANA_COST_PER_SECOND) {
                    DomainAbility.stopDomain(player);
                    player.sendSystemMessage(Component.literal("Not enough mana! Domain deactivated.").withStyle(ChatFormatting.RED));
                } else {
                    mana.removeMana(MANA_COST_PER_SECOND);
                    // Накладываем эффект для игровой механики (не для визуализации - визуализация через Ability)
                    player.addEffect(new MobEffectInstance(ModEffects.EFFECT_DOMAIN.get(), 40, 0, false, false, true));
                }
            });
        }
    }

    @SubscribeEvent
    public static void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        DomainAbility.activeDomainPlayers.remove(event.getEntity().getUUID());
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag isAdvanced) {
        tooltip.add(Component.translatable("item.nacikmod.domain.desc1"));
        tooltip.add(Component.translatable("item.nacikmod.domain.desc2", MANA_COST_PER_SECOND).withStyle(style -> style.withColor(0x00FFFF)));

        boolean active = stack.hasTag() && stack.getTag().getBoolean(ACTIVE_TAG);
        tooltip.add(Component.translatable(active ? "item.active" : "item.inactive")
                .withStyle(active ? ChatFormatting.GREEN : ChatFormatting.RED));
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return stack.hasTag() && stack.getTag().getBoolean(ACTIVE_TAG);
    }
}
