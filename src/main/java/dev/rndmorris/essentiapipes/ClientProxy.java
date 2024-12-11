package dev.rndmorris.essentiapipes;

import net.minecraftforge.common.MinecraftForge;

import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import dev.rndmorris.essentiapipes.blocks.BlockTinyJar;
import dev.rndmorris.essentiapipes.client.CreativeTab;
import dev.rndmorris.essentiapipes.client.Renderer;

@SuppressWarnings("unused")
public class ClientProxy extends CommonProxy {

    private final CreativeTab creativeTab;

    public ClientProxy() {
        this.creativeTab = new CreativeTab();
    }

    @Override
    public void preInit(FMLPreInitializationEvent event) {
        super.preInit(event);
        MinecraftForge.EVENT_BUS.register(BlockTinyJar.instance);
    }

    public void init(FMLInitializationEvent event) {
        super.init(event);
        Renderer.init();
    }

    @Override
    public CreativeTab getCreativeTab() {
        return creativeTab;
    }
}
