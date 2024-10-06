package dev.rndmorris.pressurizedessentia.api;

import thaumcraft.api.aspects.IEssentiaTransport;

public interface IIOPipeSegment extends IPipeSegment, IEssentiaTransport {

    boolean acceptRequestFulfillment(EssentiaRequest fulfilledRequest, int amount);

    boolean evaluateEssentiaRequest(EssentiaRequest incomingRequest);

    WorldCoordinate getCoordinate();

    void rebuildIOConnections();
}
