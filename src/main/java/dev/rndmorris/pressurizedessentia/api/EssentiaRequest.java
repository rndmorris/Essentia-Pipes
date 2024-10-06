package dev.rndmorris.pressurizedessentia.api;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.ForgeDirection;

import thaumcraft.api.aspects.Aspect;

public class EssentiaRequest {

    public static final String ASPECT = "aspect";
    public static final String DISTANCE = "distance";
    public static final String SUCTION = "suction";
    public static final String DESTINATION = "destination";
    public static final String DESTINATION_FACE = "destinationFace";

    public Aspect aspect;
    public int distance = Integer.MAX_VALUE;
    public int suction;
    public WorldCoordinate destination;
    public ForgeDirection destinationFace;

    public EssentiaRequest(NBTTagCompound nbtTagCompound) {
        readFromNBT(nbtTagCompound);
    }

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

    public void writeToNBT(NBTTagCompound nbtTagCompound) {
        if (aspect != null) {
            nbtTagCompound.setString(ASPECT, aspect.getTag());
        }
        nbtTagCompound.setInteger(DISTANCE, distance);
        nbtTagCompound.setInteger(SUCTION, suction);
        nbtTagCompound.setIntArray(
            DESTINATION,
            new int[] { destination.dimensionId(), destination.x(), destination.y(), destination.z(), });
        nbtTagCompound.setInteger(DESTINATION_FACE, destinationFace.ordinal());
    }

    public void readFromNBT(NBTTagCompound nbtTagCompound) {
        if (nbtTagCompound.hasKey(ASPECT)) {
            final var aspectTag = nbtTagCompound.getString(ASPECT);
            aspect = Aspect.getAspect(aspectTag);
        }
        distance = nbtTagCompound.getInteger(DISTANCE);
        suction = nbtTagCompound.getInteger(SUCTION);

        final var destArr = nbtTagCompound.getIntArray(DESTINATION);

        destination = new WorldCoordinate(0, 0, 0, 0);

        if (destArr.length >= 4) {
            destination = new WorldCoordinate(destArr[0], destArr[1], destArr[2], destArr[3]);
        }

        final var faceInt = nbtTagCompound.getInteger(DESTINATION_FACE);
        destinationFace = ForgeDirection.getOrientation(faceInt);
    }
}
