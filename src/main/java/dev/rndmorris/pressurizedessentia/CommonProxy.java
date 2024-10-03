package dev.rndmorris.pressurizedessentia;

import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import dev.rndmorris.pressurizedessentia.blocks.BlockPipeSegment;
import dev.rndmorris.pressurizedessentia.client.CreativeTab;

public class CommonProxy {

    // preInit "Run before anything else. Read your config, create blocks, items, etc., and register them with the
    // GameRegistry." (Remove if not needed)
    public void preInit(FMLPreInitializationEvent event) {
        Config.synchronizeConfiguration(event.getSuggestedConfigurationFile());
        BlockPipeSegment.preInit();
    }

    public CreativeTab getCreativeTab() {
        return null;
    }
}
