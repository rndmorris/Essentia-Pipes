package dev.rndmorris.essentiapipes.blocks;

import static dev.rndmorris.essentiapipes.client.BlockPipeSegmentRenderer.INSET;
import static dev.rndmorris.essentiapipes.client.BlockPipeSegmentRenderer.R_INSET;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.IIcon;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import dev.rndmorris.essentiapipes.Config;
import dev.rndmorris.essentiapipes.EssentiaPipes;
import dev.rndmorris.essentiapipes.api.IIOPipeSegment;
import dev.rndmorris.essentiapipes.api.IPipeSegment;
import dev.rndmorris.essentiapipes.api.PipeColor;
import dev.rndmorris.essentiapipes.api.PipeHelper;
import dev.rndmorris.essentiapipes.api.WorldCoordinate;
import dev.rndmorris.essentiapipes.client.BlockPipeSegmentRenderer;
import dev.rndmorris.essentiapipes.items.ItemBlockPipeSegment;
import dev.rndmorris.essentiapipes.tile.TileEntityIOPipeSegment;
import thaumcraft.api.aspects.IEssentiaTransport;
import thaumcraft.api.wands.IWandable;
import thaumcraft.codechicken.lib.raytracer.IndexedCuboid6;
import thaumcraft.codechicken.lib.raytracer.RayTracer;
import thaumcraft.codechicken.lib.vec.BlockCoord;
import thaumcraft.codechicken.lib.vec.Cuboid6;
import thaumcraft.codechicken.lib.vec.Vector3;

public class BlockPipeSegment extends Block implements IPipeSegment, ITileEntityProvider, IWandable {

    public static final String ID = "pipe_segment";
    public static final String ID_THAUMIUM = "pipe_segment_thaumium";
    public static final String ID_VOIDMETAL = "pipe_segment_voidmetal";
    public static final byte IS_IO_SEGMENT = 0b1000;
    public static BlockPipeSegment pipe_segment;
    public static BlockPipeSegment pipe_segment_thaumium;
    public static BlockPipeSegment pipe_segment_voidmetal;

    public static void preInit() {
        if (Config.pipeEnabledBasic) {
            pipe_segment = register(new BlockPipeSegment(), ID);
        }
        if (Config.pipeEnabledThaumium) {
            pipe_segment_thaumium = register(new BlockPipeSegmentThaumium(), ID_THAUMIUM);
        }
        if (Config.pipeEnabledVoidmetal) {
            pipe_segment_voidmetal = register(new BlockPipeSegmentVoidmetal(), ID_VOIDMETAL);
        }

        GameRegistry.registerTileEntity(TileEntityIOPipeSegment.class, TileEntityIOPipeSegment.ID);
    }

    private static BlockPipeSegment register(BlockPipeSegment instance, String id) {
        instance.setBlockName(EssentiaPipes.modid(id));
        instance.setCreativeTab(EssentiaPipes.proxy.getCreativeTab());

        GameRegistry.registerBlock(instance, ItemBlockPipeSegment.class, id);
        return instance;
    }

    public static PipeColor pipeColorFromMetadata(int metadata) {
        return PipeColor.fromId((metadata & ~(IS_IO_SEGMENT)));
    }

    public static boolean isIOSegment(int metadata) {
        return (metadata & IS_IO_SEGMENT) == IS_IO_SEGMENT;
    }

    /**
     * Check adjacent blocks for IEssentiaTransport instances and change IO state if needed
     *
     * @param world The world to update.
     * @param x     The x of the block to update
     * @param y     The y of the block to update
     * @param z     The z of the block to update
     * @return True if the block became or ceased being an IO block, or false if there was no change.
     */
    public static boolean verifyIOState(World world, int x, int y, int z) {
        final var segment = world.getBlock(x, y, z);

        if (!(segment instanceof BlockPipeSegment)) {
            return false;
        }

        final var metadata = world.getBlockMetadata(x, y, z);
        final var isIOSegment = isIOSegment(metadata);
        final var shouldBeIO = shouldBeIOSegment(world, x, y, z);
        final var pipeColor = pipeColorFromMetadata(metadata);

        if (!isIOSegment && shouldBeIO) {
            world.setBlockMetadataWithNotify(x, y, z, pipeColor.id | IS_IO_SEGMENT, 2);
            return true;
        }
        if (isIOSegment && !shouldBeIO) {
            world.setBlockMetadataWithNotify(x, y, z, pipeColor.id, 2);
            return true;
        }

        return false;
    }

    private static boolean shouldBeIOSegment(World world, int x, int y, int z) {
        for (var dir : ForgeDirection.VALID_DIRECTIONS) {
            final int dX = x + dir.offsetX, dY = y + dir.offsetY, dZ = z + dir.offsetZ;
            final var tileEntity = world.getTileEntity(dX, dY, dZ);
            if (tileEntity instanceof IEssentiaTransport && !(tileEntity instanceof IIOPipeSegment)) {
                return true;
            }
        }
        return false;
    }

    public final IIcon[] icons = new IIcon[PipeColor.COLORS.length];

    public final IIcon[] valveIcon = new IIcon[1];
    private final RayTracer rayTracer = new RayTracer();

    protected BlockPipeSegment() {
        super(Material.iron);
        setHardness(0.5F);
        setResistance(10F);
        setStepSound(Block.soundTypeMetal);
        this.setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F);
    }

    private float[] calcBoundingBox(World world, int x, int y, int z) {
        float minX = BlockPipeSegmentRenderer.INSET_VALVE, maxX = BlockPipeSegmentRenderer.R_INSET_VALVE,
            minY = BlockPipeSegmentRenderer.INSET_VALVE, maxY = BlockPipeSegmentRenderer.R_INSET_VALVE,
            minZ = BlockPipeSegmentRenderer.INSET_VALVE, maxZ = BlockPipeSegmentRenderer.R_INSET_VALVE;
        if (world != null) {
            final var here = new WorldCoordinate(world.provider.dimensionId, x, y, z);
            for (var dir : ForgeDirection.VALID_DIRECTIONS) {
                if (!PipeHelper.canConnect(here, dir)) {
                    continue;
                }
                switch (dir) {
                    case DOWN -> minY = 0;
                    case UP -> maxY = 1;
                    case NORTH -> minZ = 0;
                    case SOUTH -> maxZ = 1;
                    case WEST -> minX = 0;
                    case EAST -> maxX = 1;
                }
            }
        }

        return new float[] { minX, minY, minZ, maxX, maxY, maxZ };
    }

    //
    // Overrides
    //

    @Override
    public void addCollisionBoxesToList(World world, int x, int y, int z, AxisAlignedBB boundingBox,
        List<AxisAlignedBB> list, Entity entity) {
        final var bound = calcBoundingBox(world, x, y, z);
        this.setBlockBounds(bound[0], bound[1], bound[2], bound[3], bound[4], bound[5]);
        super.addCollisionBoxesToList(world, x, y, z, boundingBox, list, entity);
    }

    @Override
    public void breakBlock(World world, int x, int y, int z, Block blockBroken, int meta) {
        super.breakBlock(world, x, y, z, blockBroken, meta);
        PipeHelper.notifySegmentRemoved(world, x, y, z);
    }

    @Override
    public boolean canCreatureSpawn(EnumCreatureType type, IBlockAccess world, int x, int y, int z) {
        return false;
    }

    @Override
    public TileEntity createTileEntity(World world, int metadata) {
        return isIOSegment(metadata) ? new TileEntityIOPipeSegment() : null;
    }

    @Override
    public IIcon getIcon(int side, int metadata) {
        final var color = pipeColorFromMetadata(metadata);
        return icons[color.id];
    }

    @Override
    @SideOnly(Side.CLIENT)
    public IIcon getIcon(IBlockAccess worldIn, int x, int y, int z, int side) {
        final var metadata = worldIn.getBlockMetadata(x, y, z);
        return getIcon(side, metadata);
    }

    @Override
    public int getRenderType() {
        return BlockPipeSegmentRenderer.renderId;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public AxisAlignedBB getSelectedBoundingBoxFromPool(World world, int x, int y, int z) {
        final var bound = calcBoundingBox(world, x, y, z);
        this.setBlockBounds(bound[0], bound[1], bound[2], bound[3], bound[4], bound[5]);
        return super.getSelectedBoundingBoxFromPool(world, x, y, z);
    }

    @Override
    public boolean isOpaqueCube() {
        return false;
    }

    @Override
    public void onBlockAdded(World world, int x, int y, int z) {
        super.onBlockAdded(world, x, y, z);
        verifyIOState(world, x, y, z);
        PipeHelper.notifySegmentAddedOrChanged(world, x, y, z);
    }

    @Override
    public MovingObjectPosition collisionRayTrace(World world, int x, int y, int z, Vec3 start, Vec3 end) {
        final var cuboids = new ArrayList<IndexedCuboid6>(6);

        float min = 0.42F;
        float max = 0.58F;
        if (PipeHelper.canConnectVisually(world, x, y, z, ForgeDirection.DOWN)) {
            cuboids.add(
                new IndexedCuboid6(
                    0,
                    new Cuboid6(
                        ((float) x + min),
                        y,
                        ((float) z + min),
                        ((float) x + max),
                        (double) y + 0.5D,
                        ((float) z + max))));
        }

        if (PipeHelper.canConnectVisually(world, x, y, z, ForgeDirection.UP)) {
            cuboids.add(
                new IndexedCuboid6(
                    1,
                    new Cuboid6(
                        ((float) x + min),
                        (double) y + 0.5D,
                        ((float) z + min),
                        ((float) x + max),
                        (y + 1),
                        ((float) z + max))));
        }

        if (PipeHelper.canConnectVisually(world, x, y, z, ForgeDirection.NORTH)) {
            cuboids.add(
                new IndexedCuboid6(
                    2,
                    new Cuboid6(
                        ((float) x + min),
                        ((float) y + min),
                        z,
                        ((float) x + max),
                        ((float) y + max),
                        (double) z + 0.5D)));
        }

        if (PipeHelper.canConnectVisually(world, x, y, z, ForgeDirection.SOUTH)) {
            cuboids.add(
                new IndexedCuboid6(
                    3,
                    new Cuboid6(
                        ((float) x + min),
                        ((float) y + min),
                        (double) z + 0.5D,
                        ((float) x + max),
                        ((float) y + max),
                        (z + 1))));
        }

        if (PipeHelper.canConnectVisually(world, x, y, z, ForgeDirection.WEST)) {
            cuboids.add(
                new IndexedCuboid6(
                    4,
                    new Cuboid6(
                        x,
                        ((float) y + min),
                        ((float) z + min),
                        (double) x + 0.5D,
                        ((float) y + max),
                        ((float) z + max))));
        }

        if (PipeHelper.canConnectVisually(world, x, y, z, ForgeDirection.EAST)) {
            cuboids.add(
                new IndexedCuboid6(
                    5,
                    new Cuboid6(
                        (double) x + 0.5D,
                        ((float) y + min),
                        ((float) z + min),
                        (x + 1),
                        ((float) y + max),
                        ((float) z + max))));
        }

        cuboids.add(
            new IndexedCuboid6(
                6,
                new Cuboid6(
                    (double) x + 0.34375D,
                    (double) y + 0.34375D,
                    (double) z + 0.34375D,
                    (double) x + 0.65625D,
                    (double) y + 0.65625D,
                    (double) z + 0.65625D)));

        return this.rayTracer
            .rayTraceCuboids(new Vector3(start), new Vector3(end), cuboids, new BlockCoord(x, y, z), this);
    }

    @Override
    public void onNeighborBlockChange(World world, int x, int y, int z, Block neighbor) {
        super.onNeighborBlockChange(world, x, y, z, neighbor);
        if (!(neighbor instanceof IPipeSegment) && verifyIOState(world, x, y, z)) {
            PipeHelper.notifySegmentAddedOrChanged(world, x, y, z);
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void randomDisplayTick(World world, int x, int y, int z, Random random) {
        final var metadata = world.getBlockMetadata(x, y, z);
        if (!(isIOSegment(metadata) && world.isBlockIndirectlyGettingPowered(x, y, z))) {
            return;
        }

        final var posMin = R_INSET - INSET;
        final var pX = x + posMin + random.nextFloat() * R_INSET;
        final var pY = y + posMin + random.nextFloat() * R_INSET;
        final var pZ = z + posMin + random.nextFloat() * R_INSET;

        final var vX = 1;
        final var vY = .2F;
        final var vZ = 0F;

        world.spawnParticle("reddust", pX, pY, pZ, vX, vY, vZ);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void registerBlockIcons(IIconRegister reg) {
        for (var index = 0; index < icons.length; ++index) {
            final var path = String.format("%s:%s_%01d", EssentiaPipes.MODID, getId(), index);
            icons[index] = reg.registerIcon(path);
        }
        valveIcon[0] = reg.registerIcon("thaumcraft:pipe_2");
    }

    protected String getId() {
        return ID;
    }

    @Override
    public boolean renderAsNormalBlock() {
        return false;
    }

    ///
    /// IPressurizedPipe
    ///

    @Override
    public boolean canConnectTo(WorldCoordinate position, ForgeDirection face) {
        final var there = position.shift(face);
        final var neighbor = there.getBlock(IPipeSegment.class);

        return this == neighbor && this.getPipeColor(position)
            .willConnectTo(neighbor.getPipeColor(there));
    }

    @Override
    public boolean canConnectTo(IBlockAccess world, int x, int y, int z, ForgeDirection face) {
        final int dX = x + face.offsetX, dY = y + face.offsetY, dZ = z + face.offsetZ;
        final var adjacentBlock = world.getBlock(dX, dY, dZ);

        return this == adjacentBlock && this.getPipeColor(world, x, y, z)
            .willConnectTo(this.getPipeColor(world, dX, dY, dZ));
    }

    public PipeColor getPipeColor(WorldCoordinate position) {
        final var metadata = position.getBlockMetadata();
        if (metadata >= 0) {
            return pipeColorFromMetadata(metadata);
        }
        return null;
    }

    public PipeColor getPipeColor(IBlockAccess world, int x, int y, int z) {
        final var metadata = world.getBlockMetadata(x, y, z);
        return pipeColorFromMetadata(metadata);
    }

    ///
    /// ITileEntityProvider
    ///

    @Override
    public TileEntity createNewTileEntity(World worldIn, int meta) {
        return this.createTileEntity(worldIn, meta);
    }

    ///
    /// IWandable
    ///

    @Override
    public int onWandRightClick(World world, ItemStack wandStack, EntityPlayer player, int x, int y, int z, int side,
        int metadata) {
        final var color = pipeColorFromMetadata(metadata);
        final var connectorBit = isIOSegment(metadata) ? IS_IO_SEGMENT : 0;

        final var newColor = player.isSneaking() ? color.prevColor() : color.nextColor();

        world.setBlockMetadataWithNotify(x, y, z, newColor.id | connectorBit, 1 | 2);
        PipeHelper.notifySegmentAddedOrChanged(world, x, y, z);
        return 0;
    }

    @Override
    public ItemStack onWandRightClick(World var1, ItemStack var2, EntityPlayer var3) {
        return null;
    }

    @Override
    public void onUsingWandTick(ItemStack var1, EntityPlayer var2, int var3) {

    }

    @Override
    public void onWandStoppedUsing(ItemStack var1, World var2, EntityPlayer var3, int var4) {

    }
}
