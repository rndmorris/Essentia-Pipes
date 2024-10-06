package dev.rndmorris.pressurizedessentia.api;

import java.util.Comparator;

import javax.annotation.Nonnull;

import com.github.bsideup.jabel.Desugar;

@Desugar
public record ConnectionInfo(WorldCoordinate coordinate, int distance)
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

        compare = Integer.compare(thiz.coordinate.x(), that.coordinate.x());
        if (compare != 0) {
            return compare;
        }

        compare = Integer.compare(thiz.coordinate.y(), that.coordinate.y());
        if (compare != 0) {
            return compare;
        }

        return Integer.compare(thiz.coordinate.z(), that.coordinate.z());
    }

    public IIOPipeSegment getIOSegment() {
        return PipeHelper.getIOSegment(coordinate);
    }
}
