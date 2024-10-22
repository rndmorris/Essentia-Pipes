package dev.rndmorris.essentiapipes.client;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.common.util.ForgeDirection;

import cpw.mods.fml.client.registry.ISimpleBlockRenderingHandler;
import cpw.mods.fml.client.registry.RenderingRegistry;
import dev.rndmorris.essentiapipes.blocks.BlockPhialDisplay;
import dev.rndmorris.essentiapipes.data.BlockBounds;
import dev.rndmorris.essentiapipes.tile.TileEntityPhialDisplay;
import thaumcraft.client.renderers.block.BlockRenderer;
import thaumcraft.common.blocks.BlockTube;
import thaumcraft.common.config.ConfigBlocks;

public class BlockPhialDisplayRenderer implements ISimpleBlockRenderingHandler {

    //
    // Statics
    //

    public static int renderId;
    public static BlockPhialDisplayRenderer instance;

    public static float pixels(float count) {
        return count / 16F;
    }
    public static float subPixel(float count) {
        return count / 32F;
    }

    private static final float inset = 2F / 16F;
    public static final BlockBounds phialBound = new BlockBounds(pixels(10), pixels(12), pixels(10)).multiply(1F / 2F);
    public static final BlockBounds phialTubeBound = new BlockBounds(pixels(2), pixels(3), pixels(2));

    public static final BlockBounds[] positionedPhialBounds = new BlockBounds[] {
        phialBound.transform(inset, 0, inset), phialBound.transform(inset, 0, 1 - phialBound.maxZ - inset),
        phialBound.transform(1 - phialBound.maxX - inset, 0, inset),
        phialBound.transform(1 - phialBound.maxX - inset, 0, 1 - phialBound.maxZ - inset), };

    public static final BlockBounds[] positionedPhialTubeBounds = new BlockBounds[4];

    public static void init() {
        renderId = RenderingRegistry.getNextAvailableRenderId();
        instance = new BlockPhialDisplayRenderer();
        RenderingRegistry.registerBlockHandler(renderId, instance);
    }

    static {
        final var allPhialBounds = positionedPhialBounds;
        for (var index = 0; index < allPhialBounds.length; ++index) {
            final var b = allPhialBounds[index];
            final var deltaX = b.minX + (b.diffX / 2) - (phialTubeBound.maxX / 2F);
            final var deltaY = b.maxY - pixels(1);
            final var deltaZ = b.minZ + (b.diffZ / 2) - (phialTubeBound.maxZ / 2F);
            positionedPhialTubeBounds[index] = phialTubeBound.transform(deltaX, deltaY, deltaZ);
        }
    }

    private static void trimJarTexture(ForgeDirection direction, ScaledRenderHelper.Overrides.Override over) {
        switch (direction) {
            case DOWN, UP -> {
                over.minU = 3;
                over.maxU = 16 - over.minU;
                over.minV = 3;
                over.maxV = 16 - over.minV;
            }
            default -> {
                over.minU = 3;
                over.maxU = 16 - over.minU;
                over.minV = 16;
                over.maxV = 4;
            }
        }
    }

    private static void trimPhialTubeTexture(ForgeDirection dir, ScaledRenderHelper.Overrides.Override o) {
        switch (dir) {
            case UP, DOWN -> {
                o.minU = 7;
                o.maxU = o.minU + 2;
                o.minV = o.minU;
                o.maxV = o.minV;
            }
            default -> {
                o.minU = 7;
                o.maxU = o.minU + 2;
                o.minV = 9;
                o.maxV = o.minV + 7;
            }
        }
    }

    private static void trimNSTubeTexture(ForgeDirection dir, ScaledRenderHelper.Overrides.Override o) {
        switch (dir) {
            case UP, DOWN -> {
                o.minU = 7;
                o.maxU = o.minU + 2;
                o.minV = 0;
                o.maxV = 16;
            }
            case EAST, WEST -> {
                o.minU = 0;
                o.maxU = 16;
                o.minV = 7;
                o.maxV = o.minV + 2;
            }
            default -> {
                o.minU = 7;
                o.maxU = o.minU + 2;
                o.minV = o.minU;
                o.maxV = o.maxU;
            }
        }
    }

    private static void trimEWTubeTexture(ForgeDirection dir, ScaledRenderHelper.Overrides.Override o) {
        switch (dir) {
            case UP, DOWN, NORTH, SOUTH -> {
                o.minU = 0;
                o.maxU = 16;
                o.minV = 7;
                o.maxV = o.minV + 2;
            }
            default -> {
                o.minU = 7;
                o.maxU = o.minU + 2;
                o.minV = o.minU;
                o.maxV = o.minV;
            }
        }
    }

    //
    // Instance
    //

    private BlockPhialDisplay blockPhialDisplay;

    private boolean renderWorldBlock(IBlockAccess world, int x, int y, int z, RenderBlocks renderer) {
        BlockRenderer.setBrightness(world, x, y, z, blockPhialDisplay);

        final var tile = (TileEntityPhialDisplay) world.getTileEntity(x, y, z);
        renderTubes(tile, renderer, x, y, z);
        renderPhials(tile, renderer, x, y, z);

        blockPhialDisplay.setBlockBoundsBasedOnState(world, x, y, z);
        renderer.setRenderBoundsFromBlock(blockPhialDisplay);
        return true;
    }

    private void renderPhials(TileEntityPhialDisplay tile, RenderBlocks renderer, int x, int y, int z) {
        ScaledRenderHelper.UV_OVERRIDES.update(BlockPhialDisplayRenderer::trimJarTexture);
        for (var phialIndex = 0; phialIndex < positionedPhialBounds.length; ++phialIndex) {
            if (!tile.getPhialSet()
                .hasPhialAt(phialIndex)) {
                continue;
            }
            blockPhialDisplay.setBlockBounds(positionedPhialBounds[phialIndex]);
            renderer.setRenderBoundsFromBlock(blockPhialDisplay);
            ScaledRenderHelper.renderStandardBlock(renderer, blockPhialDisplay, x, y, z);
        }
    }

    private void renderTubes(TileEntityPhialDisplay tile, RenderBlocks renderer, int x, int y, int z) {
        if (!tile.hasTube()) {
            return;
        }

        final var tubeIcon = ((BlockTube) ConfigBlocks.blockTube).icon[0];
        ScaledRenderHelper.UV_OVERRIDES.update(BlockPhialDisplayRenderer::trimPhialTubeTexture);

        for (var index = 0; index < positionedPhialTubeBounds.length; ++index) {
            if (!tile.getPhialSet().hasPhialAt(index)) {
                continue;
            }

            final var positionedPhialTubeBound = positionedPhialTubeBounds[index];
            blockPhialDisplay.setBlockBounds(positionedPhialTubeBound);
            renderer.setRenderBoundsFromBlock(blockPhialDisplay);

            ScaledRenderHelper.renderFaceXNeg(renderer, blockPhialDisplay, x, y, z, tubeIcon);
            ScaledRenderHelper.renderFaceXPos(renderer, blockPhialDisplay, x, y, z, tubeIcon);
            ScaledRenderHelper.renderFaceZNeg(renderer, blockPhialDisplay, x, y, z, tubeIcon);
            ScaledRenderHelper.renderFaceZPos(renderer, blockPhialDisplay, x, y, z, tubeIcon);
            ScaledRenderHelper.renderFaceYNeg(renderer, blockPhialDisplay, x, y, z, tubeIcon);
        }

        final var nwBound = positionedPhialTubeBounds[0];
        final var swBound = positionedPhialTubeBounds[1];
        final var neBound = positionedPhialTubeBounds[2];
        final var seBound = positionedPhialTubeBounds[3];

        ScaledRenderHelper.UV_OVERRIDES.update(BlockPhialDisplayRenderer::trimNSTubeTexture);
        // Western North-South tube
        blockPhialDisplay.setBlockBounds(nwBound.minX, nwBound.maxY, nwBound.minZ, swBound.maxX, swBound.maxY + pixels(2), swBound.maxZ);
        renderer.setRenderBoundsFromBlock(blockPhialDisplay);
        ScaledRenderHelper.renderAllFaces(renderer, blockPhialDisplay, x, y, z, tubeIcon);

        // Eastern North-South tube
        blockPhialDisplay.setBlockBounds(neBound.minX, neBound.maxY, neBound.minZ, seBound.maxX, seBound.maxY + pixels(2), seBound.maxZ);
        renderer.setRenderBoundsFromBlock(blockPhialDisplay);
        ScaledRenderHelper.renderAllFaces(renderer, blockPhialDisplay, x, y, z, tubeIcon);

        // East-West tube
        ScaledRenderHelper.UV_OVERRIDES.update(BlockPhialDisplayRenderer::trimEWTubeTexture);
        blockPhialDisplay.setBlockBounds(nwBound.maxX, nwBound.maxY, pixels(7), neBound.minX, neBound.maxY + pixels(2), pixels(9));
        renderer.setRenderBoundsFromBlock(blockPhialDisplay);
        ScaledRenderHelper.renderAllFaces(renderer, blockPhialDisplay, x, y, z, tubeIcon);

        // reset
        blockPhialDisplay.setBlockBoundsBasedOnState(renderer.blockAccess, x, y, z);
    }

    //
    // ISimpleBlockRenderingHandler
    //

    @Override
    public void renderInventoryBlock(Block block, int metadata, int modelId, RenderBlocks renderer) {}

    @Override
    public boolean renderWorldBlock(IBlockAccess world, int x, int y, int z, Block block, int modelId,
        RenderBlocks renderer) {
        if (blockPhialDisplay == null) {
            blockPhialDisplay = BlockPhialDisplay.getInstance();
        }
        if (block == blockPhialDisplay) {
            return renderWorldBlock(world, x, y, z, renderer);
        }
        return false;
    }

    @Override
    public boolean shouldRender3DInInventory(int modelId) {
        return true;
    }

    @Override
    public int getRenderId() {
        return renderId;
    }
}
