package dev.rndmorris.essentiapipes.api;

import static dev.rndmorris.essentiapipes.EssentiaPipes.LOG;

import java.lang.ref.WeakReference;
import java.util.Comparator;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.block.Block;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.common.util.ForgeDirection;

public class WorldCoordinate implements Comparable<WorldCoordinate>, Comparator<WorldCoordinate> {

    public static WorldCoordinate[] adjacent(int dimensionId, int x, int y, int z) {
        final var result = new WorldCoordinate[ForgeDirection.VALID_DIRECTIONS.length];

        for (var index = 0; index < ForgeDirection.VALID_DIRECTIONS.length; ++index) {
            result[index] = shift(dimensionId, x, y, z, ForgeDirection.VALID_DIRECTIONS[index]);
        }

        return result;
    }

    public static WorldCoordinate fromTileEntity(@Nullable Object object) {
        if (!(object instanceof TileEntity tileEntity)) {
            return null;
        }
        return new WorldCoordinate(
            tileEntity.getWorldObj().provider.dimensionId,
            tileEntity.xCoord,
            tileEntity.yCoord,
            tileEntity.zCoord);
    }

    private @Nullable WeakReference<World> cachedWorld;
    private final int dimensionId, x, y, z;

    public WorldCoordinate(int dimensionId, int x, int y, int z) {
        this.dimensionId = dimensionId;
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public WorldCoordinate(@Nonnull World world, int x, int y, int z) {
        this(world.provider.dimensionId, x, y, z);
        cachedWorld = new WeakReference<>(world);
    }

    public int dimensionId() {
        return dimensionId;
    }

    public int x() {
        return x;
    }

    public int y() {
        return y;
    }

    public int z() {
        return z;
    }

    public Block getBlock() {
        final var world = getWorld();
        if (world == null) {
            return null;
        }
        return world.getBlock(x, y, z);
    }

    @SuppressWarnings("unchecked")
    public <T> T getBlock(Class<T> clazz) {
        final var block = getBlock();
        if (block != null && clazz.isAssignableFrom(block.getClass())) {
            return (T) block;
        }
        return null;
    }

    public int getBlockMetadata() {
        final var world = getWorld();
        if (world == null) {
            return -1;
        }
        return world.getBlockMetadata(x, y, z);
    }

    public TileEntity getTileEntity() {
        final var world = getWorld();
        if (world == null) {
            return null;
        }
        return world.getTileEntity(x, y, z);
    }

    @SuppressWarnings("unchecked")
    public <T> T getTileEntity(Class<T> clazz) {
        final var entity = getTileEntity();
        if (entity != null && clazz.isAssignableFrom(entity.getClass())) {
            return (T) entity;
        }
        return null;
    }

    public World getWorld() {
        World world;
        if (cachedWorld == null || (world = cachedWorld.get()) == null) {
            if (!DimensionManager.isDimensionRegistered(dimensionId)) {
                LOG.error("A world for dimensionId {} could not be found.", dimensionId);
                return null;
            }
            world = DimensionManager.getWorld(dimensionId);
            cachedWorld = new WeakReference<>(world);
        }
        return world;
    }

    public boolean setBlock(Block block) {
        final var world = getWorld();
        return world != null && world.setBlock(x, y, z, block);
    }

    public boolean setBlock(Block block, int metadata, int flag) {
        final var world = getWorld();
        return world != null && world.setBlock(x, y, z, block, metadata, flag);
    }

    /**
     * Set a block's metadata
     * 
     * @param metadata The block's metadata
     * @param flag     A bitwise set of option flags. Flag 1 will cause a block update. Flag 2 will send the change to
     *                 clients (you almost always want this). Flag 4 prevents the block from being re-rendered, if this
     *                 is a client world.
     */
    public boolean setBlockMetadataWithNotify(int metadata, int flag) {
        final var world = getWorld();
        return world != null && world.setBlockMetadataWithNotify(x, y, z, metadata, flag);
    }

    public static WorldCoordinate shift(int dimensionId, int x, int y, int z, ForgeDirection direction) {
        return shift(dimensionId, x, y, z, direction, 1);
    }

    public static WorldCoordinate shift(int dimensionId, int x, int y, int z, ForgeDirection direction, int magnitude) {
        return new WorldCoordinate(
            dimensionId,
            x + (direction.offsetX * magnitude),
            y + (direction.offsetY * magnitude),
            z + (direction.offsetZ * magnitude));
    }

    public WorldCoordinate shift(ForgeDirection direction) {
        return shift(direction, 1);
    }

    public WorldCoordinate shift(ForgeDirection direction, int magnitude) {
        return shift(dimensionId, x, y, z, direction, magnitude);
    }

    // Comparable

    @Override
    public int compareTo(@Nonnull WorldCoordinate that) {
        return compare(this, that);
    }

    // Comparator

    public int compare(@Nonnull WorldCoordinate thiz, @Nonnull WorldCoordinate that) {
        var compare = Integer.compare(thiz.x, that.x);
        if (compare != 0) {
            return compare;
        }

        compare = Integer.compare(thiz.y, that.y);
        if (compare != 0) {
            return compare;
        }

        compare = Integer.compare(thiz.z, that.z);
        if (compare != 0) {
            return compare;
        }

        return Integer.compare(thiz.dimensionId, that.dimensionId);
    }
}
