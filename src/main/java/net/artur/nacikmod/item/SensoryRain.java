package net.artur.nacikmod.item;

import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.artur.nacikmod.capability.mana.ManaProvider;
import net.artur.nacikmod.registry.ModEffects;
import net.artur.nacikmod.registry.ModItems;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.artur.nacikmod.NacikMod;
import org.jetbrains.annotations.Nullable;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.type.capability.ICuriosItemHandler;
import top.theillusivec4.curios.api.type.inventory.ICurioStacksHandler;

import java.util.List;
import java.util.Random;

@Mod.EventBusSubscriber(modid = NacikMod.MOD_ID)
public class SensoryRain extends Item {
    private static final int MANA_COST = 1000;
    private static final int EFFECT_DURATION = 200; // 10 seconds
    private static final String ACTIVE_TAG = "active";
    private static final String TIMER_TAG = "timer";
    private static final int ABILITY_DURATION = 100; // 5 seconds (20 ticks * 5)
    private static final Random random = new Random();
    private static final int PARTICLE_RADIUS = 25; // Радиус появления частиц
    private static final int PARTICLES_PER_TICK = 40; // Количество частиц в тик

    public SensoryRain(Properties properties) {
        super(properties.fireResistant());
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack itemStack = player.getItemInHand(hand);

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

            if (!player.getCapability(ManaProvider.MANA_CAPABILITY).map(mana -> mana.getMana() >= MANA_COST).orElse(false)) {
                player.sendSystemMessage(Component.literal("Not enough mana!")
                        .withStyle(ChatFormatting.RED));
                return InteractionResultHolder.fail(itemStack);
            }

            // Устанавливаем флаг активного дождя и таймер
            itemStack.getOrCreateTag().putBoolean(ACTIVE_TAG, true);
            itemStack.getOrCreateTag().putInt(TIMER_TAG, ABILITY_DURATION);

            // Накладываем эффект TRUE_SIGHT на игрока
            player.addEffect(new net.minecraft.world.effect.MobEffectInstance(
                ModEffects.TRUE_SIGHT.get(), EFFECT_DURATION, 0, false, false, false));

            // Тратим ману
            player.getCapability(ManaProvider.MANA_CAPABILITY).ifPresent(mana -> mana.removeMana(MANA_COST));

            // Устанавливаем кулдаун
            player.getCooldowns().addCooldown(this, 260); // 5 seconds cooldown
        }

        return InteractionResultHolder.success(itemStack);
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        Player player = event.player;
        Level level = player.level();

        // Проверяем, есть ли активный предмет SensoryRain в инвентаре
        boolean hasActiveItem = false;
        ItemStack activeStack = null;
        for (ItemStack stack : player.getInventory().items) {
            if (stack.getItem() instanceof SensoryRain && 
                stack.hasTag() && 
                stack.getTag().getBoolean(ACTIVE_TAG)) {
                hasActiveItem = true;
                activeStack = stack;
                break;
            }
        }

        // Если есть активный предмет, проверяем таймер и обновляем эффект
        if (hasActiveItem && activeStack != null) {
            int timer = activeStack.getTag().getInt(TIMER_TAG);
            
            if (timer > 0) {
                // Уменьшаем таймер
                activeStack.getTag().putInt(TIMER_TAG, timer - 1);

                // Добавляем визуальные эффекты дождя
                if (level.isClientSide) {
                    // Создаем частицы дождя вокруг игрока
                    for (int i = 0; i < PARTICLES_PER_TICK; i++) {
                        double x = player.getX() + (random.nextDouble() - 0.5) * PARTICLE_RADIUS;
                        double y = player.getY() + 10 + random.nextDouble() * 10;
                        double z = player.getZ() + (random.nextDouble() - 0.5) * PARTICLE_RADIUS;
                        
                        level.addParticle(
                            ParticleTypes.RAIN,
                            x, y, z,
                            0, -0.5, 0
                        );
                    }
                }

                // Обновляем эффект TRUE_SIGHT для игрока
                if (!player.hasEffect(ModEffects.TRUE_SIGHT.get())) {
                    player.addEffect(new net.minecraft.world.effect.MobEffectInstance(
                        ModEffects.TRUE_SIGHT.get(), EFFECT_DURATION, 0, false, false, false));
                }
            } else {
                // Если таймер истек, деактивируем предмет
                activeStack.getTag().putBoolean(ACTIVE_TAG, false);
            }
        }
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltipComponents, TooltipFlag isAdvanced) {
        super.appendHoverText(stack, level, tooltipComponents, isAdvanced);

        tooltipComponents.add(Component.translatable("item.nacikmod.sensory_rain.desc1"));
        tooltipComponents.add(Component.translatable("item.nacikmod.sensory_rain.desc2")
                .withStyle(style -> style.withColor(0x00FFFF)));
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return stack.hasTag() && stack.getTag().getBoolean(ACTIVE_TAG);
    }
}
