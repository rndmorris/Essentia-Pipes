package dev.rndmorris.pressurizedessentia;

import net.minecraftforge.common.util.ForgeDirection;

import com.github.bsideup.jabel.Desugar;

@Desugar
public record Vec(int x, int y, int z) {

    public Vec neighbor(ForgeDirection direction) {
        return new Vec(x + direction.offsetX, y + direction.offsetY, z + direction.offsetZ);
    }
}
