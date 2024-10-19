package dev.rndmorris.essentiapipes.client;

import cpw.mods.fml.client.registry.ClientRegistry;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;

import dev.rndmorris.essentiapipes.tile.TileEntityPhialDisplay;

public class TileEntityPhialDisplayRenderer extends TileEntitySpecialRenderer {

    public static TileEntityPhialDisplayRenderer instance;

    public static void init() {
        instance = new TileEntityPhialDisplayRenderer();
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityPhialDisplay.class, instance);
    }

    public void renderPhialDisplay(TileEntityPhialDisplay tile, double x, double y, double z, float f) {

    }

    @Override
    public void renderTileEntityAt(TileEntity tileEntity, double x, double y, double z,
        float f) {
        if (tileEntity instanceof TileEntityPhialDisplay display) {
            renderPhialDisplay(display, x, y, z, f);
        }
    }
}
