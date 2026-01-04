package net.artur.nacikmod.client.renderer.particle;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.ResourceLocation;
import net.artur.nacikmod.NacikMod;

public class EyeParticleRenderType {
    public static final ParticleRenderType X_RAY = new ParticleRenderType() {
        private static final ResourceLocation PARTICLE_SHEET = new ResourceLocation(NacikMod.MOD_ID, "textures/particle/eye.png");

        @Override
        public void begin(BufferBuilder builder, TextureManager textureManager) {
            RenderSystem.disableDepthTest(); // 🔥 ВАЖНО - отключаем depth test
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            try {
                RenderSystem.setShaderTexture(0, PARTICLE_SHEET);
            } catch (Exception e) {
                // Если текстура не найдена, используем стандартную текстуру частиц
                RenderSystem.setShaderTexture(0, new ResourceLocation("textures/particle/particles.png"));
            }
            builder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.PARTICLE);
        }

        @Override
        public void end(Tesselator tessellator) {
            tessellator.end();
            RenderSystem.enableDepthTest(); // 🔥 ОБЯЗАТЕЛЬНО ВКЛЮЧИТЬ ОБРАТНО
        }

        @Override
        public String toString() {
            return "EYE_XRAY";
        }
    };
}
