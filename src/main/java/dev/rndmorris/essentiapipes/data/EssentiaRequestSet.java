package dev.rndmorris.essentiapipes.data;

import java.util.Objects;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.ForgeDirection;

import dev.rndmorris.essentiapipes.api.EssentiaRequest;

public class EssentiaRequestSet {

    private final EssentiaRequest[] requests = new EssentiaRequest[ForgeDirection.VALID_DIRECTIONS.length];

    public EssentiaRequest getRequest(@Nonnull ForgeDirection direction) {
        return requests[requireValidDirection(direction).ordinal()];
    }

    public void setRequest(@Nonnull ForgeDirection direction, @Nullable EssentiaRequest request) {
        requests[requireValidDirection(direction).ordinal()] = request;
    }

    private static ForgeDirection requireValidDirection(ForgeDirection direction) {
        Objects.requireNonNull(direction);
        if (direction == ForgeDirection.UNKNOWN) {
            throw new IllegalArgumentException(
                String.format("%s is not a valid %s.", direction, ForgeDirection.class.getName()));
        }
        return direction;
    }

    public void updateWith(Updater updater) {
        Objects.requireNonNull(updater);
        for (var direction : ForgeDirection.VALID_DIRECTIONS) {
            requests[direction.ordinal()] = updater.update(direction, requests[direction.ordinal()]);
        }
    }

    public void clear() {
        updateWith((dir, request) -> null);
    }

    public interface Updater {

        @Nullable
        EssentiaRequest update(@Nonnull ForgeDirection direction, @Nullable EssentiaRequest request);
    }

    public void writeToNBT(NBTTagCompound nbtTagCompound) {
        for (var direction : ForgeDirection.VALID_DIRECTIONS) {
            final var request = getRequest(direction);

            if (request == null) {
                continue;
            }

            final var requestTag = new NBTTagCompound();
            request.writeToNBT(requestTag);
            nbtTagCompound.setTag(direction.toString(), requestTag);
        }
    }

    public void readFromNBT(NBTTagCompound nbtTagCompound) {
        for (var direction : ForgeDirection.VALID_DIRECTIONS) {
            if (!nbtTagCompound.hasKey(direction.toString())) {
                continue;
            }
            final var requestTag = nbtTagCompound.getCompoundTag(direction.toString());
            final var request = new EssentiaRequest(requestTag);
            setRequest(direction, request);
        }
    }

}
