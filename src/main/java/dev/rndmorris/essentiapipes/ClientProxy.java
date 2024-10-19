package dev.rndmorris.essentiapipes;

import cpw.mods.fml.common.event.FMLInitializationEvent;
import dev.rndmorris.essentiapipes.client.BlockPhialDisplayRenderer;
import dev.rndmorris.essentiapipes.client.BlockPipeSegmentRenderer;
import dev.rndmorris.essentiapipes.client.CreativeTab;
import dev.rndmorris.essentiapipes.client.TileEntityPhialDisplayRenderer;

@SuppressWarnings("unused")
public class ClientProxy extends CommonProxy {

    private final CreativeTab creativeTab;

    public ClientProxy() {
        this.creativeTab = new CreativeTab();
    }

    public void init(FMLInitializationEvent event) {
        super.init(event);
        registerRenderers();
    }

    // Override CommonProxy methods here, if you want a different behaviour on the client (e.g. registering renders).
    // Don't forget to call the super methods as well.

    private void registerRenderers() {
        BlockPipeSegmentRenderer.init();
        BlockPhialDisplayRenderer.init();
        TileEntityPhialDisplayRenderer.init();
    }

    @Override
    public CreativeTab getCreativeTab() {
        return creativeTab;
    }
}
