package dev.rndmorris.pressurizedessentia.api;

import net.minecraft.world.World;

import thaumcraft.api.aspects.AspectList;
import thaumcraft.api.aspects.IEssentiaTransport;

public interface IIOPipeSegment extends IPipeSegment, IEssentiaTransport {

    AspectList wantsEssentia(World world, int x, int y, int z);

    void rebuildIOConnections(World world, int x, int y, int z);
}
