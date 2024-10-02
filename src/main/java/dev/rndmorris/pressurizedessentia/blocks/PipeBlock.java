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

import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import dev.rndmorris.pressurizedessentia.PressurizedEssentia;
import dev.rndmorris.pressurizedessentia.api.IPipeSegment;
import dev.rndmorris.pressurizedessentia.api.PipeColor;
import dev.rndmorris.pressurizedessentia.items.PipeItemBlock;
import dev.rndmorris.pressurizedessentia.tile.PipeConnectorTileEntity;
import thaumcraft.api.wands.IWandable;

public class PipeBlock extends Block implements IPipeSegment, ITileEntityProvider, IWandable {

    public static final String ID = "pipe";
    public static final byte IS_CONNECTOR = 0b1000;
    public static PipeBlock pipe;

    public static void preInit() {
        pipe = new PipeBlock();
        pipe.setBlockName(PressurizedEssentia.modid(ID))
            .setCreativeTab(PressurizedEssentia.proxy.getCreativeTab());

        GameRegistry.registerBlock(pipe, PipeItemBlock.class, ID);
        GameRegistry.registerTileEntity(PipeConnectorTileEntity.class, PipeConnectorTileEntity.ID);
    }

    public static PipeColor pipeColorFromMetadata(int metadata) {
        return PipeColor.fromId((metadata & ~(IS_CONNECTOR)));
    }

    public static boolean isConnector(int metadata) {
        return (metadata & IS_CONNECTOR) == IS_CONNECTOR;
    }

    public final IIcon[] icons = new IIcon[PipeColor.COLORS.length];

    protected PipeBlock() {
        super(Material.iron);
        final var pixel = 1F / 16F;
        final var inset = pixel * 5;
        setBlockBounds(inset, inset, inset, 1F - inset, 1F - inset, 1F - inset);
    }

    @Override
    public boolean canCreatureSpawn(EnumCreatureType type, IBlockAccess world, int x, int y, int z) {
        return false;
    }

    @Override
    public TileEntity createTileEntity(World world, int metadata) {
        return isConnector(metadata) ? new PipeConnectorTileEntity() : null;
    }

    @Override
    public IIcon getIcon(int side, int metadata) {
        return icons[pipeColorFromMetadata(metadata).id];
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
    @SideOnly(Side.CLIENT)
    public void registerBlockIcons(IIconRegister reg) {
        for (var index = 0; index < icons.length; ++index) {
            icons[index] = reg.registerIcon(PressurizedEssentia.modid(ID + "_" + index));
        }
    }

    @Override
    public boolean renderAsNormalBlock() {
        return false;
    }

    ///
    /// IPressurizedPipe
    ///

    public PipeColor getPipeColor(World world, int x, int y, int z) {
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

    public int onWandRightClick(World world, ItemStack wandStack, EntityPlayer player, int x, int y, int z, int side,
        int metadata) {
        final var color = pipeColorFromMetadata(metadata);
        final var connectorBit = isConnector(metadata) ? IS_CONNECTOR : 0;

        final var newColor = player.isSneaking() ? color.prevColor() : color.nextColor();

        world.setBlockMetadataWithNotify(x, y, z, newColor.id | connectorBit, 1 | 2);
        world.markBlockForUpdate(x, y, z);
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
