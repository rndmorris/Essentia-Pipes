package dev.rndmorris.pressurizedessentia.api;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.common.util.ForgeDirection;

import com.github.bsideup.jabel.Desugar;

@Desugar
public record WorldCoordinate(int dimensionId, int x, int y, int z) {

    public static WorldCoordinate[] adjacent(int dimensionId, int x, int y, int z) {
        final var result = new WorldCoordinate[ForgeDirection.VALID_DIRECTIONS.length];

        for (var index = 0; index < ForgeDirection.VALID_DIRECTIONS.length; ++index) {
            result[index] = shift(dimensionId, x, y, z, ForgeDirection.VALID_DIRECTIONS[index]);
        }

        return result;
    }

    public static WorldCoordinate shift(int dimensionId, int x, int y, int z, ForgeDirection direction) {
        return new WorldCoordinate(dimensionId, x + direction.offsetX, y + direction.offsetY, z + direction.offsetZ);
    }

    public WorldCoordinate[] adjacent() {
        return adjacent(dimensionId, x, y, z);
    }

    public WorldCoordinate shift(ForgeDirection direction) {
        return shift(dimensionId, x, y, z, direction);
    }

    public World getWorld() {
        if (DimensionManager.isDimensionRegistered(dimensionId)) {
            return DimensionManager.getWorld(dimensionId);
        }
        return null;
    }

    public TileEntity getTileEntity() {
        final var world = getWorld();
        if (world == null) {
            return null;
        }
        return world.getTileEntity(x, y, z);
    }

    public <T> T getTileEntity(Class<T> clazz) {
        final var entity = getTileEntity();
        if (entity != null && clazz.isAssignableFrom(entity.getClass())) {
            // noinspection unchecked
            return (T) entity;
        }
        return null;
    }
}
