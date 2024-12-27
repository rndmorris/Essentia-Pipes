package dev.rndmorris.essentiapipes.client;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.common.util.ForgeDirection;

import cpw.mods.fml.client.registry.ISimpleBlockRenderingHandler;
import cpw.mods.fml.client.registry.RenderingRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import dev.rndmorris.essentiapipes.api.IIOPipeSegment;
import dev.rndmorris.essentiapipes.api.PipeHelper;
import dev.rndmorris.essentiapipes.blocks.BlockPipeSegment;
import dev.rndmorris.essentiapipes.blocks.BlockTinyJar;
import dev.rndmorris.essentiapipes.data.BlockBounds;
import thaumcraft.api.aspects.IEssentiaTransport;
import thaumcraft.client.renderers.block.BlockRenderer;

@SideOnly(Side.CLIENT)
public class Renderer implements ISimpleBlockRenderingHandler {

    public static Renderer instance = new Renderer();

    public static void init() {
        instance = new Renderer();
        RenderingRegistry
            .registerBlockHandler(BlockPipeSegment.renderId = RenderingRegistry.getNextAvailableRenderId(), instance);
        RenderingRegistry
            .registerBlockHandler(BlockTinyJar.renderId = RenderingRegistry.getNextAvailableRenderId(), instance);
    }

    @Override
    public void renderInventoryBlock(Block block, int metadata, int renderId, RenderBlocks renderer) {
        if (block instanceof BlockPipeSegment blockPipeSegment) {
            renderPipeSegmentInventory(blockPipeSegment, metadata, renderer);
            return;
        }
        if (block instanceof BlockTinyJar blockTinyJar) {
            renderTinyJarInventory(blockTinyJar, renderer);
        }
    }

    public void renderPipeSegmentInventory(BlockPipeSegment block, int metadata, RenderBlocks renderer) {
        final var icon = block.getIcon(0, metadata);
        block.setBlockBounds(INSET, INSET, 0, R_INSET, R_INSET, 1);
        renderer.setRenderBoundsFromBlock(block);
        BlockRenderer.drawFaces(renderer, block, icon, false);
    }

    public void renderTinyJarInventory(BlockTinyJar block, RenderBlocks renderer) {
        BlockTinyJar.JarPositions.NIL.place.apply(block);
        renderer.setRenderBoundsFromBlock(block);
        BlockRenderer.drawFaces(renderer, block, block.icon[0], false);
        BlockBounds.UNIT.apply(block);
    }

    public final static float PIXEL = 1F / 16F;
    public final static float INSET = 6.5F * PIXEL;
    public final static float R_INSET = 1F - INSET;
    public final static float INSET_VALVE = 6 * PIXEL;
    public final static float R_INSET_VALVE = 1F - INSET_VALVE;

    @Override
    public boolean renderWorldBlock(IBlockAccess world, int x, int y, int z, Block block, int modelId,
        RenderBlocks renderer) {
        if (block instanceof BlockPipeSegment pipeSegment) {
            return renderPipeSegment(world, x, y, z, pipeSegment, renderer);
        }
        if (block instanceof BlockTinyJar blockTinyJar) {
            return renderTinyJar(world, x, y, z, blockTinyJar, renderer);
        }
        return false;
    }

    @SuppressWarnings("UnnecessaryLocalVariable")
    public boolean renderPipeSegment(IBlockAccess world, int x, int y, int z, BlockPipeSegment blockPipeSegment,
        RenderBlocks renderer) {
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
            blockPipeSegment.setBlockBounds(xAxisMinX, xAxisMinY, xAxisMinZ, xAxisMaxX, xAxisMaxY, xAxisMaxZ);
            renderer.setRenderBoundsFromBlock(blockPipeSegment);
            renderer.renderStandardBlock(blockPipeSegment, x, y, z);
        }
        if (renderY) {
            renderedAny = true;
            blockPipeSegment.setBlockBounds(yAxisMinX, yAxisMinY, yAxisMinZ, yAxisMaxX, yAxisMaxY, yAxisMaxZ);
            renderer.setRenderBoundsFromBlock(blockPipeSegment);
            renderer.renderStandardBlock(blockPipeSegment, x, y, z);
        }
        if (renderZ) {
            renderedAny = true;
            blockPipeSegment.setBlockBounds(zAxisMinX, zAxisMinY, zAxisMinZ, zAxisMaxX, zAxisMaxY, zAxisMaxZ);
            renderer.setRenderBoundsFromBlock(blockPipeSegment);
            renderer.renderStandardBlock(blockPipeSegment, x, y, z);
        }

        if (!renderedAny) {
            blockPipeSegment.setBlockBounds(INSET, INSET, INSET, R_INSET, R_INSET, R_INSET);
            renderer.setRenderBoundsFromBlock(blockPipeSegment);
            renderer.renderStandardBlock(blockPipeSegment, x, y, z);
        }

        if (BlockPipeSegment.isIOSegment(metadata)) {
            renderer.overrideBlockTexture = blockPipeSegment.valveIcon[0];
            blockPipeSegment
                .setBlockBounds(INSET_VALVE, INSET_VALVE, INSET_VALVE, R_INSET_VALVE, R_INSET_VALVE, R_INSET_VALVE);
            renderer.setRenderBoundsFromBlock(blockPipeSegment);
            renderer.renderStandardBlock(blockPipeSegment, x, y, z);
        }

        renderer.field_152631_f = false;

        Tessellator.instance.setColorOpaque_F(1, 1, 1);
        renderer.clearOverrideBlockTexture();
        blockPipeSegment.setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F);
        renderer.setRenderBoundsFromBlock(blockPipeSegment);
        return true;
    }

    public boolean renderTinyJar(IBlockAccess world, int x, int y, int z, BlockTinyJar blockTinyJar,
        RenderBlocks renderer) {
        // EssentiaPipes.breakMouse();
        final var metadata = world.getBlockMetadata(x, y, z);
        if (metadata == 0) {
            BlockTinyJar.JarPositions.NIL.place.apply(blockTinyJar);
            renderAllFaces(blockTinyJar, x, y, z, blockTinyJar.icon[0], renderer);
            blockTinyJar.setBlockBoundsBasedOnState(world, x, y, z);
            return true;
        }
        for (var pos : BlockTinyJar.JarPositions.occupiedPlaces(metadata)) {
            pos.place.apply(blockTinyJar);
            renderAllFaces(blockTinyJar, x, y, z, blockTinyJar.icon[0], renderer);
        }
        blockTinyJar.setBlockBoundsBasedOnState(world, x, y, z);
        return true;
    }

    @Override
    public boolean shouldRender3DInInventory(int modelId) {
        return true;
    }

    @Override
    public int getRenderId() {
        return -1;
    }

    private void renderAllFaces(Block block, int x, int y, int z, IIcon icon, RenderBlocks renderer) {
        renderer.setRenderBoundsFromBlock(block);
        renderer.renderFaceYPos(block, x, y, z, icon);
        renderer.renderFaceYNeg(block, x, y, z, icon);
        renderer.renderFaceXPos(block, x, y, z, icon);
        renderer.renderFaceXNeg(block, x, y, z, icon);
        renderer.renderFaceZPos(block, x, y, z, icon);
        renderer.renderFaceZNeg(block, x, y, z, icon);
    }

}
