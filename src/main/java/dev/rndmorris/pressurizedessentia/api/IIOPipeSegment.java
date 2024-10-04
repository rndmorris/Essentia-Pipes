package dev.rndmorris.pressurizedessentia.api;

import net.minecraft.world.World;

import thaumcraft.api.aspects.IEssentiaTransport;

public interface IIOPipeSegment extends IPipeSegment, IEssentiaTransport {

    Position getPosition();

    void rebuildIOConnections(World world, int x, int y, int z);
}
