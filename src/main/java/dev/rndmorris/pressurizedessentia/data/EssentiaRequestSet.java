package dev.rndmorris.pressurizedessentia.data;

import java.util.Objects;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraftforge.common.util.ForgeDirection;

import dev.rndmorris.pressurizedessentia.api.EssentiaRequest;

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

}
