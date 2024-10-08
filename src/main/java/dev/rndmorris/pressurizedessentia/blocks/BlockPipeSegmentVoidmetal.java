package dev.rndmorris.pressurizedessentia.blocks;

import dev.rndmorris.pressurizedessentia.Config;

public class BlockPipeSegmentVoidmetal extends BlockPipeSegment {

    protected BlockPipeSegmentVoidmetal() {
        super(Config.transferRateVoidmetal);
    }

    @Override
    protected String getId() {
        return ID_VOIDMETAL;
    }
}
