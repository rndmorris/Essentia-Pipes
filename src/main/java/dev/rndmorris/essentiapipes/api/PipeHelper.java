package dev.rndmorris.essentiapipes.api;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;

import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

import com.github.bsideup.jabel.Desugar;

import thaumcraft.api.aspects.IEssentiaTransport;

/**
 * Helper methods for interacting with the pipe network.
 */
public class PipeHelper {

    public static boolean canConnect(IBlockAccess world, int x, int y, int z, ForgeDirection direction) {
        final var hereBlock = world.getBlock(x, y, z);
        if (!(hereBlock instanceof IPipeSegment herePipe)) {
            return false;
        }
        if (herePipe.canConnectTo(world, x, y, z, direction)) {
            return true;
        }
        final var dX = x + direction.offsetX;
        final var dY = y + direction.offsetY;
        final var dZ = z + direction.offsetZ;
        final var thereBlock = world.getBlock(dX, dY, dZ);
        if (!(thereBlock instanceof IPipeSegment therePipe)) {
            return false;
        }
        return therePipe.canConnectTo(world, dX, dY, dZ, direction.getOpposite());
    }

    /**
     * Whether the pipe segment at the given position should visually connect in the given direction.
     *
     * @param world     The world in which the pipe segment to check exists.
     * @param x         The x coordinate of the pipe segment to check.
     * @param y         The y coordinate of the pipe segment to check.
     * @param z         The z coordinate of the pipe segment to check.
     * @param direction The direction to check.
     * @return True if there is a pipe segment here, a pipe segment in the given direction it can connect to, or an
     *         IEssentiaTransport tile in the given direction.
     */
    public static boolean canConnectVisually(IBlockAccess world, int x, int y, int z, ForgeDirection direction) {
        final var hereBlock = world.getBlock(x, y, z);
        if (!(hereBlock instanceof IPipeSegment herePipe)) {
            return false;
        }
        if (herePipe.canConnectTo(world, x, y, z, direction)) {
            return true;
        }
        final var dX = x + direction.offsetX;
        final var dY = y + direction.offsetY;
        final var dZ = z + direction.offsetZ;
        final var thereBlock = world.getBlock(dX, dY, dZ);
        if (!(thereBlock instanceof IPipeSegment therePipe)) {
            return false;
        }
        if (therePipe.canConnectTo(world, dX, dY, dZ, direction.getOpposite())) {
            return true;
        }
        final var thereTile = world.getTileEntity(dX, dY, dZ);
        if (thereTile instanceof IEssentiaTransport transport) {
            return transport.isConnectable(direction.getOpposite());
        }
        return false;
    }

    /**
     * Notify the pipe network that a segment has been added or changed.
     *
     * @param world The world of the segment that was added or changed.
     * @param x     The x of the segment that was added or changed.
     * @param y     The y of the segment that was added or changed.
     * @param z     The z of the segment that was added or changed.
     */
    public static void notifySegmentAddedOrChanged(World world, int x, int y, int z) {
        final var toUpdate = findIOPipeSegments(
            SearchType.DepthFirst,
            new WorldCoordinate(world.provider.dimensionId, x, y, z));
        updateIOSegments(toUpdate);
    }

    /**
     * Notify the pipe network that a segment has been removed.
     *
     * @param world The world of the segment that was removed.
     * @param x     The x of the segment that was removed.
     * @param y     The y of the segment that was removed.
     * @param z     The z of the segment that was removed.
     */
    public static void notifySegmentRemoved(World world, int x, int y, int z) {
        final var found = findIOPipeSegments(
            SearchType.DepthFirst,
            WorldCoordinate.adjacent(world.provider.dimensionId, x, y, z));
        updateIOSegments(found);
    }

    private static void updateIOSegments(Collection<ConnectionInfo> toUpdate) {
        for (var result : toUpdate) {
            final var ioSegment = result.getIOSegment();
            if (ioSegment == null) {
                continue;
            }
            ioSegment.rebuildIOConnections();
        }
    }

    /**
     * From the provided initial position(s), walk along connected pipe segments and return any IO segments found.
     *
     * @param searchType       Which search algorithm to use
     * @param initialPositions The initial value(s) used to populate the navigation stack (depth-first) or queue
     *                         (breadth-first).
     * @return All IO segments found while walking the pipe network.
     */
    public static Collection<ConnectionInfo> findIOPipeSegments(SearchType searchType,
        WorldCoordinate... initialPositions) {
        final var resultMap = new HashMap<WorldCoordinate, Integer>();

        for (var p : initialPositions) {
            final var world = p.getWorld();
            if (world == null) {
                continue;
            }
            final var queue = new ArrayDeque<NavigationEntry>();
            final var visited = new HashSet<WorldCoordinate>();
            queue.add(new NavigationEntry(p, 0));

            while (!queue.isEmpty()) {
                final var current = switch (searchType) {
                    case BreadthFirst -> queue.pollFirst();
                    case DepthFirst -> queue.pollLast();
                };

                final var here = current.coordinate;
                visited.add(here);

                final var distance = current.distance;

                final var hereWorld = here.getWorld();
                if (hereWorld == null) {
                    continue;
                }

                final var ioSegment = here.getTileEntity(IIOPipeSegment.class);
                if (ioSegment != null && (!resultMap.containsKey(here) || resultMap.get(here) > distance)) {
                    resultMap.put(here, distance);
                }

                for (var dir : ForgeDirection.VALID_DIRECTIONS) {
                    final var there = here.shift(dir);
                    if (!visited.contains(here.shift(dir))
                        && canConnect(hereWorld, here.x(), here.y(), here.z(), dir)) {
                        queue.push(new NavigationEntry(there, distance + 1));
                    }
                }
            }
        }

        final var result = new ArrayList<ConnectionInfo>(resultMap.size());
        for (var entry : resultMap.entrySet()) {
            result.add(new ConnectionInfo(entry.getKey(), entry.getValue()));
        }
        return result;
    }

    /**
     * Determines the behavior of findIOPipeSegments
     */
    public enum SearchType {
        /**
         * Queue-based. Uses more memory, but guarenteed to find the shortest distances between the inital positions and
         * any IO pipe segments.
         */
        BreadthFirst,
        /**
         * Stack-based. Not guarenteed to find the shortest distances, but uses less memory.
         */
        DepthFirst,
    }

    @Desugar
    private record NavigationEntry(WorldCoordinate coordinate, int distance) {}
}
