package dev.rndmorris.pressurizedessentia.api;

import net.minecraftforge.common.util.ForgeDirection;

import thaumcraft.api.aspects.Aspect;

public class EssentiaRequest {

    public Aspect aspect;
    public int distance = Integer.MAX_VALUE;
    public int suction;
    public WorldCoordinate destination;
    public ForgeDirection destinationFace;

    public EssentiaRequest(WorldCoordinate destination, ForgeDirection destinationFace, Aspect aspect, int suction) {
        this.aspect = aspect;
        this.destination = destination;
        this.destinationFace = destinationFace;
        this.suction = suction;
    }

    public int effectiveSuction() {
        return Integer.max(0, suction - distance);
    }

    public boolean isSuperceededBy(EssentiaRequest request) {
        final var effectiveSuctionDiff = request.effectiveSuction() - effectiveSuction();
        if (effectiveSuctionDiff == 0) {
            // prefer closer connections, in case of effective suction ties
            return request.distance < distance;
        }
        return effectiveSuctionDiff > 0;
    }
}
