// ByakuganParticleRenderType.java
package net.artur.nacikmod.client.renderer.particle;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.ResourceLocation;

public class ByakuganParticleRenderType implements ParticleRenderType {
    public static final ByakuganParticleRenderType INSTANCE = new ByakuganParticleRenderType();
    private static final ResourceLocation PARTICLE_SHEET = new ResourceLocation("nacikmod:textures/particle/byakugan.png");

    @Override
    public void begin(BufferBuilder buffer, TextureManager textureManager) {
        // Отключаем тест глубины для видимости сквозь стены
        RenderSystem.disableDepthTest();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShaderTexture(0, PARTICLE_SHEET);
        buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.PARTICLE);
    }

    @Override
    public void end(Tesselator tesselator) {
        tesselator.end();
        // Включаем тест глубины обратно
        RenderSystem.enableDepthTest();
    }

    @Override
    public String toString() {
        return "BYAKUGAN_PARTICLE";
    }
}