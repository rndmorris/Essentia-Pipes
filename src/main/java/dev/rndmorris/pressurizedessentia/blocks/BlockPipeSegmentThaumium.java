package dev.rndmorris.pressurizedessentia.blocks;

import dev.rndmorris.pressurizedessentia.Config;

public class BlockPipeSegmentThaumium extends BlockPipeSegment {

    protected BlockPipeSegmentThaumium() {
        super(Config.cycleLengthThaumium, Config.transferRateThaumium);
    }

    @Override
    protected String getId() {
        return ID_THAUMIUM;
    }
}
