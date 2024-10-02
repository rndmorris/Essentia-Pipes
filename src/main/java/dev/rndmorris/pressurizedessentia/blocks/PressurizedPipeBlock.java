package dev.rndmorris.pressurizedessentia.blocks;

import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import dev.rndmorris.pressurizedessentia.PressurizedEssentia;
import dev.rndmorris.pressurizedessentia.api.IPressurizedPipe;
import dev.rndmorris.pressurizedessentia.items.PressurizedPipeItemBlock;
import dev.rndmorris.pressurizedessentia.tile.PipeConnectorTileEntity;
import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class PressurizedPipeBlock extends Block implements IPressurizedPipe, ITileEntityProvider {

    public static final String ID = "pressurized_pipe";
    public static final byte IS_CONNECTOR = 0b1000;
    public static final byte TOTAL_COLORS = 7;

    public static PressurizedPipeBlock pressurizedPipe;

    public static void preInit() {
        pressurizedPipe = new PressurizedPipeBlock();
        pressurizedPipe.setBlockName(PressurizedEssentia.modid(ID));

        GameRegistry.registerBlock(pressurizedPipe, PressurizedPipeItemBlock.class, ID);
        GameRegistry.registerTileEntity(PipeConnectorTileEntity.class, PipeConnectorTileEntity.ID);
    }

    public static int clampToColorId(int metadata) {
        return Math.max(0, Math.min(metadata, TOTAL_COLORS));
    }

    public static int getColorId(int metadata) {
        return clampToColorId(metadata & ~(IS_CONNECTOR));
    }

    public static boolean isConnector(int metadata) {
        return (metadata & IS_CONNECTOR) == IS_CONNECTOR;
    }

    public final IIcon[] icons = new IIcon[7];

    protected PressurizedPipeBlock() {
        super(Material.iron);
        final var pixel = 1F / 16F;
        final var inset = pixel * 5;
        setBlockBounds(inset, inset, inset, 1F - inset, 1F - inset, 1F - inset);
    }

    @Override
    public TileEntity createTileEntity(World world, int metadata) {
        return isConnector(metadata) ? new PipeConnectorTileEntity() : null;
    }

    @Override
    public IIcon getIcon(int side, int metadata) {
        return icons[getColorId(metadata)];
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



    ///
    /// ITileEntityProvider
    ///

    @Override
    public TileEntity createNewTileEntity(World worldIn, int meta) {
        return this.createTileEntity(worldIn, meta);
    }
}
