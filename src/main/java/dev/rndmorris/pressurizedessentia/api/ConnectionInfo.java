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
        return Integer.compare(thiz.distance, that.distance);
    }

    public IIOPipeSegment getIOSegment() {
        return PipeHelper.getIOSegment(coordinate);
    }
}
