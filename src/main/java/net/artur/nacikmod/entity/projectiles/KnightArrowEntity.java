package net.artur.nacikmod.entity.projectiles;

import net.artur.nacikmod.entity.custom.*;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.level.Level;

public class KnightArrowEntity extends Arrow {

    public KnightArrowEntity(Level level, LivingEntity shooter) {
        super(level, shooter);
    }

    // ❌ вообще не считаем союзников целью
    @Override
    protected boolean canHitEntity(Entity entity) {
        if (isKnightAlly(entity)) {
            return false;
        }
        return super.canHitEntity(entity);
    }

    // 🛡️ если вдруг попали — ничего не делаем
    @Override
    protected void onHitEntity(EntityHitResult result) {
        if (isKnightAlly(result.getEntity())) {
            return;
        }
        super.onHitEntity(result);
    }

    private boolean isKnightAlly(Entity entity) {
        return entity instanceof KnightEntity
                || entity instanceof KnightArcherEntity
                || entity instanceof KnightLeaderEntity
                || entity instanceof KnightPaladinEntity
                || entity instanceof KnightCasterEntity
                || entity instanceof KnightBossEntity;
    }
}
