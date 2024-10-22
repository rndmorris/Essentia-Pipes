package dev.rndmorris.essentiapipes.blocks;

import java.util.ArrayList;

import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import cpw.mods.fml.common.registry.GameRegistry;
import dev.rndmorris.essentiapipes.EssentiaPipes;
import dev.rndmorris.essentiapipes.client.BlockPhialDisplayRenderer;
import dev.rndmorris.essentiapipes.data.BlockBounds;
import dev.rndmorris.essentiapipes.items.ItemBlockPhialDisplay;
import dev.rndmorris.essentiapipes.tile.TileEntityPhialDisplay;
import thaumcraft.common.blocks.CustomStepSound;

public class BlockPhialDisplay extends Block implements ITileEntityProvider {

    public static final String ID = "phial_display";
    public static final int MAX_METADATA = 4;

    private static BlockPhialDisplay instance;

    public static void preInit() {
        instance = new BlockPhialDisplay();
        instance.setBlockName(EssentiaPipes.modid(ID));
        GameRegistry.registerBlock(instance, ItemBlockPhialDisplay.class, ID);

        GameRegistry.registerTileEntity(TileEntityPhialDisplay.class, TileEntityPhialDisplay.ID);
    }

    public static BlockPhialDisplay getInstance() {
        return instance;
    }

    public static class Icons {

        public IIcon jarBottom;
        public IIcon jarSide;
        public IIcon jarTop;
        public IIcon liquid;
        public IIcon phial;
    }

    public final Icons icons = new Icons();

    public BlockPhialDisplay() {
        super(Material.glass);
        setHardness(0.3F);
        setStepSound(new CustomStepSound("jar", 1.0F, 1.0F));
    }

    @Override
    public void breakBlock(World worldIn, int x, int y, int z, Block blockBroken, int meta) {
        // to-do drop phials
        super.breakBlock(worldIn, x, y, z, blockBroken, meta);
    }

    @Override
    public boolean canPlaceBlockAt(World worldIn, int x, int y, int z) {
        return worldIn.getBlock(x, y, z)
            .isReplaceable(worldIn, x, y, z) && World.doesBlockHaveSolidTopSurface(worldIn, x, y - 1, z);
    }

    @Override
    public TileEntity createNewTileEntity(World worldIn, int meta) {
        return new TileEntityPhialDisplay();
    }

    @Override
    public ArrayList<ItemStack> getDrops(World world, int x, int y, int z, int metadata, int fortune) {
        // to-do: drop phials
        return super.getDrops(world, x, y, z, metadata, fortune);
    }

    @Override
    public IIcon getIcon(int side, int meta) {
        return switch (side) {
            case 0 -> icons.jarBottom;
            case 1 -> icons.jarTop;
            default -> icons.jarSide;
        };
    }

    @Override
    public int getLightValue(IBlockAccess world, int x, int y, int z) {
        final var tile = world.getTileEntity(x, y, z);
        if (tile instanceof TileEntityPhialDisplay display) {
            return display.getLightValue();
        }
        return super.getLightValue(world, x, y, z);
    }

    @Override
    public int getRenderBlockPass() {
        return 1;
    }

    @Override
    public int getRenderType() {
        return BlockPhialDisplayRenderer.renderId;
    }

    @Override
    public void onNeighborBlockChange(World worldIn, int x, int y, int z, Block neighbor) {
        if (!World.doesBlockHaveSolidTopSurface(worldIn, x, y - 1, z)) {
            breakBlock(worldIn, x, y, z, worldIn.getBlock(x, y, z), worldIn.getBlockMetadata(x, y, z));
        }
    }

    @Override
    public boolean isOpaqueCube() {
        return false;
    }

    @Override
    public void registerBlockIcons(IIconRegister reg) {
        icons.liquid = reg.registerIcon("thaumcraft:animatedglow");
        icons.jarBottom = reg.registerIcon("thaumcraft:jar_bottom");
        icons.jarSide = reg.registerIcon("thaumcraft:jar_side");
        icons.jarTop = reg.registerIcon("thaumcraft:jar_top");
        icons.phial = reg.registerIcon("thaumcraft:phial");
    }

    @Override
    public boolean renderAsNormalBlock() {
        return false;
    }

    public void setBlockBounds(BlockBounds b) {
        super.setBlockBounds(b.minX, b.minY, b.minZ, b.maxX, b.maxY, b.maxZ);
    }

    @Override
    public void setBlockBoundsBasedOnState(IBlockAccess worldIn, int x, int y, int z) {
        final var tile = worldIn.getTileEntity(x, y, z);
        if (!(tile instanceof TileEntityPhialDisplay display)) {
            setBlockBounds(BlockBounds.UNIT);
            return;
        }

        final var allPhialBounds = BlockPhialDisplayRenderer.positionedPhialBounds;
        var bound = allPhialBounds[0];

        float minX = bound.minX, minY = bound.minY, minZ = bound.minZ, maxX = bound.maxX, maxY = bound.maxY, maxZ = bound.maxZ;
        for (var index = 1; index < 4; ++index) {
            if (display.getPhialSet()
                .hasPhialAt(index)) {
                bound = allPhialBounds[index];
                minX = Float.min(minX, bound.minX);
                maxX = Float.max(maxX, bound.maxX);
                minZ = Float.min(minZ, bound.minZ);
                maxZ = Float.max(maxZ, bound.maxZ);
            }
        }

        final var pixel = 1F / 16F;
        super.setBlockBounds(minX - pixel, minY, minZ - pixel, maxX + pixel, maxY + pixel, maxZ + pixel);
    }
}
