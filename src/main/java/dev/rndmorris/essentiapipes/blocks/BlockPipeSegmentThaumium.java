package dev.rndmorris.essentiapipes.blocks;

import dev.rndmorris.essentiapipes.Config;

public class BlockPipeSegmentThaumium extends BlockPipeSegment {

    protected BlockPipeSegmentThaumium() {
        super(Config.cycleLengthThaumium, Config.transferRateThaumium);
    }

    @Override
    protected String getId() {
        return ID_THAUMIUM;
    }
}
