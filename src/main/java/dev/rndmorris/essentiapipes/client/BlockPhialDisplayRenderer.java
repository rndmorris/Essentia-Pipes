package dev.rndmorris.essentiapipes.client;

import dev.rndmorris.essentiapipes.tile.TileEntityPhialDisplay;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.common.util.ForgeDirection;

import cpw.mods.fml.client.registry.ISimpleBlockRenderingHandler;
import cpw.mods.fml.client.registry.RenderingRegistry;
import dev.rndmorris.essentiapipes.blocks.BlockPhialDisplay;
import dev.rndmorris.essentiapipes.data.BlockBounds;
import thaumcraft.client.renderers.block.BlockRenderer;

public class BlockPhialDisplayRenderer implements ISimpleBlockRenderingHandler {

    public static int renderId;
    public static BlockPhialDisplayRenderer instance;

    public static float pixels(float count) {
        return count / 16F;
    }

    private static final float inset = 2F / 16F;
    public static final BlockBounds phialBounds = new BlockBounds(pixels(10), pixels(12), pixels(10)).multiply(1F / 2F);// .transform(pixels(3),
                                                                                                                        // 0,
                                                                                                                        // pixels(3));
    public static final BlockBounds[] positionedPhialBounds = new BlockBounds[] {
        phialBounds.transform(inset, 0, inset), phialBounds.transform(inset, 0, 1 - phialBounds.maxZ - inset),
        phialBounds.transform(1 - phialBounds.maxX - inset, 0, inset),
        phialBounds.transform(1 - phialBounds.maxX - inset, 0, 1 - phialBounds.maxZ - inset), };

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
        if (block instanceof BlockPhialDisplay display) {
            return renderWorldBlock(world, x, y, z, display, renderer);
        }
        return false;
    }

    private static void trimTextureForGlass(ForgeDirection direction, ScaledRenderHelper.Overrides.Override over) {
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

    private boolean renderWorldBlock(IBlockAccess world, int x, int y, int z, BlockPhialDisplay block,
        RenderBlocks renderer) {
        BlockRenderer.setBrightness(world, x, y, z, block);

        ScaledRenderHelper.UV_OVERRIDES.update(BlockPhialDisplayRenderer::trimTextureForGlass);

        final var tile = (TileEntityPhialDisplay) world.getTileEntity(x, y, z);

        for (var phialIndex = 0; phialIndex < positionedPhialBounds.length; ++phialIndex) {
            if (!tile.getPhialSet().hasPhialAt(phialIndex)) {
                continue;
            }
            block.setBlockBounds(positionedPhialBounds[phialIndex]);
            renderer.setRenderBoundsFromBlock(block);
            ScaledRenderHelper.renderStandardBlock(renderer, block, x, y, z);
        }

        block.setBlockBoundsBasedOnState(world, x, y, z);
        renderer.setRenderBoundsFromBlock(block);
        return true;
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
