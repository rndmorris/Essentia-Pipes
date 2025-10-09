package dev.rndmorris.essentiapipes.client;

import javax.annotation.Nullable;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.RenderBlocks;
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
        if (!(block instanceof BlockPipeSegment pipeSegment)) {
            return;
        }
        Bounds.Z_FULL.applyTo(renderer);
        BlockRenderer.drawFaces(renderer, block, pipeSegment.getIcon(0, metadata), false);
        Bounds.VALVE.applyTo(renderer);
        BlockRenderer.drawFaces(renderer, block, pipeSegment.valveIcon[0], false);
    }

    @Override
    public boolean renderWorldBlock(IBlockAccess world, int x, int y, int z, Block block, int modelId,
        RenderBlocks renderer) {
        if (!(block instanceof BlockPipeSegment pipeSegment)) {
            return false;
        }

        // directions to render towards
        final var render = new boolean[ForgeDirection.VALID_DIRECTIONS.length];
        // directions to extend towards
        final var extend = new boolean[ForgeDirection.VALID_DIRECTIONS.length];

        for (var dir : ForgeDirection.VALID_DIRECTIONS) {
            if (!PipeHelper.canConnectVisually(world, x, y, z, dir)) {
                continue;
            }
            render[dir.ordinal()] = true;
            extend[dir.ordinal()] = extendTowards(world, x, y, z, dir);
        }

        var renderedAny = false;

        var bounds = Bounds.getXBounds(render);
        if (bounds != null) {
            renderedAny = true;
            bounds.applyTo(renderer)
                .renderStandardBlock(block, x, y, z);

            if (extend[ForgeDirection.WEST.ordinal()]) {
                Bounds.EXTEND_X_NEG.applyTo(renderer)
                    .renderStandardBlock(block, x - 1, y, z);
            }
            if (extend[ForgeDirection.EAST.ordinal()]) {
                Bounds.EXTEND_X_POS.applyTo(renderer)
                    .renderStandardBlock(block, x + 1, y, z);
            }
        }

        bounds = Bounds.getYBounds(render);
        if (bounds != null) {
            renderedAny = true;
            bounds.applyTo(renderer)
                .renderStandardBlock(block, x, y, z);

            if (extend[ForgeDirection.DOWN.ordinal()]) {
                Bounds.EXTEND_Y_NEG.applyTo(renderer)
                    .renderStandardBlock(block, x, y - 1, z);
            }
            if (extend[ForgeDirection.EAST.ordinal()]) {
                Bounds.EXTEND_Y_POS.applyTo(renderer)
                    .renderStandardBlock(block, x, y + 1, z);
            }
        }

        bounds = Bounds.getZBounds(render);
        if (bounds != null) {
            renderedAny = true;
            bounds.applyTo(renderer)
                .renderStandardBlock(block, x, y, z);

            if (extend[ForgeDirection.NORTH.ordinal()]) {
                Bounds.EXTEND_Z_NEG.applyTo(renderer)
                    .renderStandardBlock(block, x, y - 1, z);
            }
            if (extend[ForgeDirection.SOUTH.ordinal()]) {
                Bounds.EXTEND_Z_POS.applyTo(renderer)
                    .renderStandardBlock(block, x, y + 1, z);
            }
        }

        if (!renderedAny) {
            Bounds.DEFAULT.applyTo(renderer)
                .renderStandardBlock(block, x, y, z);
        }

        final var metadata = world.getBlockMetadata(x, y, z);
        if (BlockPipeSegment.isIOSegment(metadata)) {
            renderer.overrideBlockTexture = pipeSegment.valveIcon[0];
            Bounds.VALVE.applyTo(renderer)
                .renderStandardBlock(block, x, y, z);
            renderer.clearOverrideBlockTexture();
        }

        return true;
    }

    private static boolean extendTowards(IBlockAccess world, int x, int y, int z, ForgeDirection direction) {
        final var adjacentTile = world
            .getTileEntity(x + direction.offsetX, y + direction.offsetY, z + direction.offsetZ);
        if (adjacentTile instanceof IIOPipeSegment) {
            return false;
        }
        return adjacentTile instanceof IEssentiaTransport transport && transport.renderExtendedTube();
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

final class Bounds {

    private static final float MIN = BlockPipeSegment.INSET;
    private static final float MAX = BlockPipeSegment.R_INSET;
    private static final float MIN_FULL = 0;
    private static final float MAX_FULL = 1;
    private static final float MAX_EXTEND = 6F / 16F;
    private static final float MIN_EXTEND = 1 - MAX_EXTEND;

    private static final float MIN_VALVE = BlockPipeSegment.INSET_VALVE;
    private static final float MAX_VALVE = BlockPipeSegment.R_INSET_VALVE;

    public static final Bounds DEFAULT = new Bounds(MIN, MIN, MIN, MAX, MAX, MAX);
    public static final Bounds VALVE = new Bounds(MIN_VALVE, MIN_VALVE, MIN_VALVE, MAX_VALVE, MAX_VALVE, MAX_VALVE);

    public static final Bounds X_FULL = new Bounds(MIN_FULL, MIN, MIN, MAX_FULL, MAX, MAX);
    public static final Bounds X_NEG = new Bounds(MIN_FULL, MIN, MIN, MAX, MAX, MAX);
    public static final Bounds X_POS = new Bounds(MIN, MIN, MIN, MAX_FULL, MAX, MAX);

    public static final Bounds Y_FULL = new Bounds(MIN, MIN_FULL, MIN, MAX, MAX_FULL, MAX);
    public static final Bounds Y_NEG = new Bounds(MIN, MIN_FULL, MIN, MAX, MAX, MAX);
    public static final Bounds Y_POS = new Bounds(MIN, MIN, MIN, MAX, MAX_FULL, MAX);

    public static final Bounds Z_FULL = new Bounds(MIN, MIN, MIN_FULL, MAX, MAX, MAX_FULL);
    public static final Bounds Z_NEG = new Bounds(MIN, MIN, MIN_FULL, MAX, MAX, MAX);
    public static final Bounds Z_POS = new Bounds(MIN, MIN, MIN, MAX, MAX, MAX_FULL);

    public static final Bounds EXTEND_X_NEG = new Bounds(MIN_EXTEND, MIN, MIN, MAX_FULL, MAX, MAX);
    public static final Bounds EXTEND_X_POS = new Bounds(MIN_FULL, MIN, MIN, MAX_EXTEND, MAX, MAX);
    public static final Bounds EXTEND_Y_NEG = new Bounds(MIN, MIN_EXTEND, MIN, MAX, MAX_FULL, MAX);
    public static final Bounds EXTEND_Y_POS = new Bounds(MIN, MIN_FULL, MIN, MAX, MAX_EXTEND, MAX);
    public static final Bounds EXTEND_Z_NEG = new Bounds(MIN, MIN, MIN_EXTEND, MAX, MAX, MAX_FULL);
    public static final Bounds EXTEND_Z_POS = new Bounds(MIN, MIN, MIN_FULL, MAX, MAX, MAX_EXTEND);

    @Nullable
    public static Bounds getXBounds(boolean[] render) {
        final var renderNeg = render[ForgeDirection.WEST.ordinal()];
        final var renderPos = render[ForgeDirection.EAST.ordinal()];
        if (renderNeg && renderPos) {
            return X_FULL;
        }
        if (renderNeg) {
            return X_NEG;
        }
        if (renderPos) {
            return X_POS;
        }
        return null;
    }

    @Nullable
    public static Bounds getYBounds(boolean[] render) {
        final var renderNeg = render[ForgeDirection.DOWN.ordinal()];
        final var renderPos = render[ForgeDirection.UP.ordinal()];

        if (renderNeg && renderPos) {
            return Y_FULL;
        }
        if (renderNeg) {
            return Y_NEG;
        }
        if (renderPos) {
            return Y_POS;
        }
        return null;
    }

    @Nullable
    public static Bounds getZBounds(boolean[] render) {
        final var renderNeg = render[ForgeDirection.NORTH.ordinal()];
        final var renderPos = render[ForgeDirection.SOUTH.ordinal()];
        if (renderNeg && renderPos) {
            return Z_FULL;
        }
        if (renderNeg) {
            return Z_NEG;
        }
        if (renderPos) {
            return Z_POS;
        }
        return null;
    }

    public final float minX;
    public final float minY;
    public final float minZ;
    public final float maxX;
    public final float maxY;
    public final float maxZ;

    private Bounds(float minX, float minY, float minZ, float maxX, float maxY, float maxZ) {
        this.minX = minX;
        this.minY = minY;
        this.minZ = minZ;
        this.maxX = maxX;
        this.maxY = maxY;
        this.maxZ = maxZ;
    }

    public RenderBlocks applyTo(RenderBlocks renderer) {
        renderer.setRenderBounds(minX, minY, minZ, maxX, maxY, maxZ);
        return renderer;
    }
}
