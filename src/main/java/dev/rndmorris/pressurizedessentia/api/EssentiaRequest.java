package dev.rndmorris.pressurizedessentia.api;

import net.minecraftforge.common.util.ForgeDirection;

import thaumcraft.api.aspects.Aspect;

public class EssentiaRequest {

    public Aspect aspect;
    public int distance = Integer.MAX_VALUE;
    public int suction;
    public WorldCoordinate requestor;
    public ForgeDirection willOutputTo;

    public EssentiaRequest(WorldCoordinate requestor, ForgeDirection willOutputTo, Aspect aspect, int suction) {
        this.aspect = aspect;
        this.requestor = requestor;
        this.willOutputTo = willOutputTo;
        this.suction = suction;
    }

    public int effectiveSuction() {
        return Integer.max(0, suction - distance);
    }

    public boolean isLowerPriorityThan(EssentiaRequest request) {
        final var effectiveSuctionDiff = request.effectiveSuction() - effectiveSuction();
        if (effectiveSuctionDiff == 0) {
            return request.suction > suction;
        }
        return effectiveSuctionDiff > 0;
    }
}
