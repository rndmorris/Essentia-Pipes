package dev.rndmorris.essentiapipes;

import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import dev.rndmorris.essentiapipes.blocks.BlockPipeSegment;
import dev.rndmorris.essentiapipes.blocks.BlockTinyJar;
import dev.rndmorris.essentiapipes.client.CreativeTab;

public class CommonProxy {

    // preInit "Run before anything else. Read your config, create blocks, items, etc., and register them with the
    // GameRegistry." (Remove if not needed)
    public void preInit(FMLPreInitializationEvent event) {
        Config.synchronizeConfiguration(event.getSuggestedConfigurationFile());
        BlockPipeSegment.preInit();
        BlockTinyJar.preInit();
    }

    public void init(FMLInitializationEvent event) {

    }

    public void postInit(FMLPostInitializationEvent event) {
        Recipes.postInit();
        Research.postInit();
    }

    public CreativeTab getCreativeTab() {
        return null;
    }
}
