package dev.rndmorris.essentiapipes.client;

import static dev.rndmorris.essentiapipes.blocks.BlockPipeSegment.INSET;
import static dev.rndmorris.essentiapipes.blocks.BlockPipeSegment.INSET_VALVE;
import static dev.rndmorris.essentiapipes.blocks.BlockPipeSegment.R_INSET;
import static dev.rndmorris.essentiapipes.blocks.BlockPipeSegment.R_INSET_VALVE;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.common.util.ForgeDirection;

import cpw.mods.fml.client.registry.ISimpleBlockRenderingHandler;
import cpw.mods.fml.client.registry.RenderingRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import dev.rndmorris.essentiapipes.api.IIOPipeSegment;
import dev.rndmorris.essentiapipes.api.PipeHelper;
import dev.rndmorris.essentiapipes.blocks.BlockPipeSegment;
import thaumcraft.api.aspects.IEssentiaTransport;
import thaumcraft.client.renderers.block.BlockRenderer;

@SideOnly(Side.CLIENT)
public class BlockPipeSegmentRenderer extends RenderBlocks implements ISimpleBlockRenderingHandler {

    public static int renderId;
    public static BlockPipeSegmentRenderer instance = new BlockPipeSegmentRenderer();

    public static void init() {
        renderId = RenderingRegistry.getNextAvailableRenderId();
        BlockPipeSegment.renderId = renderId;
        instance = new BlockPipeSegmentRenderer();
        RenderingRegistry.registerBlockHandler(renderId, instance);
    }

    @Override
    public void renderInventoryBlock(Block block, int metadata, int modelId, RenderBlocks renderer) {
        final var icon = block.getIcon(0, metadata);
        block.setBlockBounds(INSET, INSET, 0, R_INSET, R_INSET, 1);
        renderer.setRenderBoundsFromBlock(block);
        BlockRenderer.drawFaces(renderer, block, icon, false);
    }

    @SuppressWarnings("UnnecessaryLocalVariable")
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
            if (!PipeHelper.canConnectVisually(world, x, y, z, dir)) {
                continue;
            }
            final var adjacentTile = world.getTileEntity(x + dir.offsetX, y + dir.offsetY, z + dir.offsetZ);
            final var needsExtension = (!(adjacentTile instanceof IIOPipeSegment)
                && adjacentTile instanceof IEssentiaTransport transport
                && transport.renderExtendedTube());
            switch (dir) {
                case DOWN -> {
                    yAxisMinY = needsExtension ? -BlockRenderer.W6 : 0;
                    renderY = true;
                }
                case UP -> {
                    yAxisMaxY = needsExtension ? 1 + BlockRenderer.W6 : 1;
                    renderY = true;
                }
                case NORTH -> {
                    zAxisMinZ = needsExtension ? -BlockRenderer.W6 : 0;
                    renderZ = true;
                }
                case SOUTH -> {
                    zAxisMaxZ = needsExtension ? 1 + BlockRenderer.W6 : 1;
                    renderZ = true;
                }
                case WEST -> {
                    xAxisMinX = needsExtension ? -BlockRenderer.W6 : 0;
                    renderX = true;
                }
                case EAST -> {
                    xAxisMaxX = needsExtension ? 1 + BlockRenderer.W6 : 1;
                    renderX = true;
                }
            }
        }

        final var metadata = world.getBlockMetadata(x, y, z);

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

        if (!renderedAny) {
            block.setBlockBounds(INSET, INSET, INSET, R_INSET, R_INSET, R_INSET);
            renderer.setRenderBoundsFromBlock(block);
            renderer.renderStandardBlock(block, x, y, z);
        }

        if (BlockPipeSegment.isIOSegment(metadata)) {
            renderer.overrideBlockTexture = pipeSegment.valveIcon[0];
            block.setBlockBounds(INSET_VALVE, INSET_VALVE, INSET_VALVE, R_INSET_VALVE, R_INSET_VALVE, R_INSET_VALVE);
            renderer.setRenderBoundsFromBlock(block);
            renderer.renderStandardBlock(block, x, y, z);
        }

        renderer.field_152631_f = false;

        Tessellator.instance.setColorOpaque_F(1, 1, 1);
        renderer.clearOverrideBlockTexture();
        block.setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F);
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
