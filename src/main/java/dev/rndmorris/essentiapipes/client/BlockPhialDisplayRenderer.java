package dev.rndmorris.essentiapipes.client;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.world.IBlockAccess;

import cpw.mods.fml.client.registry.ISimpleBlockRenderingHandler;
import cpw.mods.fml.client.registry.RenderingRegistry;
import thaumcraft.client.renderers.block.BlockRenderer;

public class BlockPhialDisplayRenderer extends BlockRenderer implements ISimpleBlockRenderingHandler {

    public static int renderId;
    public static BlockPhialDisplayRenderer instance = new BlockPhialDisplayRenderer();

    public static void init() {
        renderId = RenderingRegistry.getNextAvailableRenderId();
        instance = new BlockPhialDisplayRenderer();
        RenderingRegistry.registerBlockHandler(renderId, instance);
    }

    @Override
    public void renderInventoryBlock(Block block, int metadata, int modelId, RenderBlocks renderer) {

    }

    @Override
    public boolean renderWorldBlock(IBlockAccess world, int x, int y, int z, Block block, int modelId,
        RenderBlocks renderer) {
        setBrightness(world, x, y, z, block);

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
