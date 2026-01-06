// SimpleTrackParticle.java (новый, простой класс)
package net.artur.nacikmod.client.renderer.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class SimpleTrackParticle extends TextureSheetParticle {

    protected SimpleTrackParticle(ClientLevel level, double x, double y, double z,
                                  SpriteSet spriteSet, float red, float green, float blue) {
        super(level, x, y, z);

        // Устанавливаем цвет
        this.setColor(red, green, blue);

        // Размер частицы
        this.setSize(0.2F, 0.2F);
        this.quadSize = 0.2F;

        // Время жизни (в тиках)
        this.lifetime = 30;

        // Отключаем гравитацию
        this.gravity = 0.0F;
        this.hasPhysics = false;

        // Полупрозрачность
        this.alpha = 0.7F;

        // Устанавливаем спрайт
        this.pickSprite(spriteSet);
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
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

        // Медленное движение вверх
        this.yd += 0.001;
        this.move(this.xd, this.yd, this.zd);

        // Плавное исчезновение
        if (this.age > this.lifetime * 0.7F) {
            this.alpha = 0.7F * (1.0F - (float)(this.age - this.lifetime * 0.7F) / (float)(this.lifetime * 0.3F));
        }
    }

    // Фабрики для создания частиц
    @OnlyIn(Dist.CLIENT)
    public static class PlayerFactory implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprites;

        public PlayerFactory(SpriteSet spriteSet) {
            this.sprites = spriteSet;
        }

        @Override
        public Particle createParticle(SimpleParticleType type, ClientLevel level,
                                       double x, double y, double z,
                                       double xd, double yd, double zd) {
            SimpleTrackParticle particle = new SimpleTrackParticle(level, x, y, z, sprites, 0.0F, 0.0F, 1.0F);
            return particle;
        }
    }

    @OnlyIn(Dist.CLIENT)
    public static class AnimalFactory implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprites;

        public AnimalFactory(SpriteSet spriteSet) {
            this.sprites = spriteSet;
        }

        @Override
        public Particle createParticle(SimpleParticleType type, ClientLevel level,
                                       double x, double y, double z,
                                       double xd, double yd, double zd) {
            SimpleTrackParticle particle = new SimpleTrackParticle(level, x, y, z, sprites, 0.0F, 1.0F, 0.0F);
            return particle;
        }
    }

    @OnlyIn(Dist.CLIENT)
    public static class MonsterFactory implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprites;

        public MonsterFactory(SpriteSet spriteSet) {
            this.sprites = spriteSet;
        }

        @Override
        public Particle createParticle(SimpleParticleType type, ClientLevel level,
                                       double x, double y, double z,
                                       double xd, double yd, double zd) {
            SimpleTrackParticle particle = new SimpleTrackParticle(level, x, y, z, sprites, 1.0F, 0.0F, 0.0F);
            return particle;
        }
    }
}