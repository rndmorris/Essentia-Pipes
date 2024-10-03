package dev.rndmorris.pressurizedessentia.api;

import net.minecraftforge.common.util.ForgeDirection;

import com.github.bsideup.jabel.Desugar;

@SuppressWarnings("unused")
@Desugar
public record Position(int x, int y, int z) {

    public static Position[] adjacent(int x, int y, int z) {
        final var result = new Position[ForgeDirection.VALID_DIRECTIONS.length];

        for (var index = 0; index < ForgeDirection.VALID_DIRECTIONS.length; ++index) {
            result[index] = shift(x, y, z, ForgeDirection.VALID_DIRECTIONS[index]);
        }

        return result;
    }

    public static Position shift(int x, int y, int z, ForgeDirection direction) {
        return new Position(x + direction.offsetX, y + direction.offsetY, z + direction.offsetZ);
    }

    public Position[] adjacent() {
        return adjacent(x, y, z);
    }

    public Position shift(ForgeDirection direction) {
        return shift(x, y, z, direction);
    }
}
