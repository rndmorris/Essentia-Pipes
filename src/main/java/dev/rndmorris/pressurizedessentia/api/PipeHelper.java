package dev.rndmorris.pressurizedessentia.api;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

import com.github.bsideup.jabel.Desugar;

import dev.rndmorris.pressurizedessentia.Vec;

public class PipeHelper {

    /**
     * Get adjacent pipe segments that can connect to the pipe at x, y, z.
     *
     * @param world The world to check.
     * @return A list of connected segments (empty if none), or null if the block at x, y, z was not a pipe.
     */
    public static List<Vec> connectedSegments(World world, int x, int y, int z) {
        final var pipe = getPipeSegment(world, x, y, z);
        if (pipe == null) {
            return Collections.emptyList();
        }
        final var result = new ArrayList<Vec>();
        final var thisColor = pipe.getPipeColor(world, x, y, z);
        for (var dir : ForgeDirection.VALID_DIRECTIONS) {
            final int nX = x + dir.offsetX, nY = y + dir.offsetY, nZ = z + dir.offsetZ;
            final var neighborColor = getPipeColor(world, nX, nY, nZ);
            if (neighborColor != null && thisColor.willConnectTo(neighborColor)) {
                result.add(new Vec(nX, nY, nZ));
            }
        }
        return result;
    }

    public static IPipeSegment getPipeSegment(World world, int x, int y, int z) {
        final var block = world.getBlock(x, y, z);
        if (block instanceof IPipeSegment blockSegment) {
            return blockSegment;
        }
        final var tileEntity = world.getTileEntity(x, y, z);
        return tileEntity instanceof IPipeSegment tileSegment ? tileSegment : null;
    }

    public static PipeColor getPipeColor(World world, int x, int y, int z) {
        final var pipe = getPipeSegment(world, x, y, z);
        return pipe == null ? null : pipe.getPipeColor(world, x, y, z);
    }

    public static IIOPipeSegment getIOSegment(World world, int x, int y, int z) {
        final var block = world.getBlock(x, y, z);
        if (block instanceof IIOPipeSegment ioSegment) {
            return ioSegment;
        }
        final var tileEntity = world.getTileEntity(x, y, z);
        return tileEntity instanceof IIOPipeSegment tileIOSegment ? tileIOSegment : null;
    }

    public static List<IoSegmentResult> findAllIOSegments(World world, int x, int y, int z) {
        return findAllIOSegments(world, Collections.singletonList(new Vec(x, y, z)), 0);
    }

    public static List<IoSegmentResult> findAllIOSegments(World world, Collection<Vec> seedCoordinates,
        int startDistance) {
        final var queue = new ArrayDeque<>(seedCoordinates);
        final var visited = new HashSet<Vec>();

        final var results = new ArrayList<IoSegmentResult>();

        var distance = startDistance;

        while (!queue.isEmpty()) {
            final var popped = queue.pop();
            final int pX = popped.x(), pY = popped.y(), pZ = popped.z();

            final var connector = getIOSegment(world, pX, pY, pZ);
            if (connector != null) {
                results.add(new IoSegmentResult(popped.x(), popped.y(), popped.z(), distance));
            }

            visited.add(popped);

            for (var adjacent : connectedSegments(world, pX, pY, pZ)) {
                if (!visited.contains(adjacent)) {
                    queue.push(adjacent);
                }
            }
            distance += 1;
        }

        return results;
    }

    public static void rebuildPipeNetwork(World world, int x, int y, int z) {
        final var seedCoordinates = new ArrayList<Vec>();

        final var originSegment = getPipeSegment(world, x, y, z);
        final var startDistance = originSegment != null ? 0 : 1;
        if (originSegment == null) {
            for (var dir : ForgeDirection.VALID_DIRECTIONS) {
                final int dX = x + dir.offsetX, dY = y + dir.offsetY, dZ = z + dir.offsetZ;
                final var adjacentSegment = getPipeSegment(world, dX, dY, dZ);
                if (adjacentSegment != null) {
                    seedCoordinates.add(new Vec(dX, dY, dZ));
                }
            }
        } else {
            seedCoordinates.add(new Vec(x, y, z));
        }

        final var addresses = findAllIOSegments(world, seedCoordinates, startDistance);
        for (var address : addresses) {
            final var ioSegment = getIOSegment(world, address.x, address.y, address.z);
            if (ioSegment == null) {
                continue;
            }
            ioSegment.rebuildIOConnections(world, address.x, address.y, address.z);
        }
    }

    @Desugar
    public record IoSegmentResult(int x, int y, int z, int distance) {

        @Override
        public String toString() {
            return "ConnectorResult{" + "x=" + x + ", y=" + y + ", z=" + z + ", distance=" + distance + '}';
        }
    };
}
