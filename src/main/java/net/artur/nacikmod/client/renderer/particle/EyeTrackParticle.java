package net.artur.nacikmod.client.renderer.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class EyeTrackParticle extends TextureSheetParticle {

    protected EyeTrackParticle(ClientLevel level, double x, double y, double z) {
        super(level, x, y, z);
        this.lifetime = 20;
        this.gravity = 0;
        this.hasPhysics = false;
        this.quadSize = 0.05F;
        this.alpha = 0.8F;
    }

    @Override
    public net.minecraft.client.particle.ParticleRenderType getRenderType() {
        return EyeParticleRenderType.X_RAY;
    }

    @Override
    public void tick() {
        this.xo = this.x;
        this.yo = this.y;
        this.zo = this.z;

        if (this.age++ >= this.lifetime) {
            this.remove();
            return;
        }

        // Плавное исчезновение
        if (this.age > this.lifetime * 0.7F) {
            this.alpha = 0.8F * (1.0F - (float)(this.age - this.lifetime * 0.7F) / (float)(this.lifetime * 0.3F));
        }
    }

    @OnlyIn(Dist.CLIENT)
    public static class Factory implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet spriteSet;

        public Factory(SpriteSet spriteSet) {
            this.spriteSet = spriteSet;
        }

        @Override
        public Particle createParticle(SimpleParticleType type, ClientLevel level,
                                      double x, double y, double z,
                                      double xd, double yd, double zd) {
            EyeTrackParticle particle = new EyeTrackParticle(level, x, y, z);
            particle.pickSprite(spriteSet);
            return particle;
        }
    }
}
