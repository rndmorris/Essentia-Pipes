package dev.rndmorris.essentiapipes.api;

import java.util.Comparator;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.github.bsideup.jabel.Desugar;

/**
 * Represents a connection between one IIOPipeSegment and another, and the taxicab distance between them.
 *
 * @param coordinate The position of the target IIOPipeSegment.
 * @param distance   The taxicab distance between this ConnectionInfo's owner and the coordinate.
 */
@Desugar
public record ConnectionInfo(@Nonnull WorldCoordinate coordinate, int distance)
    implements Comparable<ConnectionInfo>, Comparator<ConnectionInfo> {

    @Override
    public int compareTo(@Nonnull ConnectionInfo that) {
        return compare(this, that);
    }

    @Override
    public int compare(ConnectionInfo thiz, ConnectionInfo that) {
        var compare = Integer.compare(thiz.distance, that.distance);
        if (compare != 0) {
            return compare;
        }

        return thiz.coordinate.compareTo(that.coordinate);
    }

    /**
     * Get the IIOPipeSegment at the coordinate.
     *
     * @return The IIOPipeSegment if it exists, or null if not.
     */
    public @Nullable IIOPipeSegment getIOSegment() {
        return coordinate.getTileEntity(IIOPipeSegment.class);
    }
}
