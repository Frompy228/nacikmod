package net.artur.nacikmod.client.renderer;

import net.artur.nacikmod.NacikMod;
import net.artur.nacikmod.item.BarrierSeal;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.List;

@OnlyIn(Dist.CLIENT)
@Mod.EventBusSubscriber(value = Dist.CLIENT, modid = NacikMod.MOD_ID)
public class BarrierRenderHandler {

    // radius must match server logic (informational only)
    private static final double BARRIER_RADIUS = 20.0;
    // step controls particle sparsity along edges (bigger = fewer particles)
    private static final double PARTICLE_STEP = 2.0;

    @SubscribeEvent
    public static void onRenderLevel(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) return;

        // Проверяем, что BarrierSeal в руке (main hand или off hand)
        ItemStack mainHand = mc.player.getMainHandItem();
        ItemStack offHand = mc.player.getOffhandItem();
        
        ItemStack barrierSeal = null;
        if (mainHand.getItem() instanceof BarrierSeal && BarrierSeal.isOwner(mainHand, mc.player)) {
            barrierSeal = mainHand;
        } else if (offHand.getItem() instanceof BarrierSeal && BarrierSeal.isOwner(offHand, mc.player)) {
            barrierSeal = offHand;
        }

        // Частицы показываются только если предмет в руке
        if (barrierSeal == null) return;

        // Получаем только активные барьеры
        List<BlockPos> positions = BarrierSeal.getBarrierPositions(barrierSeal);
        if (positions == null || positions.isEmpty()) return;

        ClientLevel level = mc.level;

        // Рендерим только активные барьеры владельца (getBarrierPositions уже возвращает только активные)
        for (BlockPos pos : positions) {
            if (pos == null) continue;
            Vec3 center = new Vec3(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5);
            spawnBoundaryParticles(level, center, ParticleTypes.END_ROD);
        }
    }

    // Spawn particles only on the cube edges (12 edges of a cube)
    private static void spawnBoundaryParticles(ClientLevel level, Vec3 center, ParticleOptions particle) {
        double r = BARRIER_RADIUS;
        double step = PARTICLE_STEP;

        // 4 vertical edges (parallel to Y axis)
        // Front-left vertical edge
        for (double y = -r; y <= r; y += step) {
            level.addParticle(particle, center.x - r, center.y + y, center.z - r, 0.0, 0.0, 0.0);
        }
        // Front-right vertical edge
        for (double y = -r; y <= r; y += step) {
            level.addParticle(particle, center.x + r, center.y + y, center.z - r, 0.0, 0.0, 0.0);
        }
        // Back-left vertical edge
        for (double y = -r; y <= r; y += step) {
            level.addParticle(particle, center.x - r, center.y + y, center.z + r, 0.0, 0.0, 0.0);
        }
        // Back-right vertical edge
        for (double y = -r; y <= r; y += step) {
            level.addParticle(particle, center.x + r, center.y + y, center.z + r, 0.0, 0.0, 0.0);
        }

        // 4 horizontal edges on top face (y = +r)
        // Top front edge (parallel to X axis)
        for (double x = -r; x <= r; x += step) {
            level.addParticle(particle, center.x + x, center.y + r, center.z - r, 0.0, 0.0, 0.0);
        }
        // Top back edge (parallel to X axis)
        for (double x = -r; x <= r; x += step) {
            level.addParticle(particle, center.x + x, center.y + r, center.z + r, 0.0, 0.0, 0.0);
        }
        // Top left edge (parallel to Z axis)
        for (double z = -r; z <= r; z += step) {
            level.addParticle(particle, center.x - r, center.y + r, center.z + z, 0.0, 0.0, 0.0);
        }
        // Top right edge (parallel to Z axis)
        for (double z = -r; z <= r; z += step) {
            level.addParticle(particle, center.x + r, center.y + r, center.z + z, 0.0, 0.0, 0.0);
        }

        // 4 horizontal edges on bottom face (y = -r)
        // Bottom front edge (parallel to X axis)
        for (double x = -r; x <= r; x += step) {
            level.addParticle(particle, center.x + x, center.y - r, center.z - r, 0.0, 0.0, 0.0);
        }
        // Bottom back edge (parallel to X axis)
        for (double x = -r; x <= r; x += step) {
            level.addParticle(particle, center.x + x, center.y - r, center.z + r, 0.0, 0.0, 0.0);
        }
        // Bottom left edge (parallel to Z axis)
        for (double z = -r; z <= r; z += step) {
            level.addParticle(particle, center.x - r, center.y - r, center.z + z, 0.0, 0.0, 0.0);
        }
        // Bottom right edge (parallel to Z axis)
        for (double z = -r; z <= r; z += step) {
            level.addParticle(particle, center.x + r, center.y - r, center.z + z, 0.0, 0.0, 0.0);
        }
    }
}
