package dev.rndmorris.pressurizedessentia.blocks;

import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import dev.rndmorris.pressurizedessentia.PressurizedEssentia;
import dev.rndmorris.pressurizedessentia.api.IPipeSegment;
import dev.rndmorris.pressurizedessentia.api.PipeColor;
import dev.rndmorris.pressurizedessentia.api.PipeHelper;
import dev.rndmorris.pressurizedessentia.items.ItemBlockPipeSegment;
import dev.rndmorris.pressurizedessentia.tile.TileEntityIOPipeSegment;
import thaumcraft.api.aspects.IEssentiaTransport;
import thaumcraft.api.wands.IWandable;

public class BlockPipeSegment extends Block implements IPipeSegment, ITileEntityProvider, IWandable {

    public static final String ID = "pipe_segment";
    public static final byte IS_IO_SEGMENT = 0b1000;
    public static BlockPipeSegment pipe_segment;

    public static void preInit() {
        pipe_segment = new BlockPipeSegment();
        pipe_segment.setBlockName(PressurizedEssentia.modid(ID))
            .setCreativeTab(PressurizedEssentia.proxy.getCreativeTab());

        GameRegistry.registerBlock(pipe_segment, ItemBlockPipeSegment.class, ID);
        GameRegistry.registerTileEntity(TileEntityIOPipeSegment.class, TileEntityIOPipeSegment.ID);
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

        if (segment != pipe_segment) {
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
            if (tileEntity instanceof IEssentiaTransport && !(tileEntity instanceof IPipeSegment)) {
                return true;
            }
        }
        return false;
    }

    public final IIcon[] icons = new IIcon[PipeColor.COLORS.length * 2];

    protected BlockPipeSegment() {
        super(Material.iron);
        final var pixel = 1F / 16F;
        final var inset = pixel * 6;
        setBlockBounds(inset, inset, inset, 1F - inset, 1F - inset, 1F - inset);
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
        return icons[metadata];
    }

    @Override
    @SideOnly(Side.CLIENT)
    public IIcon getIcon(IBlockAccess worldIn, int x, int y, int z, int side) {
        final var metadata = worldIn.getBlockMetadata(x, y, z);
        return getIcon(side, metadata);
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
    public void onNeighborBlockChange(World world, int x, int y, int z, Block neighbor) {
        super.onNeighborBlockChange(world, x, y, z, neighbor);
        if (!(neighbor instanceof IPipeSegment) && verifyIOState(world, x, y, z)) {
            PipeHelper.notifySegmentAddedOrChanged(world, x, y, z);
        }
    }

    ///
    /// IPressurizedPipe
    ///

    public PipeColor getPipeColor(World world, int x, int y, int z) {
        final var metadata = world.getBlockMetadata(x, y, z);
        return pipeColorFromMetadata(metadata);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void registerBlockIcons(IIconRegister reg) {
        for (var index = 0; index < icons.length; ++index) {
            final var path = String.format("%s:%s_%02d", PressurizedEssentia.MODID, ID, index);
            icons[index] = reg.registerIcon(path);
        }
    }

    @Override
    public boolean renderAsNormalBlock() {
        return false;
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

    public int onWandRightClick(World world, ItemStack wandStack, EntityPlayer player, int x, int y, int z, int side,
        int metadata) {
        final var color = pipeColorFromMetadata(metadata);
        final var connectorBit = isIOSegment(metadata) ? IS_IO_SEGMENT : 0;

        final var newColor = player.isSneaking() ? color.prevColor() : color.nextColor();

        world.setBlockMetadataWithNotify(x, y, z, newColor.id | connectorBit, 1 | 2);
        PipeHelper.notifySegmentAddedOrChanged(world, x, y, z);
        return 0;
    }

    public ItemStack onWandRightClick(World var1, ItemStack var2, EntityPlayer var3) {
        return null;
    }

    public void onUsingWandTick(ItemStack var1, EntityPlayer var2, int var3) {

    }

    public void onWandStoppedUsing(ItemStack var1, World var2, EntityPlayer var3, int var4) {

    }
}
