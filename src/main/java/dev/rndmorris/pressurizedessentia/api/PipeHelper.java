package dev.rndmorris.pressurizedessentia.api;

import java.util.ArrayDeque;
import java.util.ArrayList;
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
            if (thisColor.connectsTo(neighborColor)) {
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

    public static IPipeConnector getPipeConnector(World world, int x, int y, int z) {
        final var block = world.getBlock(x, y, z);
        if (block instanceof IPipeConnector blockConnector) {
            return blockConnector;
        }
        final var tileEntity = world.getTileEntity(x, y, z);
        return tileEntity instanceof IPipeConnector tileConnector ? tileConnector : null;
    }

    public static List<ConnectorResult> getConnectorsInGraph(World world, int x, int y, int z) {
        final var queue = new ArrayDeque<Vec>();
        final var visited = new HashSet<Vec>();

        final var results = new ArrayList<ConnectorResult>();

        var depth = 0;

        queue.add(new Vec(x, y, z));

        while (!queue.isEmpty()) {
            final var popped = queue.pop();
            final int pX = popped.x(), pY = popped.y(), pZ = popped.z();

            final var connector = getPipeConnector(world, pX, pY, pZ);
            if (connector != null) {
                results.add(new ConnectorResult(popped.x(), popped.y(), popped.z(), depth));
            }

            visited.add(popped);

            for (var adjacent : connectedSegments(world, pX, pY, pZ)) {
                if (!visited.contains(adjacent)) {
                    queue.push(adjacent);
                }
            }
            depth += 1;
        }

        return results;
    }

    @Desugar
    public record ConnectorResult(int x, int y, int z, int distance) {};
}