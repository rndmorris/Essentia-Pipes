package dev.rndmorris.essentiapipes.api;

import java.util.Comparator;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.block.Block;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.common.util.ForgeDirection;

import com.github.bsideup.jabel.Desugar;

@Desugar
public record WorldCoordinate(int dimensionId, int x, int y, int z)
    implements Comparable<WorldCoordinate>, Comparator<WorldCoordinate> {

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
        if (DimensionManager.isDimensionRegistered(dimensionId)) {
            return DimensionManager.getWorld(dimensionId);
        }
        return null;
    }

    public static WorldCoordinate shift(int dimensionId, int x, int y, int z, ForgeDirection direction) {
        return new WorldCoordinate(dimensionId, x + direction.offsetX, y + direction.offsetY, z + direction.offsetZ);
    }

    public WorldCoordinate shift(ForgeDirection direction) {
        return shift(dimensionId, x, y, z, direction);
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
