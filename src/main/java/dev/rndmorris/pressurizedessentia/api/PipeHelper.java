package dev.rndmorris.pressurizedessentia.api;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;

import javax.annotation.Nonnull;

import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

import com.github.bsideup.jabel.Desugar;

import thaumcraft.api.aspects.IEssentiaTransport;

/**
 * Helper methods for interacting with the pipe network.
 */
public class PipeHelper {

    /**
     * Whether the pipe segment at the given position should connect in the given direction.
     *
     * @param here      The position of the pipe segment to check.
     * @param direction The direction to check.
     * @return True if there is a pipe segment both here and in the given direction, and they can connect, or false
     *         otherwise.
     */
    public static boolean canConnect(@Nonnull WorldCoordinate here, @Nonnull ForgeDirection direction) {
        final var herePipe = here.getBlock(IPipeSegment.class);
        if (herePipe == null) {
            return false;
        }
        final var there = here.shift(direction);
        final var therePipe = there.getBlock(IPipeSegment.class);
        if (therePipe == null) {
            return false;
        }
        return herePipe.canConnectTo(here, direction) || therePipe.canConnectTo(there, direction.getOpposite());
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
        final int dX = x + direction.offsetX, dY = y + direction.offsetY, dZ = z + direction.offsetZ;
        final var here = world.getBlock(x, y, z);
        if (!(here instanceof IPipeSegment herePipe)) {
            return false;
        }
        final var there = world.getBlock(dX, dY, dZ);
        if (there == null) {
            return false;
        }
        final var neighborSide = direction.getOpposite();
        if (there instanceof IPipeSegment pipe) {
            return herePipe.canConnectTo(world, x, y, z, direction)
                || pipe.canConnectTo(world, dX, dY, dZ, neighborSide);
        }
        final var thereTile = world.getTileEntity(dX, dY, dZ);
        if (thereTile instanceof IEssentiaTransport transport) {
            return transport.isConnectable(neighborSide);
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
                final var distance = current.distance;

                visited.add(here);

                final var ioSegment = here.getTileEntity(IIOPipeSegment.class);
                if (ioSegment != null && (!resultMap.containsKey(here) || resultMap.get(here) > distance)) {
                    resultMap.put(here, distance);
                }

                for (var dir : ForgeDirection.VALID_DIRECTIONS) {
                    final var there = here.shift(dir);
                    if (!visited.contains(here.shift(dir)) && canConnect(here, dir)) {
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
