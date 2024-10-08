package dev.rndmorris.pressurizedessentia.api;

import thaumcraft.api.aspects.IEssentiaTransport;

/**
 * Implement on a tile entity that should participate in an essentia pipe network.
 */
public interface IIOPipeSegment extends IEssentiaTransport {

    /**
     * Determine if an essentia request could potentially be fulfilled by this node.
     * 
     * @param incomingRequest The request to evaluate.
     * @return True if the request could be granted by this IO segment, or false if not. A true result DOES NOT mean a
     *         request will be fulfilled.
     */
    boolean evaluateEssentiaRequest(EssentiaRequest incomingRequest);

    /**
     * The world coordinate of this IO segment.
     * 
     * @return This segment's world coordinate.
     */
    WorldCoordinate getCoordinate();

    /**
     * Tell the IO segment that it needs to rebuild its connections.
     */
    void rebuildIOConnections();
}
