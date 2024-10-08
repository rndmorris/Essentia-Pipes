package dev.rndmorris.essentiapipes.data;

import java.util.TreeSet;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.nbt.NBTTagCompound;

import dev.rndmorris.essentiapipes.api.ConnectionInfo;
import dev.rndmorris.essentiapipes.api.WorldCoordinate;

public class ConnectionSet extends TreeSet<ConnectionInfo> {

    public static final String DIM = "dimensionId";
    public static final String X = "x";
    public static final String Y = "y";
    public static final String Z = "z";
    public static final String DIST = "distance";

    public void writeToNBT(@Nonnull NBTTagCompound nbtTagCompound) {
        final var dim = new int[this.size()];
        final var x = new int[this.size()];
        final var y = new int[this.size()];
        final var z = new int[this.size()];
        final var dist = new int[this.size()];
        var index = 0;
        for (var info : this) {
            final var pos = info.coordinate();
            dim[index] = pos.dimensionId();
            x[index] = pos.x();
            y[index] = pos.y();
            z[index] = pos.z();
            dist[index] = info.distance();
        }
        nbtTagCompound.setIntArray(DIM, dim);
        nbtTagCompound.setIntArray(X, x);
        nbtTagCompound.setIntArray(Y, y);
        nbtTagCompound.setIntArray(Z, z);
        nbtTagCompound.setIntArray(DIST, dist);
    }

    public void readFromNBT(@Nullable NBTTagCompound nbtTagCompound) {
        clear();
        if (nbtTagCompound == null || missingAnyKeys(nbtTagCompound)) {
            return;
        }
        final var dim = nbtTagCompound.getIntArray(DIM);
        final var x = nbtTagCompound.getIntArray(X);
        final var y = nbtTagCompound.getIntArray(Y);
        final var z = nbtTagCompound.getIntArray(Z);
        final var dist = nbtTagCompound.getIntArray(DIST);

        if (mismatchedLengths(dim, x, y, z, dist)) {
            return;
        }

        for (var index = 0; index < dim.length; ++index) {
            this.add(new ConnectionInfo(new WorldCoordinate(dim[index], x[index], y[index], z[index]), dist[index]));
        }
    }

    private static boolean missingAnyKeys(@Nonnull NBTTagCompound nbtTagCompound) {
        return !(nbtTagCompound.hasKey(DIM) && nbtTagCompound.hasKey(X)
            && nbtTagCompound.hasKey(Y)
            && nbtTagCompound.hasKey(Z)
            && nbtTagCompound.hasKey(DIST));
    }

    private static boolean mismatchedLengths(int[]... arrays) {
        var length = -1;
        for (var arr : arrays) {
            if (length < 0) {
                length = arr.length;
                continue;
            }
            if (length != arr.length) {
                return true;
            }
        }
        return false;
    }

}
