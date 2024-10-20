package dev.rndmorris.essentiapipes.client;

import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

import org.lwjgl.opengl.GL11;

import cpw.mods.fml.client.registry.ClientRegistry;
import dev.rndmorris.essentiapipes.blocks.BlockPhialDisplay;
import dev.rndmorris.essentiapipes.data.BlockBounds;
import dev.rndmorris.essentiapipes.data.StoragePhial;
import dev.rndmorris.essentiapipes.tile.TileEntityPhialDisplay;
import thaumcraft.common.blocks.BlockTube;
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
        BlockPhialDisplayRenderer.positionedPhialBounds[0].expand(expand),
        BlockPhialDisplayRenderer.positionedPhialBounds[1].expand(expand),
        BlockPhialDisplayRenderer.positionedPhialBounds[2].expand(expand),
        BlockPhialDisplayRenderer.positionedPhialBounds[3].expand(expand), };

    public static final BlockBounds[] positionedPhialTubeBounds = calculatePhialTubeBounds();

    private static BlockBounds[] calculatePhialTubeBounds() {
        final var allPhialBounds = BlockPhialDisplayRenderer.positionedPhialBounds;
        final var result = new BlockBounds[allPhialBounds.length];

        final var pipeLength = pixels(4); // to-do: calculate proper length
        final var pipeWidth = pixels(2);

        for (var index = 0; index < allPhialBounds.length; ++index) {
            final var phialBounds = allPhialBounds[index];
            float minX, minY, minZ, maxX, maxY, maxZ;

            minX = phialBounds.minX + ((phialBounds.maxX - phialBounds.minX) / 2) - (pipeWidth / 2);
            maxX = minX + pipeWidth;

            minY = phialBounds.maxY - pixels(1);
            maxY = minY + pipeLength;

            minZ = phialBounds.minZ + ((phialBounds.maxZ - phialBounds.minZ) / 2) - (pipeWidth / 2);
            maxZ = minZ + pipeWidth;

            result[index] = new BlockBounds(minX, minY, minZ, maxX, maxY, maxZ);
        }

        return result;
    }

    private BlockPhialDisplay blockPhialDisplay;

    public void renderPhialDisplay(TileEntityPhialDisplay tile, double x, double y, double z) {
        if (blockPhialDisplay == null) {
            blockPhialDisplay = BlockPhialDisplay.getInstance();
        }

        final var phials = tile.getPhialSet()
            .getPhials();
        final var renderer = new RenderBlocks();
        renderer.blockAccess = tile.getWorldObj();

//        if (tile.hasTube()) {
//            ScaledRenderHelper.UV_OVERRIDES.update((dir, o) -> {
//                final var subPx = 1F / 32F;
//                switch (dir) {
//                    case UP, DOWN -> {
//                        o.minU = 16 * 14 * subPx;
//                        o.maxU = o.minU + (4 * subPx);
//                        o.minV = o.minU;
//                        o.maxV = o.maxU;
//                    }
//                    default -> {
//                        o.minU = 16 * 14 * subPx;
//                        o.maxU = o.minU + (4 * subPx);
//                        o.minV = 16 - (16 * 8 * subPx);
//                        o.maxV = 16;
//                    }
//                }
//            });
//        }
        for (var index = 0; index < phials.length; ++index) {
//            if (tile.hasTube()) {
//                renderPhialTube(tile, renderer, positionedPhialTubeBounds[index], (int) x, (int) y, (int) z);
//            }

            var phial = phials[index];
            if (phial == null || phial.getAmount() < 1) {
                continue;
            }
            renderEssentia(renderer, tile, phial, positionedLiquidBounds[index], x, y, z);
        }
    }

    private void renderPhialTube(TileEntityPhialDisplay tile, RenderBlocks renderer, BlockBounds bounds, int x, int y, int z) {
        final var tubeIcon = ((BlockTube) ConfigBlocks.blockTube).icon[0];

        blockPhialDisplay.setBlockBounds(bounds);
        renderer.setOverrideBlockTexture(tubeIcon);

        ScaledRenderHelper.renderStandardBlock(renderer, blockPhialDisplay, x, y, z);

        renderer.clearOverrideBlockTexture();
        blockPhialDisplay.setBlockBoundsBasedOnState(tile.getWorldObj(), x, y, z);
    }

    private void renderEssentia(RenderBlocks renderer, TileEntityPhialDisplay te, StoragePhial phial,
        BlockBounds bounds, double x, double y, double z) {
        if (this.field_147501_a.field_147553_e == null) {
            return;
        }
        GL11.glPushMatrix();
        GL11.glDisable(2884);
        GL11.glTranslatef((float) x + 0.5F, (float) y + 0.01F, (float) z + 0.5F);
        GL11.glRotatef(180.0F, 1.0F, 0.0F, 0.0F);
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);

        final var icon = blockPhialDisplay.icons.liquid;
        GL11.glPushMatrix();
        GL11.glRotatef(180.0F, 1.0F, 0.0F, 0.0F);
        World world = te.getWorldObj();
        GL11.glDisable(2896);
        Tessellator tessellator = Tessellator.instance;

        final var fullness = (bounds.maxY - bounds.minY) * phial.getPercentFull();

        renderer
            .setRenderBounds(bounds.minX, bounds.minY, bounds.minZ, bounds.maxX, bounds.minY + fullness, bounds.maxZ);
        tessellator.startDrawingQuads();
        if (phial.getAspect() != null) {
            tessellator.setColorOpaque_I(
                phial.getAspect()
                    .getColor());
        }

        int bright = 200;
        if (world != null) {
            bright = Math.max(
                200,
                ConfigBlocks.blockJar.getMixedBrightnessForBlock(te.getWorldObj(), te.xCoord, te.yCoord, te.zCoord));
        }

        tessellator.setBrightness(bright);
        this.field_147501_a.field_147553_e.bindTexture(TextureMap.locationBlocksTexture);
        renderer.renderFaceYNeg(blockPhialDisplay, -0.5D, 0.0D, -0.5D, icon);
        renderer.renderFaceYPos(blockPhialDisplay, -0.5D, 0.0D, -0.5D, icon);
        renderer.renderFaceZNeg(blockPhialDisplay, -0.5D, 0.0D, -0.5D, icon);
        renderer.renderFaceZPos(blockPhialDisplay, -0.5D, 0.0D, -0.5D, icon);
        renderer.renderFaceXNeg(blockPhialDisplay, -0.5D, 0.0D, -0.5D, icon);
        renderer.renderFaceXPos(blockPhialDisplay, -0.5D, 0.0D, -0.5D, icon);
        tessellator.draw();
        GL11.glEnable(2896);
        GL11.glPopMatrix();
        GL11.glColor3f(1.0F, 1.0F, 1.0F);

        GL11.glEnable(2884);
        GL11.glPopMatrix();
    }

    @Override
    public void renderTileEntityAt(TileEntity tileEntity, double x, double y, double z, float f) {
        if (tileEntity instanceof TileEntityPhialDisplay display) {
            renderPhialDisplay(display, x, y, z);
        }
    }
}
