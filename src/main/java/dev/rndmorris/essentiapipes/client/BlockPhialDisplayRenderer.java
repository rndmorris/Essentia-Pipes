package dev.rndmorris.essentiapipes.client;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.world.IBlockAccess;

import cpw.mods.fml.client.registry.ISimpleBlockRenderingHandler;
import cpw.mods.fml.client.registry.RenderingRegistry;
import dev.rndmorris.essentiapipes.blocks.BlockPhialDisplay;
import dev.rndmorris.essentiapipes.data.BlockBounds;
import thaumcraft.client.renderers.block.BlockRenderer;

public class BlockPhialDisplayRenderer implements ISimpleBlockRenderingHandler {

    public static int renderId;
    public static BlockPhialDisplayRenderer instance = new BlockPhialDisplayRenderer();

    public static final BlockBounds bounds1 = new BlockBounds(
        BlockRenderer.W3,
        0.0F,
        BlockRenderer.W3,
        BlockRenderer.W13,
        BlockRenderer.W12,
        BlockRenderer.W13).scale(.5F);
    public static final BlockBounds bounds2 = new BlockBounds(
        BlockRenderer.W5,
        BlockRenderer.W12,
        BlockRenderer.W5,
        BlockRenderer.W11,
        BlockRenderer.W14,
        BlockRenderer.W11).scale(.5F);

    public static void init() {
        renderId = RenderingRegistry.getNextAvailableRenderId();
        instance = new BlockPhialDisplayRenderer();
        RenderingRegistry.registerBlockHandler(renderId, instance);
    }

    @Override
    public void renderInventoryBlock(Block block, int metadata, int modelId, RenderBlocks renderer) {
        // if (!(block instanceof BlockPhialDisplay display)) {
        // return;
        // }
        // renderer.renderFaceYPos(block, 0.0D, 0.0D, 0.0D, display.icons.phial);
    }

    @Override
    public boolean renderWorldBlock(IBlockAccess world, int x, int y, int z, Block block, int modelId,
        RenderBlocks renderer) {
        if (block instanceof BlockPhialDisplay display) {
            return renderWorldBlock(world, x, y, z, display, modelId, renderer);
        }
        return false;
    }

    private boolean renderWorldBlock(IBlockAccess world, int x, int y, int z, BlockPhialDisplay block, int modelId,
        RenderBlocks renderer) {
        BlockRenderer.setBrightness(world, x, y, z, block);
        world.getBlockMetadata(x, y, z);
        block.setBlockBounds(bounds1);
        renderer.setRenderBoundsFromBlock(block);
        ScaledRenderHelper.renderStandardBlock(renderer, block, x, y, z);
        block.setBlockBounds(bounds2);
        renderer.setRenderBoundsFromBlock(block);
        // ScaledRenderHelper.renderStandardBlock(renderer, block, x, y, z);
        renderer.clearOverrideBlockTexture();
        block.setBlockBounds(BlockBounds.UNIT);
        renderer.setRenderBoundsFromBlock(block);
        return true;
    }

    @Override
    public boolean shouldRender3DInInventory(int modelId) {
        return false;
    }

    @Override
    public int getRenderId() {
        return renderId;
    }
}
