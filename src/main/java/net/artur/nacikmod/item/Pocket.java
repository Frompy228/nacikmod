package net.artur.nacikmod.item;

import net.artur.nacikmod.capability.mana.ManaProvider;
import net.artur.nacikmod.entity.custom.GraalEntity;
import net.artur.nacikmod.registry.ModEntities;
import net.artur.nacikmod.registry.ModItems;
import net.artur.nacikmod.worldgen.dimension.ModDimensions;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.NeutralMob;
import net.minecraft.world.entity.boss.EnderDragonPart;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.util.ITeleporter;
import org.jetbrains.annotations.Nullable;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.type.inventory.ICurioStacksHandler;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

public class Pocket extends Item {
    private static final BlockPos POCKET_POS = new BlockPos(4, 108, 4);
    private static final int MANA_COST = 750;
    private static final double RANGE = 5.0;
    private static final int CHARGE_TIME = 50; // 2.5 секунды

    public Pocket(Properties properties) {
        super(properties.fireResistant().stacksTo(1));
    }

    // --- Логика использования ---

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack itemStack = player.getItemInHand(hand);

        // 1. Проверка Curios
        boolean hasDarkSphere = CuriosApi.getCuriosInventory(player)
                .map(handler -> {
                    for (ICurioStacksHandler stacksHandler : handler.getCurios().values()) {
                        for (int i = 0; i < stacksHandler.getSlots(); i++) {
                            ItemStack stack = stacksHandler.getStacks().getStackInSlot(i);
                            if (stack.getItem() == ModItems.DARK_SPHERE.get()) return true;
                        }
                    }
                    return false;
                }).orElse(false);

        if (!hasDarkSphere && !level.isClientSide) {
            player.sendSystemMessage(Component.literal("You need Dark Sphere to use this ability!").withStyle(ChatFormatting.RED));
            return InteractionResultHolder.fail(itemStack);
        }

        // 2. Shift + ПКМ: Телепортация себя
        if (player.isShiftKeyDown()) {
            if (!level.isClientSide) {
                if (!checkAndConsumeMana(player)) return InteractionResultHolder.fail(itemStack);
                teleportEntity(player, level, itemStack, player);
                player.sendSystemMessage(Component.literal(level.dimension().equals(ModDimensions.POCKET_LEVEL_KEY)
                        ? "Returned to previous dimension!" : "Teleported to Pocket!").withStyle(ChatFormatting.GREEN));
                player.getCooldowns().addCooldown(this, 20);
            }
            return InteractionResultHolder.success(itemStack);
        }

        // 3. Обычный ПКМ: Захват цели
        Entity target = getTargetedEntity(player);
        if (target != null) {
            // Очищаем старые теги перед началом нового использования
            clearTarget(itemStack);
            itemStack.getOrCreateTag().putInt("TargetEntityId", target.getId());
            player.startUsingItem(hand);
            return InteractionResultHolder.consume(itemStack);
        }

        return InteractionResultHolder.pass(itemStack);
    }

    @Override
    public void onUseTick(Level level, LivingEntity user, ItemStack stack, int remainingUseDuration) {
        if (!(user instanceof Player player)) return;

        int ticksUsed = this.getUseDuration(stack) - remainingUseDuration;
        int targetId = stack.getOrCreateTag().getInt("TargetEntityId");
        Entity target = level.getEntity(targetId);

        // --- Гибкая проверка цели ---
        if (target == null || !target.isAlive()) {
            abortTeleport(player, stack, "Target lost or dead!");
            return;
        }

        double distSqr = player.distanceToSqr(target);
        if (distSqr > RANGE * RANGE) {
            abortTeleport(player, stack, "Target is too far!");
            return;
        }

        if (!isLookingAt(player, target)) {
            abortTeleport(player, stack, "Teleport cancelled: keep aim!");
            return;
        }

        // --- Пробуждение ИИ без отталкивания ---
        if (ticksUsed % 10 == 0 && target instanceof Mob mob) {
            mob.setTarget(player);               // Устанавливаем игрока как цель
            mob.setLastHurtByPlayer(player);    // Vanilla логика агрессии
            mob.hurtTime = 0;                    // Чтобы не дергало
        }


        // --- Завершение каналинга ---
        // --- Завершение каналинга ---
        if (ticksUsed >= CHARGE_TIME) {
            // Выполняем логику действий ТОЛЬКО на сервере
            if (!level.isClientSide) {
                if (checkAndConsumeMana(player)) {
                    teleportEntity(target, level, stack, player);
                    player.sendSystemMessage(Component.literal("Teleported " + target.getName().getString() + "!")
                            .withStyle(ChatFormatting.GREEN));
                    player.getCooldowns().addCooldown(this, 60);
                }
                clearTarget(stack);
            }

            // Останавливаем использование на ОБЕИХ сторонах
            player.stopUsingItem();
        }
    }


    // Важно: Вызывается, если игрок отпустил кнопку ПКМ раньше времени
    @Override
    public void releaseUsing(ItemStack stack, Level level, LivingEntity livingEntity, int timeCharged) {
        if (livingEntity instanceof Player player && !level.isClientSide) {
            // Просто очищаем цель, сообщение можно не писать, так как игрок сам отменил
            clearTarget(stack);
        }
        super.releaseUsing(stack, level, livingEntity, timeCharged);
    }

    // --- Вспомогательные методы ---

    private void abortTeleport(Player player, ItemStack stack, String message) {
        player.stopUsingItem();
        clearTarget(stack); // Обязательно чистим тег
        player.sendSystemMessage(Component.literal(message).withStyle(ChatFormatting.RED));
    }

    private void clearTarget(ItemStack stack) {
        if (stack.hasTag()) {
            stack.getTag().remove("TargetEntityId");
        }
    }

    // Проверяет, смотрит ли игрок на конкретную сущность (более оптимизировано для тика)
    private boolean isLookingAt(Player player, Entity target) {
        Vec3 eyePos = player.getEyePosition(1.0F);
        Vec3 lookVec = player.getLookAngle();
        Vec3 endPos = eyePos.add(lookVec.x * RANGE, lookVec.y * RANGE, lookVec.z * RANGE);

        // Расширяем хитбокс цели чуть-чуть, чтобы прицел не сбивался слишком легко
        AABB entityBox = target.getBoundingBox().inflate(0.5D);
        return entityBox.clip(eyePos, endPos).isPresent();
    }

    @Nullable
    private Entity getTargetedEntity(Player player) {
        Vec3 eyePos = player.getEyePosition(1.0F);
        Vec3 lookVec = player.getLookAngle();
        Vec3 endPos = eyePos.add(lookVec.x * RANGE, lookVec.y * RANGE, lookVec.z * RANGE);
        AABB searchBox = player.getBoundingBox().expandTowards(lookVec.x * RANGE, lookVec.y * RANGE, lookVec.z * RANGE).inflate(1.0D);

        Entity targetEntity = null;
        double minDistance = RANGE;

        for (Entity entity : player.level().getEntities(player, searchBox, e -> e != player && e.isAlive() && e.isPickable())) {
            if (entity instanceof EnderDragon || entity instanceof EnderDragonPart) continue;
            if (entity instanceof GraalEntity || entity.getType() == ModEntities.GRAIL.get()) continue;

            AABB entityBox = entity.getBoundingBox().inflate(0.3D);
            Optional<Vec3> hitVec = entityBox.clip(eyePos, endPos);

            if (hitVec.isPresent()) {
                double distance = eyePos.distanceTo(hitVec.get());
                if (distance < minDistance) {
                    minDistance = distance;
                    targetEntity = entity;
                }
            }
        }
        return targetEntity;
    }

    // --- Стандартные методы Item и Teleporter ---

    @Override
    public int getUseDuration(ItemStack stack) {
        return 72000;
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.BOW;
    }

    private boolean checkAndConsumeMana(Player player) {
        var manaCap = player.getCapability(ManaProvider.MANA_CAPABILITY).orElse(null);
        if (manaCap != null && manaCap.getMana() >= MANA_COST) {
            manaCap.removeMana(MANA_COST);
            return true;
        }
        player.sendSystemMessage(Component.literal("Not enough mana!").withStyle(ChatFormatting.RED));
        return false;
    }

    private static class SimpleTeleporter implements ITeleporter {
        private final BlockPos targetPos;

        public SimpleTeleporter(BlockPos targetPos) {
            this.targetPos = targetPos;
        }

        @Override
        public Entity placeEntity(Entity entity, ServerLevel currentWorld, ServerLevel destinationWorld,
                                  float yaw, Function<Boolean, Entity> repositionEntity) {
            entity = repositionEntity.apply(false);
            entity.teleportTo(targetPos.getX() + 0.5, targetPos.getY(), targetPos.getZ() + 0.5);
            return entity;
        }
    }

    private void teleportEntity(Entity entity, Level level, ItemStack itemStack, Player teleporter) {
        if (!level.isClientSide && level instanceof ServerLevel serverLevel) {
            if (level.dimension().equals(ModDimensions.POCKET_LEVEL_KEY)) {
                String sourceDim = itemStack.getOrCreateTag().getString("SourceDimension");
                if (!sourceDim.isEmpty()) {
                    ResourceKey<Level> sourceDimension = ResourceKey.create(Registries.DIMENSION, new ResourceLocation(sourceDim));
                    ServerLevel targetLevel = serverLevel.getServer().getLevel(sourceDimension);
                    if (targetLevel != null) {
                        BlockPos returnPos = new BlockPos(
                                itemStack.getOrCreateTag().getInt("ReturnX"),
                                itemStack.getOrCreateTag().getInt("ReturnY"),
                                itemStack.getOrCreateTag().getInt("ReturnZ")
                        );
                        entity.changeDimension(targetLevel, new SimpleTeleporter(returnPos));
                    }
                }
            } else {
                ServerLevel spartaLevel = serverLevel.getServer().getLevel(ModDimensions.POCKET_LEVEL_KEY);
                if (spartaLevel != null) {
                    itemStack.getOrCreateTag().putString("SourceDimension", level.dimension().location().toString());
                    itemStack.getOrCreateTag().putInt("ReturnX", entity.blockPosition().getX());
                    itemStack.getOrCreateTag().putInt("ReturnY", entity.blockPosition().getY());
                    itemStack.getOrCreateTag().putInt("ReturnZ", entity.blockPosition().getZ());
                    entity.changeDimension(spartaLevel, new SimpleTeleporter(POCKET_POS));
                }
            }
        }
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltipComponents, TooltipFlag isAdvanced) {
        super.appendHoverText(stack, level, tooltipComponents, isAdvanced);
        tooltipComponents.add(Component.translatable("item.nacikmod.pocket.desc1"));
        tooltipComponents.add(Component.translatable("item.nacikmod.pocket.desc2").withStyle(style -> style.withColor(0x00FFFF)));
        tooltipComponents.add(Component.translatable("item.nacikmod.pocket.desc3").withStyle(ChatFormatting.GRAY));
        tooltipComponents.add(Component.translatable("item.nacikmod.pocket.desc4").withStyle(ChatFormatting.GRAY));
    }
}