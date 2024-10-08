package dev.rndmorris.essentiapipes.blocks;

import dev.rndmorris.essentiapipes.Config;

public class BlockPipeSegmentVoidmetal extends BlockPipeSegment {

    protected BlockPipeSegmentVoidmetal() {
        super(Config.cycleLengthVoidmetal, Config.transferRateVoidmetal);
    }

    @Override
    protected String getId() {
        return ID_VOIDMETAL;
    }
}
