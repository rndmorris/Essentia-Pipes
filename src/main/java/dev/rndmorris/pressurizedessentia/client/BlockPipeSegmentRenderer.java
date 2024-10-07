package dev.rndmorris.pressurizedessentia.client;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.common.util.ForgeDirection;

import cpw.mods.fml.client.registry.ISimpleBlockRenderingHandler;
import cpw.mods.fml.client.registry.RenderingRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import dev.rndmorris.pressurizedessentia.api.PipeHelper;
import dev.rndmorris.pressurizedessentia.blocks.BlockPipeSegment;
import thaumcraft.client.renderers.block.BlockRenderer;

@SideOnly(Side.CLIENT)
public class BlockPipeSegmentRenderer extends RenderBlocks implements ISimpleBlockRenderingHandler {

    public static int renderId;
    public static BlockPipeSegmentRenderer instance = new BlockPipeSegmentRenderer();

    public static void init() {
        renderId = RenderingRegistry.getNextAvailableRenderId();
        instance = new BlockPipeSegmentRenderer();
        RenderingRegistry.registerBlockHandler(renderId, instance);
    }

    @Override
    public void renderInventoryBlock(Block block, int metadata, int modelId, RenderBlocks renderer) {
        final var icon = block.getIcon(0, metadata);
        BlockRenderer.drawFaces(renderer, block, icon, false);
    }

    public final static float PIXEL = 1F / 16F;
    public final static float INSET = 6 * PIXEL;
    public final static float R_INSET = 1F - INSET;

    @Override
    public boolean renderWorldBlock(IBlockAccess world, int x, int y, int z, Block block, int modelId,
        RenderBlocks renderer) {
        if (!(block instanceof BlockPipeSegment pipeSegment)) {
            return false;
        }

        renderer.field_152631_f = true;
        boolean renderX = false, renderY = false, renderZ = false;

        float xAxisMinX = INSET, xAxisMaxX = R_INSET, xAxisMinY = INSET, xAxisMaxY = R_INSET, xAxisMinZ = INSET,
            xAxisMaxZ = R_INSET;

        float yAxisMinX = INSET, yAxisMaxX = R_INSET, yAxisMinY = INSET, yAxisMaxY = R_INSET, yAxisMinZ = INSET,
            yAxisMaxZ = R_INSET;

        float zAxisMinX = INSET, zAxisMaxX = R_INSET, zAxisMinY = INSET, zAxisMaxY = R_INSET, zAxisMinZ = INSET,
            zAxisMaxZ = R_INSET;

        for (var dir : ForgeDirection.VALID_DIRECTIONS) {
            if (!PipeHelper.shouldVisuallyConnect(world, x, y, z, dir)) {
                continue;
            }
            switch (dir) {
                case DOWN -> {
                    yAxisMinY = 0;
                    renderY = true;
                }
                case UP -> {
                    yAxisMaxY = 1;
                    renderY = true;
                }
                case NORTH -> {
                    zAxisMinZ = 0;
                    renderZ = true;
                }
                case SOUTH -> {
                    zAxisMaxZ = 1;
                    renderZ = true;
                }
                case WEST -> {
                    xAxisMinX = 0;
                    renderX = true;
                }
                case EAST -> {
                    xAxisMaxX = 1;
                    renderX = true;
                }
            }
        }

        var renderedAny = false;
        if (renderX) {
            renderedAny = true;
            block.setBlockBounds(xAxisMinX, xAxisMinY, xAxisMinZ, xAxisMaxX, xAxisMaxY, xAxisMaxZ);
            renderer.setRenderBoundsFromBlock(block);
            renderer.renderStandardBlock(block, x, y, z);
        }
        if (renderY) {
            renderedAny = true;
            block.setBlockBounds(yAxisMinX, yAxisMinY, yAxisMinZ, yAxisMaxX, yAxisMaxY, yAxisMaxZ);
            renderer.setRenderBoundsFromBlock(block);
            renderer.renderStandardBlock(block, x, y, z);
        }
        if (renderZ) {
            renderedAny = true;
            block.setBlockBounds(zAxisMinX, zAxisMinY, zAxisMinZ, zAxisMaxX, zAxisMaxY, zAxisMaxZ);
            renderer.setRenderBoundsFromBlock(block);
            renderer.renderStandardBlock(block, x, y, z);
        }

        final var metadata = world.getBlockMetadata(x, y, z);

        if (!renderedAny) {
            renderer.overrideBlockTexture = pipeSegment.icons[metadata];
            block.setBlockBounds(INSET, INSET, INSET, R_INSET, R_INSET, R_INSET);
            renderer.setRenderBoundsFromBlock(block);
            renderer.renderStandardBlock(block, x, y, z);
        }

        renderer.field_152631_f = false;

        renderer.clearOverrideBlockTexture();
        block.setBlockBounds(INSET, INSET, INSET, R_INSET, R_INSET, R_INSET);
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
