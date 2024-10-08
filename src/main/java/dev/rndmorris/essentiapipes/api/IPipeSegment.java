package dev.rndmorris.essentiapipes.api;

import net.minecraft.world.IBlockAccess;
import net.minecraftforge.common.util.ForgeDirection;

/**
 * Implement on a block that should participate in an essentia pipe network.
 */
public interface IPipeSegment {

    /**
     * Check if the pipe segment is willing to connect to the block in the stated direciton.
     *
     * @param position The position of the block to check.
     * @param face     The direction to check against.
     * @return True if the pipe segment can connect in that direction, or false if not.
     */
    boolean canConnectTo(WorldCoordinate position, ForgeDirection face);

    /**
     * Check if the pipe segment is willing to connect to the block in the stated direciton.
     *
     * @param world The world in which the pipe segment to check exists.
     * @param x     The x coordinate of the pipe segment to check.
     * @param y     The y coordinate of the pipe segment to check.
     * @param z     The z coordinate of the pipe segment to check.
     * @param face  The direction to check against.
     * @return True if the pipe segment can connect in that direction, or false if not.
     */
    boolean canConnectTo(IBlockAccess world, int x, int y, int z, ForgeDirection face);

    /**
     * Get the color of the pipe segment at this world and coordinate.
     *
     * @param position The coordinate of the pipe segment to check.
     * @return The color of the pipe, or null if there is no pipe.
     */
    PipeColor getPipeColor(WorldCoordinate position);

    /**
     * Get the color of the pipe segment at this world and coordinate.
     *
     * @param world The world in which the pipe segment to check exists.
     * @param x     The x coordinate of the pipe segment to check.
     * @param y     The y coordinate of the pipe segment to check.
     * @param z     The z coordinate of the pipe segment to check.
     * @return The color of the pipe, or null if there is no pipe.
     */
    PipeColor getPipeColor(IBlockAccess world, int x, int y, int z);

}
