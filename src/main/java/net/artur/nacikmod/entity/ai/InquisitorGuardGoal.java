package net.artur.nacikmod.entity.ai;

import net.artur.nacikmod.entity.custom.InquisitorEntity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.target.TargetGoal;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.WaterAnimal;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

import java.util.EnumSet;
import java.util.List;
import java.util.UUID;

public class InquisitorGuardGoal extends TargetGoal {
    private static final double SEARCH_RADIUS = 18.0D;

    private final Mob mob;
    private final InquisitorEntity owner;
    private final TargetingConditions targetingConditions;

    public InquisitorGuardGoal(Mob mob, InquisitorEntity owner) {
        super(mob, false);
        this.mob = mob;
        this.owner = owner;
        this.targetingConditions = TargetingConditions.forCombat()
                .range(SEARCH_RADIUS)
                .ignoreLineOfSight();
        this.setFlags(EnumSet.of(Goal.Flag.TARGET));
    }

    @Override
    public boolean canUse() {
        if (owner == null || !owner.isAlive()) {
            return false;
        }

        LivingEntity currentTarget = mob.getTarget();
        if (currentTarget != null && !isValidTarget(currentTarget)) {
            mob.setTarget(null);
        }

        LivingEntity ownerTarget = owner.getTarget();
        if (ownerTarget != null && isValidTarget(ownerTarget)) {
            mob.setTarget(ownerTarget);
            return true;
        }

        LivingEntity nearby = findTargetNearOwner();
        if (nearby != null) {
            mob.setTarget(nearby);
            return true;
        }

        return false;
    }

    @Override
    public boolean canContinueToUse() {
        if (owner == null || !owner.isAlive()) {
            return false;
        }

        LivingEntity target = mob.getTarget();
        if (target == null || !target.isAlive()) {
            return false;
        }

        if (!isValidTarget(target)) {
            mob.setTarget(null);
            return false;
        }

        return targetingConditions.test(mob, target);
    }

    private LivingEntity findTargetNearOwner() {
        Level level = owner.level();
        AABB area = owner.getBoundingBox().inflate(SEARCH_RADIUS);
        List<LivingEntity> entities = level.getEntitiesOfClass(LivingEntity.class, area, this::isValidTarget);
        double closest = Double.MAX_VALUE;
        LivingEntity closestEntity = null;

        for (LivingEntity entity : entities) {
            double distance = owner.distanceToSqr(entity);
            if (distance < closest) {
                closest = distance;
                closestEntity = entity;
            }
        }

        return closestEntity;
    }

    private boolean isValidTarget(LivingEntity entity) {
        if (entity == null || entity == owner || entity == mob) {
            return false;
        }

        if (entity instanceof Animal || entity instanceof WaterAnimal) {
            return false;
        }

        if (entity instanceof InquisitorEntity other && other.getUUID().equals(owner.getUUID())) {
            return false;
        }

        UUID ownerUUID = owner.getUUID();
        if (sharesInquisitorOwner(entity, ownerUUID)) {
            return false;
        }

        return true;
    }

    private boolean sharesInquisitorOwner(LivingEntity entity, UUID ownerUUID) {
        if (entity.getPersistentData().contains("inquisitor_owner")) {
            try {
                UUID persistentOwner = entity.getPersistentData().getUUID("inquisitor_owner");
                return persistentOwner != null && persistentOwner.equals(ownerUUID);
            } catch (Exception ignored) {
            }
        }
        return false;
    }
}



