package dev.rndmorris.pressurizedessentia;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;

@Mod(
    modid = PressurizedEssentia.MODID,
    version = Tags.VERSION,
    name = "PressurizedEssentia",
    acceptedMinecraftVersions = "[1.7.10]")
public class PressurizedEssentia {

    public static final String MODID = "pressurizedessentia";
    @SuppressWarnings("unused")
    public static final Logger LOG = LogManager.getLogger(MODID);

    public static String modid(String name) {
        return String.format("%s:%s", MODID, name);
    }

    @SidedProxy(
        clientSide = "dev.rndmorris.pressurizedessentia.ClientProxy",
        serverSide = "dev.rndmorris.pressurizedessentia.CommonProxy")
    public static CommonProxy proxy;

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        proxy.preInit(event);
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        proxy.init(event);
    }
}
