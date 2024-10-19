package dev.rndmorris.essentiapipes.client;

import cpw.mods.fml.client.registry.ClientRegistry;
import dev.rndmorris.essentiapipes.blocks.BlockPhialDisplay;
import dev.rndmorris.essentiapipes.data.BlockBounds;
import dev.rndmorris.essentiapipes.data.StoragePhial;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;

import dev.rndmorris.essentiapipes.tile.TileEntityPhialDisplay;
import net.minecraft.world.World;
import org.lwjgl.opengl.GL11;
import thaumcraft.common.config.ConfigBlocks;

public class TileEntityPhialDisplayRenderer extends TileEntitySpecialRenderer {

    public static TileEntityPhialDisplayRenderer instance;

    public static void init() {
        instance = new TileEntityPhialDisplayRenderer();
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityPhialDisplay.class, instance);
    }

    public static float pixels(float count) {
        return count / 16F;
    }

    public static final float expand = pixels(-0.5F);
    public static final BlockBounds[] positionedLiquidBounds = new BlockBounds[] {
        BlockPhialDisplayRenderer.positionedPhialBounds[0].expand(expand, expand, expand),
        BlockPhialDisplayRenderer.positionedPhialBounds[1].expand(expand, expand, expand),
        BlockPhialDisplayRenderer.positionedPhialBounds[2].expand(expand, expand, expand),
        BlockPhialDisplayRenderer.positionedPhialBounds[3].expand(expand, expand, expand),
    };

    public void renderPhialDisplay(TileEntityPhialDisplay tile, double x, double y, double z, float f) {
        final var phials = tile.getPhials().getPhials();
        final var renderer = new RenderBlocks();
        for (var index = 0; index < phials.length; ++index) {
            var phial = phials[index];
            if (phial == null || phial.getAmount() < 1) {
                continue;
            }

            preRender(x, y, z);
            renderEssentia(renderer, tile, phial, positionedLiquidBounds[index]);
            postRender();

        }
    }

    private void preRender(double x, double y, double z) {
        GL11.glPushMatrix();
        GL11.glDisable(2884);
        GL11.glTranslatef((float)x + 0.5F, (float)y + 0.01F, (float)z + 0.5F);
        GL11.glRotatef(180.0F, 1.0F, 0.0F, 0.0F);
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
    }

    private void renderEssentia(RenderBlocks renderer, TileEntityPhialDisplay te, StoragePhial phial, BlockBounds bounds) {
        if(this.field_147501_a.field_147553_e == null) {
            return;
        }

        final var block = BlockPhialDisplay.getInstance();
        final var icon = block.icons.liquid;
        GL11.glPushMatrix();
        GL11.glRotatef(180.0F, 1.0F, 0.0F, 0.0F);
        World world = te.getWorldObj();
        GL11.glDisable(2896);
        Tessellator t = Tessellator.instance;

        final var fullness = (bounds.maxY - bounds.minY) * phial.getPercentFull();

        renderer.setRenderBounds(bounds.minX, bounds.minY, bounds.minZ, bounds.maxX, bounds.minY + fullness, bounds.maxZ);
        t.startDrawingQuads();
        if(phial.getAspect() != null) {
            t.setColorOpaque_I(phial.getAspect().getColor());
        }

        int bright = 200;
        if(world != null) {
            bright = Math.max(200, ConfigBlocks.blockJar.getMixedBrightnessForBlock(te.getWorldObj(), te.xCoord, te.yCoord, te.zCoord));
        }

        t.setBrightness(bright);
        this.field_147501_a.field_147553_e.bindTexture(TextureMap.locationBlocksTexture);
        renderer.renderFaceYNeg(block, -0.5D, 0.0D, -0.5D, icon);
        renderer.renderFaceYPos(block, -0.5D, 0.0D, -0.5D, icon);
        renderer.renderFaceZNeg(block, -0.5D, 0.0D, -0.5D, icon);
        renderer.renderFaceZPos(block, -0.5D, 0.0D, -0.5D, icon);
        renderer.renderFaceXNeg(block, -0.5D, 0.0D, -0.5D, icon);
        renderer.renderFaceXPos(block, -0.5D, 0.0D, -0.5D, icon);
        t.draw();
        GL11.glEnable(2896);
        GL11.glPopMatrix();
        GL11.glColor3f(1.0F, 1.0F, 1.0F);
    }

    private void postRender() {
        GL11.glEnable(2884);
        GL11.glPopMatrix();
    }

    @Override
    public void renderTileEntityAt(TileEntity tileEntity, double x, double y, double z,
        float f) {
        if (tileEntity instanceof TileEntityPhialDisplay display) {
            renderPhialDisplay(display, x, y, z, f);
        }
    }
}
