package net.gaalee;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.level.block.CreateFluidSourceEvent;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@EventBusSubscriber(modid = waterfinitetillx.MODID)
public class FluidEventHandler {

    // Cache for recently validated positions as part of a large body of water.
    // Key: BlockPos, Value: Expiration time (ms)
    private static final Map<BlockPos, Long> LARGE_BODY_CACHE = new ConcurrentHashMap<>();
    private static final long CACHE_DURATION_MS = 15000;
    private static int scansThisTick = 0;
    private static long lastTickTime = 0;
    private static final int MAX_SCANS_PER_TICK = 5; // Limits BFS scans per tick to prevent server lag

    @SubscribeEvent
    public static void onCreateFluidSource(CreateFluidSourceEvent event) {
        if (event.getFluidState().is(Fluids.WATER)) {
            // If requiredSources is 0, behave like vanilla (allow source creation)
            if (Config.requiredSources == 0) {
                return;
            }

            var level = event.getLevel();
            BlockPos pos = event.getPos();

            long currentTime = System.currentTimeMillis();
            
            // 0. Fast filter: if less than 2 adjacent sources, no need for BFS
            if (countAdjacentSources(level, pos) < 2) {
                event.setCanConvert(false);
                return;
            }
            
            // Manage scan counter per tick (approx 50ms)
            if (currentTime / 50 != lastTickTime / 50) {
                scansThisTick = 0;
                lastTickTime = currentTime;
            }

            // 1. Check cache first
            if (isPositionCached(pos, currentTime)) {
                return;
            }
            
            // 2. Limit scans per tick
            if (scansThisTick >= MAX_SCANS_PER_TICK) {
                event.setCanConvert(false);
                return;
            }

            scansThisTick++;
            if (!isLargeBodyOfWater(level, pos)) {
                event.setCanConvert(false);
            } else {
                // Cache the position and neighbors to avoid redundant scans
                for (int x = -1; x <= 1; x++) {
                    for (int z = -1; z <= 1; z++) {
                        LARGE_BODY_CACHE.put(pos.offset(x, 0, z), currentTime + CACHE_DURATION_MS);
                    }
                }
                
                if (LARGE_BODY_CACHE.size() > 2000) {
                    cleanCache(currentTime);
                }
            }
        }
    }

    private static boolean isPositionCached(BlockPos pos, long currentTime) {
        Long expiration = LARGE_BODY_CACHE.get(pos);
        if (expiration != null) {
            if (currentTime < expiration) {
                return true;
            }
            LARGE_BODY_CACHE.remove(pos);
        }
        return false;
    }

    private static void cleanCache(long currentTime) {
        LARGE_BODY_CACHE.entrySet().removeIf(entry -> currentTime >= entry.getValue());
    }

    private static boolean isLargeBodyOfWater(LevelAccessor level, BlockPos pos) {
        int requiredSources = Config.requiredSources;
        int maxScan = 1000;
        long maxTimeNs = 500000; // 0.5ms (500,000 ns)
        long startTimeNs = System.nanoTime();
        
        Set<BlockPos> visited = new HashSet<>();
        Queue<BlockPos> queue = new ArrayDeque<>();
        
        queue.add(pos);
        visited.add(pos);
        
        int checkedCount = 0;

        while (!queue.isEmpty() && checkedCount < maxScan) {
            // Check time every 32 blocks to minimize nanoTime calls
            if ((checkedCount & 31) == 0) {
                if (System.nanoTime() - startTimeNs > maxTimeNs) {
                    return false;
                }
            }

            BlockPos current = queue.poll();
            if (current == null) continue;
            checkedCount++;

            // Cardinal directions without array allocation
            checkNeighbor(level, current.north(), visited, queue);
            checkNeighbor(level, current.south(), visited, queue);
            checkNeighbor(level, current.east(), visited, queue);
            checkNeighbor(level, current.west(), visited, queue);
            checkNeighbor(level, current.above(), visited, queue);
            checkNeighbor(level, current.below(), visited, queue);

            // Return true if enough connected sources are found
            if (visited.size() >= requiredSources) {
                return true; 
            }
        }
        
        return false;
    }

    private static void checkNeighbor(LevelAccessor level, BlockPos neighbor, Set<BlockPos> visited, Queue<BlockPos> queue) {
        if (!visited.contains(neighbor)) {
            visited.add(neighbor);
            if (level.getFluidState(neighbor).isSourceOfType(Fluids.WATER)) {
                queue.add(neighbor);
            }
        }
    }

    private static int countAdjacentSources(LevelAccessor level, BlockPos pos) {
        int count = 0;
        // Check 6 directions quickly
        if (level.getFluidState(pos.north()).isSourceOfType(Fluids.WATER)) count++;
        if (level.getFluidState(pos.south()).isSourceOfType(Fluids.WATER)) count++;
        if (level.getFluidState(pos.east()).isSourceOfType(Fluids.WATER)) count++;
        if (level.getFluidState(pos.west()).isSourceOfType(Fluids.WATER)) count++;
        if (level.getFluidState(pos.above()).isSourceOfType(Fluids.WATER)) count++;
        if (level.getFluidState(pos.below()).isSourceOfType(Fluids.WATER)) count++;
        return count;
    }
}
