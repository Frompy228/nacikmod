package net.artur.nacikmod.item.ability;

import net.minecraft.core.BlockPos;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

import java.util.ArrayList;
import java.util.List;

public class ShinraTenseiExplosion extends Explosion {
    private static final float FIXED_DAMAGE = 1000.0f;
    private static final int MAX_BLOCKS_PER_TICK = 100;
    private static final int CYLINDER_HEIGHT = 20; // Высота в одну сторону
    private static final int CYLINDER_RADIUS = 50;
    
    private final double x, y, z;
    private final Level level;
    private final Entity source;

    public ShinraTenseiExplosion(Level level, Entity source, double x, double y, double z, float radius) {
        super(level, source, null, null, x, y, z, radius, false, Explosion.BlockInteraction.DESTROY);
        this.level = level;
        this.source = source;
        this.x = x;
        this.y = y;
        this.z = z;
    }

    @Override
    public void explode() {
        // Получаем все блоки в цилиндре
        List<BlockPos> blocks = getBlocksInCylinder();
        
        // Разбиваем обработку на части для оптимизации
        for (int i = 0; i < blocks.size(); i += MAX_BLOCKS_PER_TICK) {
            int end = Math.min(i + MAX_BLOCKS_PER_TICK, blocks.size());
            List<BlockPos> batch = blocks.subList(i, end);
            
            // Обрабатываем текущую партию блоков
            for (BlockPos pos : batch) {
                BlockState state = level.getBlockState(pos);
                if (state.getExplosionResistance(level, pos, this) < 3600000.0F) {
                    level.removeBlock(pos, false);
                }
            }
        }

        // Наносим урон сущностям в цилиндре
        AABB explosionBox = new AABB(
            x - CYLINDER_RADIUS, y - CYLINDER_HEIGHT, z - CYLINDER_RADIUS,
            x + CYLINDER_RADIUS, y + CYLINDER_HEIGHT, z + CYLINDER_RADIUS
        );
        
        List<Entity> entities = level.getEntities(null, explosionBox);
        for (Entity entity : entities) {
            if (entity instanceof LivingEntity) {
                // Проверяем, находится ли сущность в цилиндре
                double dx = entity.getX() - x;
                double dz = entity.getZ() - z;
                double horizontalDistance = Math.sqrt(dx * dx + dz * dz);
                
                if (horizontalDistance <= CYLINDER_RADIUS && 
                    entity.getY() >= y - CYLINDER_HEIGHT && 
                    entity.getY() <= y + CYLINDER_HEIGHT) {
                    
                    // Наносим фиксированный урон
                    entity.hurt(level.damageSources().explosion(source, source), FIXED_DAMAGE);
                }
            }
        }
    }

    private List<BlockPos> getBlocksInCylinder() {
        List<BlockPos> blocks = new ArrayList<>();
        int centerX = (int) this.x;
        int centerY = (int) this.y;
        int centerZ = (int) this.z;
        
        // Перебираем все блоки в цилиндре
        for (int x = -CYLINDER_RADIUS; x <= CYLINDER_RADIUS; x++) {
            for (int z = -CYLINDER_RADIUS; z <= CYLINDER_RADIUS; z++) {
                // Проверяем, находится ли блок в радиусе цилиндра
                double horizontalDistance = Math.sqrt(x * x + z * z);
                if (horizontalDistance <= CYLINDER_RADIUS) {
                    // Добавляем все блоки по высоте цилиндра (вверх и вниз)
                    for (int y = -CYLINDER_HEIGHT; y <= CYLINDER_HEIGHT; y++) {
                        BlockPos pos = new BlockPos(
                            centerX + x,
                            centerY + y,
                            centerZ + z
                        );
                        blocks.add(pos);
                    }
                }
            }
        }
        
        return blocks;
    }
}
