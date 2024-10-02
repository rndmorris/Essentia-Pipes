package dev.rndmorris.pressurizedessentia.api;

import net.minecraft.world.World;

public interface IPipeSegment {

    PipeColor getPipeColor(World world, int x, int y, int z);

}
