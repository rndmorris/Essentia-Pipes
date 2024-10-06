package dev.rndmorris.pressurizedessentia.api;

import net.minecraft.world.World;

import thaumcraft.api.aspects.IEssentiaTransport;

public interface IIOPipeSegment extends IPipeSegment, IEssentiaTransport {

    boolean acceptRequestFulfillment(EssentiaRequest fulfilledRequest, int amount);

    boolean evaluateEssentiaRequest(EssentiaRequest incomingRequest, int distance);

    WorldCoordinate getCoordinate();

    void rebuildIOConnections(World world, int x, int y, int z);
}
