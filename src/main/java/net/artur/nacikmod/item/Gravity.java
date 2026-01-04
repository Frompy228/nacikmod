package net.artur.nacikmod.item;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.artur.nacikmod.capability.mana.ManaProvider;
import net.artur.nacikmod.registry.ModEffects;
import net.artur.nacikmod.registry.ModItems;
import net.artur.nacikmod.registry.ModSounds;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.artur.nacikmod.NacikMod;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.EntityItemPickupEvent;
import org.jetbrains.annotations.Nullable;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.type.capability.ICuriosItemHandler;
import top.theillusivec4.curios.api.type.inventory.ICurioStacksHandler;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Mod.EventBusSubscriber(modid = NacikMod.MOD_ID)
public class Gravity extends Item {
    private static final int MANA_COST_PER_SECOND = 20;
    private static final int MANA_COST_EFFECT = 500;
    private static final int EFFECT_RADIUS = 10;
    private static final int EFFECT_DURATION = 80;
    private static final String ACTIVE_TAG = "active";
    private static final Set<UUID> activeFlyingPlayers = new HashSet<>();

    public Gravity(Properties properties) {
        super(properties.fireResistant());
    }

    @SubscribeEvent
    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            // Проверяем все предметы Gravity в инвентаре
            for (ItemStack stack : player.getInventory().items) {
                if (stack.getItem() instanceof Gravity) {
                    // Если предмет помечен как активный, но игрок не в списке активных
                    if (stack.hasTag() && stack.getTag().getBoolean(ACTIVE_TAG) && !activeFlyingPlayers.contains(player.getUUID())) {
                        stack.getOrCreateTag().putBoolean(ACTIVE_TAG, false);
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public static void onItemPickup(EntityItemPickupEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        
        ItemStack pickedStack = event.getItem().getItem();
        if (pickedStack.getItem() instanceof Gravity) {
            // Если игрок не в списке активных, но предмет помечен как активный
            if (!activeFlyingPlayers.contains(player.getUUID()) && 
                pickedStack.hasTag() && 
                pickedStack.getTag().getBoolean(ACTIVE_TAG)) {
                pickedStack.getOrCreateTag().putBoolean(ACTIVE_TAG, false);
            }
        }
    }

    public static void startFlying(Player player) {
        activeFlyingPlayers.add(player.getUUID());
        player.getAbilities().mayfly = true;
        player.getAbilities().flying = true;
        player.onUpdateAbilities();
        
        // Находим предмет в инвентаре и помечаем его как активный
        for (ItemStack stack : player.getInventory().items) {
            if (stack.getItem() instanceof Gravity) {
                stack.getOrCreateTag().putBoolean(ACTIVE_TAG, true);
                break;
            }
        }
    }

    public static void stopFlying(Player player) {
        activeFlyingPlayers.remove(player.getUUID());
        player.getAbilities().mayfly = false;
        player.getAbilities().flying = false;
        player.onUpdateAbilities();
        
        // Находим предмет в инвентаре и помечаем его как неактивный
        for (ItemStack stack : player.getInventory().items) {
            if (stack.getItem() instanceof Gravity) {
                stack.getOrCreateTag().putBoolean(ACTIVE_TAG, false);
            }
        }
    }

    public static boolean isFlyingActive(Player player) {
        return activeFlyingPlayers.contains(player.getUUID());
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack itemStack = player.getItemInHand(hand);
        boolean isShiftKeyDown = player.isShiftKeyDown();

        if (!level.isClientSide) {
            // Проверяем наличие Dark Sphere в слотах Curios используя новый API
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

            if (isShiftKeyDown) {
                // Применение эффекта усиленной гравитации
                if (!player.getCapability(ManaProvider.MANA_CAPABILITY).map(mana -> mana.getMana() >= MANA_COST_EFFECT).orElse(false)) {
                    player.sendSystemMessage(Component.literal("Not enough mana!")
                            .withStyle(ChatFormatting.RED));
                    return InteractionResultHolder.fail(itemStack);
                }

                // Получаем все сущности в радиусе
                AABB area = new AABB(
                    player.getX() - EFFECT_RADIUS, player.getY() - EFFECT_RADIUS, player.getZ() - EFFECT_RADIUS,
                    player.getX() + EFFECT_RADIUS, player.getY() + EFFECT_RADIUS, player.getZ() + EFFECT_RADIUS
                );
                List<LivingEntity> entities = level.getEntitiesOfClass(LivingEntity.class, area);

                // Создаем эффект распространения партиклов
                for (double radius = 0; radius <= EFFECT_RADIUS; radius += 0.5) {
                    for (double angle = 0; angle < 360; angle += 10) {
                        double x = player.getX() + radius * Math.cos(Math.toRadians(angle));
                        double z = player.getZ() + radius * Math.sin(Math.toRadians(angle));
                        
                        ((ServerLevel) level).sendParticles(
                            net.minecraft.core.particles.ParticleTypes.ENCHANTED_HIT,
                            x, player.getY() + 0.2, z,
                            1, 0, 0, 0, 0
                        );
                    }
                }

                // Применяем эффект ко всем сущностям кроме игрока
                for (LivingEntity entity : entities) {
                    if (entity != player) {
                        entity.addEffect(new net.minecraft.world.effect.MobEffectInstance(
                            ModEffects.ENHANCED_GRAVITY.get(), EFFECT_DURATION, 0, false, false, false));
                        
                        // Добавляем партиклы
                        for (int i = 0; i < 8; i++) {
                            double offsetX = (entity.getRandom().nextDouble() - 0.5) * entity.getBbWidth();
                            double offsetY = entity.getRandom().nextDouble() * entity.getBbHeight();
                            double offsetZ = (entity.getRandom().nextDouble() - 0.5) * entity.getBbWidth();
                            
                            ((ServerLevel) level).sendParticles(
                                net.minecraft.core.particles.ParticleTypes.ENCHANTED_HIT,
                                entity.getX() + offsetX,
                                entity.getY() + offsetY,
                                entity.getZ() + offsetZ,
                                1, 0, 0, 0, 0
                            );
                        }
                    }
                }

                // Проигрываем звук
                level.playSound(null, player.getX(), player.getY(), player.getZ(), 
                    ModSounds.GRAVITY.get(), SoundSource.PLAYERS, 1.0F, 1.0F);

                // Тратим ману
                player.getCapability(ManaProvider.MANA_CAPABILITY).ifPresent(mana -> mana.removeMana(MANA_COST_EFFECT));

                // Устанавливаем кулдаун на 5 секунд (100 тиков)
                player.getCooldowns().addCooldown(this, 140);
            } else {
                // Переключение полета
                if (!player.getCapability(ManaProvider.MANA_CAPABILITY).map(mana -> mana.getMana() >= MANA_COST_PER_SECOND).orElse(false)) {
                    player.sendSystemMessage(Component.literal("Not enough mana!")
                            .withStyle(ChatFormatting.RED));
                    return InteractionResultHolder.fail(itemStack);
                }

                if (isFlyingActive(player)) {
                    stopFlying(player);
                } else {
                    startFlying(player);
                }

                // Устанавливаем кулдаун на 1 секунду (20 тиков)
                player.getCooldowns().addCooldown(this, 20);
            }
        }

        return InteractionResultHolder.success(itemStack);
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        Player player = event.player;

        // Проверяем наличие Dark Sphere
        boolean hasDarkSphere = CuriosApi.getCuriosInventory(player)
                .map(handler -> {
                    for (ICurioStacksHandler stacksHandler : handler.getCurios().values()) {
                        for (int i = 0; i < stacksHandler.getSlots(); i++) {
                            ItemStack curiosStack = stacksHandler.getStacks().getStackInSlot(i);
                            if (curiosStack.getItem() == ModItems.DARK_SPHERE.get()) {
                                return true;
                            }
                        }
                    }
                    return false;
                })
                .orElse(false);
        if (!hasDarkSphere) {
            if (isFlyingActive(player)) {
                stopFlying(player);
            }
            // Сбросить ACTIVE_TAG у всех Gravity (на всякий случай)
            for (ItemStack stack : player.getInventory().items) {
                if (stack.getItem() instanceof Gravity) {
                    stack.getOrCreateTag().putBoolean(ACTIVE_TAG, false);
                }
            }
            return;
        }

        // Проверяем все предметы Gravity в инвентаре
        boolean hasActiveItem = false;
        for (ItemStack stack : player.getInventory().items) {
            if (stack.getItem() instanceof Gravity) {
                if (stack.hasTag() && stack.getTag().getBoolean(ACTIVE_TAG)) {
                    hasActiveItem = true;
                } else if (activeFlyingPlayers.contains(player.getUUID())) {
                    // Если предмет помечен как неактивный, но игрок в списке активных
                    stack.getOrCreateTag().putBoolean(ACTIVE_TAG, false);
                }
            }
        }

        // Если нет активного предмета, но игрок в списке активных
        if (!hasActiveItem && activeFlyingPlayers.contains(player.getUUID())) {
            stopFlying(player);
            return;
        }

        // Если эффект активен и предмет есть в инвентаре
        if (activeFlyingPlayers.contains(player.getUUID()) && hasActiveItem) {
            // Проверяем ману
            if (!player.getCapability(ManaProvider.MANA_CAPABILITY).map(mana -> mana.getMana() >= MANA_COST_PER_SECOND).orElse(false)) {
                stopFlying(player);
            } else {
                // Тратим ману каждую секунду (20 тиков)
                if (player.tickCount % 20 == 0) {
                    player.getCapability(ManaProvider.MANA_CAPABILITY).ifPresent(mana -> mana.removeMana(MANA_COST_PER_SECOND));
                }
            }
        }
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltipComponents, TooltipFlag isAdvanced) {
        super.appendHoverText(stack, level, tooltipComponents, isAdvanced);

        tooltipComponents.add(Component.translatable("item.nacikmod.gravity.desc1"));
        tooltipComponents.add(Component.translatable("item.nacikmod.gravity.desc2")
                .withStyle(style -> style.withColor(0x00FFFF)));

        boolean isActive = stack.hasTag() && stack.getTag().getBoolean(ACTIVE_TAG);
        if (isActive) {
            tooltipComponents.add(Component.translatable("item.active")
                    .withStyle(ChatFormatting.GREEN));
        } else {
            tooltipComponents.add(Component.translatable("item.inactive")
                    .withStyle(ChatFormatting.RED));
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
        return stack.hasTag() && stack.getTag().getBoolean(ACTIVE_TAG);
    }
}
