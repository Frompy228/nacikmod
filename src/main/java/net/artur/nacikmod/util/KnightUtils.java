package net.artur.nacikmod.util;

import net.artur.nacikmod.entity.custom.*;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;

/**
 * Утилиты для рыцарей: проверка «свой/чужой», разрешение источника урона.
 * Рыцари не должны наносить урон друг другу спеллами и не агряться друг на друга.
 */
public final class KnightUtils {

    private KnightUtils() {}

    public static boolean isKnight(LivingEntity entity) {
        if (entity == null) return false;
        return entity instanceof KnightEntity
                || entity instanceof KnightArcherEntity
                || entity instanceof KnightCasterEntity
                || entity instanceof KnightPaladinEntity
                || entity instanceof KnightBossEntity
                || entity instanceof KnightLeaderEntity;
    }

    /**
     * Возвращает «настоящего» атакующего: владельца проектайла или саму сущность.
     * Нужно для корректной проверки «не агриться на рыцарей» при уроне от спеллов.
     */
    public static LivingEntity resolveAttacker(DamageSource source) {
        Entity direct = source.getDirectEntity();
        Entity entity = source.getEntity();

        if (direct instanceof Projectile projectile && projectile.getOwner() instanceof LivingEntity owner) {
            return owner;
        }
        if (entity instanceof LivingEntity living) {
            return living;
        }
        return null;
    }
}
