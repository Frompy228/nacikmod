package net.artur.nacikmod.item.ability;

import net.artur.nacikmod.NacikMod;
import net.artur.nacikmod.item.BloodContract;
import net.artur.nacikmod.capability.mana.ManaProvider;
import net.artur.nacikmod.util.ItemUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.joml.Vector3f;

import java.util.*;

@Mod.EventBusSubscriber(modid = NacikMod.MOD_ID)
public class BloodContractManager {
    public static final int TICK_DRAIN_COST = 5;
    private static final double MAX_DISTANCE = 100.0;
    private static final double CIRCLE_AOE_RANGE = 15.0;
    
    private static final Map<UUID, UUID> CONTRACT_TARGETS = new HashMap<>();
    private static final Set<UUID> ACTIVE_CONTRACTS = new HashSet<>();

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || event.player.level().isClientSide) return;
        ServerPlayer player = (ServerPlayer) event.player;

        if (isContractActive(player)) {
            // 1. Проверка предмета
            ItemStack activeStack = ItemUtils.findActiveItem(player, BloodContract.class);
            if (activeStack == null) {
                breakContract(player, "Contract Lost: Item missing");
                return;
            }

            // 2. Списание маны
            player.getCapability(ManaProvider.MANA_CAPABILITY).ifPresent(mana -> {
                if (mana.getMana() < TICK_DRAIN_COST) {
                    breakContract(player, "Contract Broken: Out of mana");
                } else if (player.tickCount % 20 == 0) {
                    mana.removeMana(TICK_DRAIN_COST);
                }
            });

            // 3. Логика связи (Одиночная или Сетевая)
            boolean circleActive = BloodCircleManager.isActive(player);

            if (circleActive) {
                handleAoeContract(player);
            } else {
                handleSingleContract(player);
            }
        }
    }

    private static void handleSingleContract(ServerPlayer player) {
        UUID targetUUID = getContractTarget(player.getUUID());
        if (targetUUID == null) return;
        
        Entity target = ((ServerLevel)player.level()).getEntity(targetUUID);

        if (target == null || !target.isAlive() || player.distanceTo(target) > MAX_DISTANCE) {
            breakContract(player, "Target Lost");
        } else {
            if (player.tickCount % 5 == 0) {
                spawnConnectionLine(player, (LivingEntity) target, new Vector3f(0.8f, 0f, 0f)); // Красная линия
            }
        }
    }

    private static void handleAoeContract(ServerPlayer player) {
        AABB area = player.getBoundingBox().inflate(CIRCLE_AOE_RANGE);
        List<LivingEntity> entities = player.level().getEntitiesOfClass(LivingEntity.class, area,
                e -> e != player && !e.isSpectator() && e.isAlive());

        for (LivingEntity entity : entities) {
            if (player.tickCount % 10 == 0) {
                spawnConnectionLine(player, entity, new Vector3f(0.5f, 0f, 0.5f)); // Фиолетовая линия (бафф круга)
            }
            // Тут можно добавить механику: например, передача урона от одного всем остальным
        }
    }

    public static void activateContract(ServerPlayer player, LivingEntity target) {
        UUID playerUUID = player.getUUID();
        CONTRACT_TARGETS.put(playerUUID, target.getUUID());
        ACTIVE_CONTRACTS.add(playerUUID);
    }

    public static void breakContract(ServerPlayer player, String reason) {
        UUID playerUUID = player.getUUID();
        CONTRACT_TARGETS.remove(playerUUID);
        ACTIVE_CONTRACTS.remove(playerUUID);
        
        // Обновляем состояние всех предметов BloodContract в инвентаре
        for (ItemStack stack : player.getInventory().items) {
            if (stack.getItem() instanceof BloodContract) {
                stack.getOrCreateTag().putBoolean("active", false);
            }
        }
        
        if (reason != null) {
            player.sendSystemMessage(Component.literal(reason).withStyle(ChatFormatting.RED));
        }
    }

    public static boolean isContractActive(Player player) {
        return ACTIVE_CONTRACTS.contains(player.getUUID());
    }

    public static UUID getContractTarget(UUID playerUUID) {
        return CONTRACT_TARGETS.get(playerUUID);
    }


    private static void spawnConnectionLine(ServerPlayer player, LivingEntity target, Vector3f color) {
        ServerLevel level = player.serverLevel();
        Vec3 start = player.position().add(0, player.getBbHeight() / 1.5, 0);
        Vec3 end = target.position().add(0, target.getBbHeight() / 1.5, 0);

        double dist = start.distanceTo(end);
        int points = (int) (dist * 2);
        DustParticleOptions particle = new DustParticleOptions(color, 0.7f);

        for (int i = 0; i <= points; i++) {
            double r = (double) i / points;
            level.sendParticles(particle,
                    start.x + (end.x - start.x) * r,
                    start.y + (end.y - start.y) * r,
                    start.z + (end.z - start.z) * r,
                    1, 0, 0, 0, 0);
        }
    }
}
