package dev.rndmorris.essentiapipes.client;

import dev.rndmorris.essentiapipes.tile.TileEntityPhialDisplay;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.client.renderers.models.ModelJar;

public class TileEntityPhialDisplayRenderer extends TileEntitySpecialRenderer {

    private ModelJar jarModel = new ModelJar();

    private final float[][][] JAR_POSITION = new float[][][] {
        new float[][] { new float[] { .5F, .5F, }, },
        new float[][] { new float[] { .5F, .5F, }, new float[] { .5F, .5F, }, },
        new float[][] { new float[] { .5F, .5F, }, new float[] { .5F, .5F, }, new float[] { .5F, .5F, }, },
        new float[][] { new float[] { .5F, .5F, }, new float[] { .5F, .5F, }, new float[] { .5F, .5F, }, new float[] { .5F, .5F, }, },
    };


    public void renderPhialDisplay(TileEntityPhialDisplay phialDisplay, double x, double y, double z, float f) {


    }

    public void renderPhial(Aspect aspect, int amount, double x, double y, double z) {

    }

    @Override
    public void renderTileEntityAt(TileEntity p_147500_1_, double p_147500_2_, double p_147500_4_, double p_147500_6_, float p_147500_8_) {

    }
}
