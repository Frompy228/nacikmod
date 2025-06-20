package net.artur.nacikmod.effect;

import net.artur.nacikmod.registry.ModEffects;
import net.minecraft.client.Minecraft;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.ViewportEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(value = Dist.CLIENT, modid = "nacikmod")
public class EffectTrueSight extends MobEffect {

    public EffectTrueSight() {
        super(MobEffectCategory.BENEFICIAL, 0x00FFFF);
    }

    @Override
    public void removeAttributeModifiers(LivingEntity pLivingEntity, AttributeMap pAttributeMap, int pAmplifier) {
        super.removeAttributeModifiers(pLivingEntity, pAttributeMap, pAmplifier);
        // Здесь можно добавить логику для удаления эффектов, связанных с TrueSight
    }

    @Override
    public void addAttributeModifiers(LivingEntity pLivingEntity, AttributeMap pAttributeMap, int pAmplifier) {
        super.addAttributeModifiers(pLivingEntity, pAttributeMap, pAmplifier);
        // Здесь можно добавить логику для применения эффектов, связанных с TrueSight
    }

    @Override
    public boolean isDurationEffectTick(int pDuration, int pAmplifier) {
        return true;
    }

    @Override
    public void applyEffectTick(LivingEntity livingEntity, int pAmplifier) {
        if (livingEntity.level().isClientSide && livingEntity == Minecraft.getInstance().player) {
            // Создаем различные частицы для визуального эффекта TrueSight
            for (int i = 0; i < 3; i++) {
                Vec3 pos = new Vec3(
                    livingEntity.getRandom().nextDouble() * 40 - 20, 
                    livingEntity.getRandom().nextDouble() * 15 + 2, 
                    livingEntity.getRandom().nextDouble() * 40 - 20
                ).add(livingEntity.position());
                
                Vec3 random = new Vec3(
                    livingEntity.getRandom().nextDouble() * 0.2 - 0.1, 
                    livingEntity.getRandom().nextDouble() * 0.2 - 0.1, 
                    livingEntity.getRandom().nextDouble() * 0.2 - 0.1
                );
                
                // Чередуем разные типы частиц для более интересного эффекта
                if (i % 2 == 0) {
                    livingEntity.level().addParticle(ParticleTypes.END_ROD, pos.x, pos.y, pos.z, random.x, random.y, random.z);
                } else {
                    livingEntity.level().addParticle(ParticleTypes.FIREWORK, pos.x, pos.y, pos.z, random.x, random.y, random.z);
                }
            }
        }
    }

    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent
    public static void onFogRender(ViewportEvent.RenderFog event) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player != null && minecraft.player.hasEffect(ModEffects.TRUE_SIGHT.get())) {
            // Значительно увеличиваем дальность видимости для TrueSight
            event.setNearPlaneDistance(0.05f);
            event.setFarPlaneDistance(300.0f);
            event.setCanceled(true);
        }
    }

    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent
    public static void onFogColor(ViewportEvent.ComputeFogColor event) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player != null && minecraft.player.hasEffect(ModEffects.TRUE_SIGHT.get())) {
            // Делаем туман практически прозрачным для TrueSight
            event.setRed(event.getRed() * 0.3f);
            event.setGreen(event.getGreen() * 0.3f);
            event.setBlue(event.getBlue() * 0.3f);
        }
    }
}
