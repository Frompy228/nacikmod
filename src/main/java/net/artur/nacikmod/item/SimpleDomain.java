package net.artur.nacikmod.item;

import net.artur.nacikmod.NacikMod;
import net.artur.nacikmod.capability.mana.ManaProvider;
import net.artur.nacikmod.registry.ModEffects;
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
public class SimpleDomain extends Item {

    private static final int MANA_COST_PER_SECOND = 5;
    private static final String ACTIVE_TAG = "active";
    private static final Set<UUID> activePlayers = new HashSet<>();

    public SimpleDomain(Properties properties) {
        super(properties.fireResistant());
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack item = player.getItemInHand(hand);

        if (!level.isClientSide) {
            boolean isActive = item.hasTag() && item.getTag().getBoolean(ACTIVE_TAG);

            if (isActive) {
                deactivate(player);
                player.sendSystemMessage(Component.literal("Simple Domain deactivated!").withStyle(ChatFormatting.RED));
            } else {
                // Проверка маны
                if (!player.getCapability(ManaProvider.MANA_CAPABILITY)
                        .map(m -> m.getMana() >= MANA_COST_PER_SECOND)
                        .orElse(false)) {
                    player.sendSystemMessage(Component.literal("Not enough mana!").withStyle(ChatFormatting.RED));
                    return InteractionResultHolder.fail(item);
                }

                activate(player);
                player.sendSystemMessage(Component.literal("Simple Domain activated!").withStyle(ChatFormatting.GREEN));
            }

            player.getCooldowns().addCooldown(this, 20);
        }

        return InteractionResultHolder.success(item);
    }

    private static void activate(Player player) {
        activePlayers.add(player.getUUID());
        for (ItemStack stack : player.getInventory().items) {
            if (stack.getItem() instanceof SimpleDomain) {
                stack.getOrCreateTag().putBoolean(ACTIVE_TAG, true);
                break;
            }
        }
    }

    private static void deactivate(Player player) {
        activePlayers.remove(player.getUUID());
        for (ItemStack stack : player.getInventory().items) {
            if (stack.getItem() instanceof SimpleDomain) {
                stack.getOrCreateTag().putBoolean(ACTIVE_TAG, false);
            }
        }
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        if (!(event.player instanceof net.minecraft.server.level.ServerPlayer player)) return;

        UUID uuid = player.getUUID();

        // Если игрок не активен, ничего не делаем
        if (!activePlayers.contains(uuid)) return;

        // Проверяем наличие активного предмета в инвентаре
        ItemStack activeItem = null;
        for (ItemStack stack : player.getInventory().items) {
            if (stack.getItem() instanceof SimpleDomain && stack.hasTag() && stack.getTag().getBoolean(ACTIVE_TAG)) {
                activeItem = stack;
                break;
            }
        }

        if (activeItem == null) {
            deactivate(player);
            return;
        }

        // Проверяем ману каждую секунду
        if (player.tickCount % 20 == 0) {
            player.getCapability(ManaProvider.MANA_CAPABILITY).ifPresent(mana -> {
                if (mana.getMana() < MANA_COST_PER_SECOND) {
                    deactivate(player);
                    player.sendSystemMessage(Component.literal("Not enough mana! Simple Domain deactivated.").withStyle(ChatFormatting.RED));
                } else {
                    mana.removeMana(MANA_COST_PER_SECOND);
                    // Накладываем эффект, видимый всем
                    player.addEffect(new MobEffectInstance(ModEffects.EFFECT_SIMPLE_DOMAIN.get(), 40, 0, false, false, true));
                }
            });
        }
    }

    @SubscribeEvent
    public static void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        activePlayers.remove(event.getEntity().getUUID());
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag isAdvanced) {
        tooltip.add(Component.translatable("item.nacikmod.simple_domain.desc1"));
        tooltip.add(Component.translatable("item.nacikmod.simple_domain.desc2", MANA_COST_PER_SECOND).withStyle(style -> style.withColor(0x00FFFF)));

        boolean active = stack.hasTag() && stack.getTag().getBoolean(ACTIVE_TAG);
        tooltip.add(Component.translatable(active ? "item.active" : "item.inactive")
                .withStyle(active ? ChatFormatting.GREEN : ChatFormatting.RED));
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return stack.hasTag() && stack.getTag().getBoolean(ACTIVE_TAG);
    }
}
