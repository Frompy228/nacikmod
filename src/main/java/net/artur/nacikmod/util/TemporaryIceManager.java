package net.artur.nacikmod.util;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import java.util.*;

public class TemporaryIceManager {
    private static final Map<Level, List<IceBlockEntry>> ICE_BLOCKS = new HashMap<>();

    public static void addIceBlock(Level level, BlockPos pos, int lifetime) {
        ICE_BLOCKS.computeIfAbsent(level, l -> new ArrayList<>()).add(new IceBlockEntry(pos, lifetime));
    }

    public static void tick(Level level) {
        List<IceBlockEntry> list = ICE_BLOCKS.get(level);
        if (list == null) return;
        Iterator<IceBlockEntry> it = list.iterator();
        while (it.hasNext()) {
            IceBlockEntry entry = it.next();
            entry.ticks--;
            if (entry.ticks <= 0) {
                if (level.getBlockState(entry.pos).is(Blocks.ICE)) {
                    level.removeBlock(entry.pos, false);
                }
                it.remove();
            }
        }
        if (list.isEmpty()) {
            ICE_BLOCKS.remove(level);
        }
    }

    private static class IceBlockEntry {
        BlockPos pos;
        int ticks;
        IceBlockEntry(BlockPos pos, int ticks) {
            this.pos = pos;
            this.ticks = ticks;
        }
    }
} 