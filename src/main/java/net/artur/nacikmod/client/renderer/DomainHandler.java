package net.artur.nacikmod.client.renderer;

import net.artur.nacikmod.registry.ModEffects;
import net.minecraft.client.Minecraft;
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
public class DomainHandler {

	@SubscribeEvent
	public static void onRenderLevelStage(RenderLevelStageEvent event) {
		if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_ENTITIES) return;

		Minecraft mc = Minecraft.getInstance();
		if (mc.player == null || mc.level == null) return;
		if (!mc.player.hasEffect(ModEffects.EFFECT_DOMAIN.get())) return;

		for (Entity entity : mc.level.entitiesForRendering()) {
			if (!(entity instanceof LivingEntity living)) continue;
			if (!living.isAlive()) continue;
			if (living == mc.player) continue;


			if (living.distanceToSqr(mc.player) <= 10.0D) {
				spawnSubtlePossessionParticles(living);
			}
		}
	}

	private static void spawnSubtlePossessionParticles(Entity entity) {
		Minecraft mc = Minecraft.getInstance();
		if (mc.level == null || mc.particleEngine == null) return;

		double x = entity.getX();
		double y = entity.getY() + entity.getBbHeight() * 0.6; // чуть выше центра
		double z = entity.getZ();

		// Очень мало частиц: 1-2 за кадр возле сущности
		var rand = entity.level().random;
		spawnDust(x + (rand.nextDouble() - 0.5) * 0.3,
				y + (rand.nextDouble() - 0.5) * 0.3,
				z + (rand.nextDouble() - 0.5) * 0.3,
				0.0f, 1.0f, 1.0f); // бирюзовый, как в описании предмета

		if (rand.nextBoolean()) {
			spawnDust(x, y + 0.2, z, 0.0f, 1.0f, 1.0f);
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
}
