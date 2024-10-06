package dev.rndmorris.pressurizedessentia.api;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

import com.github.bsideup.jabel.Desugar;

public class PipeHelper {

    public static List<WorldCoordinate> connectedSegments(WorldCoordinate coordinate) {
        return connectedSegments(coordinate.getWorld(), coordinate.x(), coordinate.y(), coordinate.z());
    }

    /**
     * Get adjacent pipe segments that can connect to the pipe at x, y, z.
     *
     * @param world The world to check.
     * @return A list of connected segments (empty if none), or null if the block at x, y, z was not a pipe.
     */
    public static List<WorldCoordinate> connectedSegments(World world, int x, int y, int z) {
        final var pipe = getPipeSegment(world, x, y, z);
        if (pipe == null) {
            return Collections.emptyList();
        }
        final var result = new ArrayList<WorldCoordinate>();
        final var thisColor = pipe.getPipeColor(world, x, y, z);
        for (var dir : ForgeDirection.VALID_DIRECTIONS) {
            final int nX = x + dir.offsetX, nY = y + dir.offsetY, nZ = z + dir.offsetZ;
            final var neighborColor = getPipeColor(world, nX, nY, nZ);
            if (neighborColor != null && thisColor.willConnectTo(neighborColor)) {
                result.add(new WorldCoordinate(world.provider.dimensionId, nX, nY, nZ));
            }
        }
        return result;
    }

    public static IPipeSegment getPipeSegment(WorldCoordinate coordinate) {
        final var world = coordinate.getWorld();
        if (world == null) {
            return null;
        }
        return getPipeSegment(world, coordinate.x(), coordinate.y(), coordinate.z());
    }

    public static IPipeSegment getPipeSegment(World world, int x, int y, int z) {
        final var block = world.getBlock(x, y, z);
        if (block instanceof IPipeSegment blockSegment) {
            return blockSegment;
        }
        final var tileEntity = world.getTileEntity(x, y, z);
        return tileEntity instanceof IPipeSegment tileSegment ? tileSegment : null;
    }

    public static PipeColor getPipeColor(WorldCoordinate coordinate) {
        final var world = coordinate.getWorld();
        if (world == null) {
            return null;
        }
        return getPipeColor(world, coordinate.x(), coordinate.y(), coordinate.z());
    }

    public static PipeColor getPipeColor(World world, int x, int y, int z) {
        final var pipe = getPipeSegment(world, x, y, z);
        return pipe == null ? null : pipe.getPipeColor(world, x, y, z);
    }

    public static IIOPipeSegment getIOSegment(WorldCoordinate coordinate) {
        final var world = coordinate.getWorld();
        if (world == null) {
            return null;
        }
        return getIOSegment(coordinate.getWorld(), coordinate.x(), coordinate.y(), coordinate.z());
    }

    public static IIOPipeSegment getIOSegment(World world, int x, int y, int z) {
        final var block = world.getBlock(x, y, z);
        if (block instanceof IIOPipeSegment ioSegment) {
            return ioSegment;
        }
        final var tileEntity = world.getTileEntity(x, y, z);
        return tileEntity instanceof IIOPipeSegment tileIOSegment ? tileIOSegment : null;
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
        final var toUpdate = findIOSegmentsInNetwork(
            world,
            SearchType.DepthFirst,
            new WorldCoordinate(world.provider.dimensionId, x, y, z));
        updateIOSegments(world, toUpdate);
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
        final var toUpdate = findIOSegmentsInNetwork(
            world,
            SearchType.DepthFirst,
            WorldCoordinate.adjacent(world.provider.dimensionId, x, y, z));
        updateIOSegments(world, toUpdate);
    }

    private static void updateIOSegments(World world, Collection<IOSegmentResult> toUpdate) {
        for (var result : toUpdate) {
            final var ioSegment = getIOSegment(world, result.x, result.y, result.z);
            if (ioSegment == null) {
                continue;
            }
            ioSegment.rebuildIOConnections(world, result.x, result.y, result.z);
        }
    }

    public static List<IOSegmentResult> findIOSegmentsInNetwork(World world, SearchType searchType,
        WorldCoordinate... initialPositions) {

        final var queue = Arrays.stream(initialPositions)
            .map(p -> new IOSegmentResult(p.x(), p.y(), p.z(), 0))
            .collect(Collectors.toCollection(ArrayDeque::new));
        final var visited = new HashSet<WorldCoordinate>();

        final var foundAndDistance = new HashMap<WorldCoordinate, Integer>();

        while (!queue.isEmpty()) {
            final var at = switch (searchType) {
                case BreadthFirst -> queue.pollFirst();
                case DepthFirst -> queue.pollLast();
            };

            final int x = at.x(), y = at.y(), z = at.z(), distance = at.distance();
            final var pos = new WorldCoordinate(world.provider.dimensionId, x, y, z);
            visited.add(pos);

            final var ioSegment = getIOSegment(world, x, y, z);
            if (ioSegment != null && (!foundAndDistance.containsKey(pos) || foundAndDistance.get(pos) > distance)) {
                foundAndDistance.put(pos, distance);
            }

            for (var adj : connectedSegments(world, x, y, z)) {
                if (!visited.contains(adj)) {
                    queue.push(new IOSegmentResult(adj.x(), adj.y(), adj.z(), distance + 1));
                }
            }

        }

        return foundAndDistance.entrySet()
            .stream()
            .map(kv -> {
                final var coord = kv.getKey();
                final int distance = kv.getValue();
                return new IOSegmentResult(coord.x(), coord.y(), coord.z(), distance);
            })
            .collect(Collectors.toList());
    }

    public enum SearchType {
        BreadthFirst,
        DepthFirst,
    }

    @Desugar
    public record IOSegmentResult(int x, int y, int z, int distance) implements Comparable<IOSegmentResult> {

        @Override
        public String toString() {
            return "ConnectorResult{" + "x=" + x + ", y=" + y + ", z=" + z + ", distance=" + distance + '}';
        }

        @Override
        public int compareTo(IOSegmentResult o) {
            return Integer.compare(distance, o.distance);
        }
    }
}
