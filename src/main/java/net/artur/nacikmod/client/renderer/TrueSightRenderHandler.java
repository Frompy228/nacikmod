package net.artur.nacikmod.client.renderer;

import net.artur.nacikmod.registry.ModEffects;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.joml.Vector3f;

@Mod.EventBusSubscriber(value = Dist.CLIENT, modid = "nacikmod")
@OnlyIn(Dist.CLIENT)
public class TrueSightRenderHandler {

	@SubscribeEvent
	public static void onRenderLevelStage(RenderLevelStageEvent event) {
		if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_ENTITIES) return;

		Minecraft mc = Minecraft.getInstance();
		if (mc.player == null || mc.level == null) return;
		if (!mc.player.hasEffect(ModEffects.TRUE_SIGHT.get())) return;

		var camera = event.getCamera();
		double d0 = camera.getPosition().x();
		double d1 = camera.getPosition().y();
		double d2 = camera.getPosition().z();
		var frustum = event.getFrustum();
		EntityRenderDispatcher dispatcher = mc.getEntityRenderDispatcher();

		for (Entity entity : mc.level.entitiesForRendering()) {
			if (!(entity instanceof LivingEntity living)) continue;
			if (!living.isAlive()) continue;

			// Проверяем: должна ли сущность рендериться с точки зрения движка
			boolean wouldRender = dispatcher.shouldRender(entity, frustum, d0, d1, d2);
			if (!wouldRender) {
				// Проверяем, видна ли сущность на экране (не за блоками)
				if (isEntityVisibleOnScreen(entity, camera, mc.level)) {
					spawnParticleColumn(entity);
				}
			}
		}
	}

	private static void spawnParticleColumn(Entity entity) {
		Minecraft mc = Minecraft.getInstance();
		double cx = entity.getX();
		double cy = entity.getY();
		double cz = entity.getZ();
		double height = entity.getBbHeight();

		// Создаём белый столб частиц от земли до верха сущности
		for (int i = 0; i < 15; i++) {
			double y = cy + (height * i / 14.0);
			spawnDust(cx, y, cz, 1.0f, 1.0f, 1.0f); // Белый цвет
		}
	}

	private static void spawnDust(double x, double y, double z, float r, float g, float b) {
		Minecraft mc = Minecraft.getInstance();
		if (mc.level == null || mc.particleEngine == null) return;
		mc.particleEngine.createParticle(
				new net.minecraft.core.particles.DustParticleOptions(new Vector3f(r, g, b), 1.0f),
				x, y, z, 0.0, 0.0, 0.0
		);
	}

	/**
	 * Проверяет, находится ли сущность в поле зрения игрока
	 */
	private static boolean isEntityVisibleOnScreen(Entity entity, net.minecraft.client.Camera camera, net.minecraft.client.multiplayer.ClientLevel level) {
		// Получаем направление взгляда игрока
		double lookX = camera.getLookVector().x;
		double lookY = camera.getLookVector().y;
		double lookZ = camera.getLookVector().z;

		// Вектор от камеры к сущности
		double toEntityX = entity.getX() - camera.getPosition().x;
		double toEntityY = entity.getY() + entity.getBbHeight() / 2.0 - camera.getPosition().y;
		double toEntityZ = entity.getZ() - camera.getPosition().z;

		// Нормализуем вектор к сущности
		double distance = Math.sqrt(toEntityX * toEntityX + toEntityY * toEntityY + toEntityZ * toEntityZ);
		if (distance < 0.1) return true; // Если сущность очень близко

		toEntityX /= distance;
		toEntityY /= distance;
		toEntityZ /= distance;

		// Вычисляем угол между направлением взгляда и направлением к сущности
		double dotProduct = lookX * toEntityX + lookY * toEntityY + lookZ * toEntityZ;
		double angle = Math.acos(Math.max(-1.0, Math.min(1.0, dotProduct)));

		// Угол обзора примерно 70 градусов (чуть больше стандартного FOV)
		double maxAngle = Math.toRadians(70.0);

		return angle <= maxAngle;
	}
}
