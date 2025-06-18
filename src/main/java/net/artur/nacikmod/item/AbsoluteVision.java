package net.artur.nacikmod.item;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.artur.nacikmod.client.MoonTextureManager;
import net.artur.nacikmod.capability.mana.ManaProvider;
import net.artur.nacikmod.registry.ModItems;
import org.jetbrains.annotations.Nullable;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.type.inventory.ICurioStacksHandler;

import java.util.List;

public class AbsoluteVision extends Item {
    private static final int NIGHT_TIME = 13000; // Время ночи в тиках (13000 = полночь)
    private static final int COOLDOWN_TICKS = 15000; // 12.5 минут кулдауна
    private static final int MANA_COST = 3000; // Стоимость маны

    public AbsoluteVision(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack itemStack = player.getItemInHand(hand);

        if (!level.isClientSide && level instanceof ServerLevel serverLevel) {
            // Проверяем, не находится ли игрок в кулдауне
            if (player.getCooldowns().isOnCooldown(this)) {
                player.sendSystemMessage(Component.literal("Item is on cooldown!")
                        .withStyle(ChatFormatting.RED));
                return InteractionResultHolder.fail(itemStack);
            }

            // Проверяем наличие Dark Sphere в слотах Curios
            boolean hasDarkSphere = CuriosApi.getCuriosInventory(player)
                    .map(handler -> {
                        for (ICurioStacksHandler stacksHandler : handler.getCurios().values()) {
                            for (int i = 0; i < stacksHandler.getSlots(); i++) {
                                ItemStack stack = stacksHandler.getStacks().getStackInSlot(i);
                                if (stack.getItem() == ModItems.DARK_SPHERE.get()) {
                                    return true;
                                }
                            }
                        }
                        return false;
                    })
                    .orElse(false);

            if (!hasDarkSphere) {
                player.sendSystemMessage(Component.literal("You need Dark Sphere to use this ability!")
                        .withStyle(ChatFormatting.RED));
                return InteractionResultHolder.fail(itemStack);
            }

            // Проверяем наличие маны
            if (!player.getCapability(ManaProvider.MANA_CAPABILITY).map(mana -> mana.getMana() >= MANA_COST).orElse(false)) {
                player.sendSystemMessage(Component.literal("Not enough mana!")
                        .withStyle(ChatFormatting.RED));
                return InteractionResultHolder.fail(itemStack);
            }

            // Используем ману
            player.getCapability(ManaProvider.MANA_CAPABILITY).ifPresent(mana -> mana.removeMana(MANA_COST));

            // Устанавливаем время на ночь
            serverLevel.setDayTime(NIGHT_TIME);

            // Отправляем координаты всех игроков
            player.sendSystemMessage(Component.literal("=== Player Coordinates ===")
                    .withStyle(ChatFormatting.GOLD));
            
            for (ServerPlayer serverPlayer : serverLevel.getServer().getPlayerList().getPlayers()) {
                String playerName = serverPlayer.getName().getString();
                int x = (int) serverPlayer.getX();
                int y = (int) serverPlayer.getY();
                int z = (int) serverPlayer.getZ();
                String dimension = serverPlayer.level().dimension().location().getPath();
                
                player.sendSystemMessage(Component.literal(String.format("%s: %d, %d, %d (%s)", 
                        playerName, x, y, z, dimension))
                        .withStyle(ChatFormatting.YELLOW));
            }

            // Устанавливаем кулдаун
            player.getCooldowns().addCooldown(this, COOLDOWN_TICKS);

            // Воспроизводим звук
            level.playSound(null, player.blockPosition(), 
                net.minecraft.sounds.SoundEvents.ENDERMAN_TELEPORT, 
                net.minecraft.sounds.SoundSource.PLAYERS, 1.0F, 1.0F);
        } else if (level.isClientSide) {
            // На клиенте включаем магическую ночь
            MoonTextureManager.activateMagicNight();
        }

        return InteractionResultHolder.success(itemStack);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltipComponents, TooltipFlag isAdvanced) {
        super.appendHoverText(stack, level, tooltipComponents, isAdvanced);
        tooltipComponents.add(Component.translatable("item.nacikmod.absolute_vision.desc1"));
        tooltipComponents.add(Component.translatable("item.nacikmod.absolute_vision.desc2")
                .withStyle(style -> style.withColor(0x00FFFF)));
    }
}
