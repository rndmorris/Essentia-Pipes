package dev.rndmorris.pressurizedessentia.api;

import net.minecraftforge.common.util.ForgeDirection;

import thaumcraft.api.aspects.Aspect;

public class EssentiaRequest {

    public WorldCoordinate requestor;
    public Aspect aspect;
    public int suction;
    public ForgeDirection willOutputTo;

    public EssentiaRequest(WorldCoordinate requestor, Aspect aspect, int suction, ForgeDirection willOutputTo) {
        this.requestor = requestor;
        this.aspect = aspect;
        this.suction = suction;
        this.willOutputTo = willOutputTo;
    }

    public int effectiveSuction(int distance) {
        return Integer.max(0, suction - distance);
    }

    public boolean isLowerPriorityThan(EssentiaRequest request, int distance) {
        final var beatsAspect = this.aspect == null || request.aspect == this.aspect;
        if (!beatsAspect) {
            return false;
        }
        final var effectiveSuctionDiff = request.effectiveSuction(distance) - effectiveSuction(distance);
        if (effectiveSuctionDiff == 0) {
            return request.suction > suction;
        }
        return effectiveSuctionDiff > 0;
    }
}
