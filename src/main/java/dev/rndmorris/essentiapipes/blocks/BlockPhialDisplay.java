package dev.rndmorris.essentiapipes.blocks;

import cpw.mods.fml.common.registry.GameRegistry;
import dev.rndmorris.essentiapipes.EssentiaPipes;
import dev.rndmorris.essentiapipes.tile.TileEntityPhialDisplay;
import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

public class BlockPhialDisplay extends Block implements ITileEntityProvider {

    public static final String ID = "phial_display";

    private static BlockPhialDisplay instance;

    public static void preInit() {
        instance = new BlockPhialDisplay();
        instance.setBlockName(EssentiaPipes.modid(ID));
        GameRegistry.registerBlock(instance, ID);

        GameRegistry.registerTileEntity(TileEntityPhialDisplay.class, TileEntityPhialDisplay.ID);
    }

    public static BlockPhialDisplay getInstance() {
        return instance;
    }

    public BlockPhialDisplay() {
        super(Material.glass);
    }

    @Override
    public TileEntity createNewTileEntity(World worldIn, int meta) {
        return new TileEntityPhialDisplay();
    }

    @Override
    public void breakBlock(World worldIn, int x, int y, int z, Block blockBroken, int meta) {
        // to-do drop phials
        super.breakBlock(worldIn, x, y, z, blockBroken, meta);
    }

    @Override
    public void onNeighborBlockChange(World worldIn, int x, int y, int z, Block neighbor) {
        if (!World.doesBlockHaveSolidTopSurface(worldIn, x, y - 1, z)) {
            breakBlock(worldIn, x, y, z, worldIn.getBlock(x, y, z), worldIn.getBlockMetadata(x, y, z));
        }
    }

    @Override
    public boolean canPlaceBlockAt(World worldIn, int x, int y, int z)
    {
        return worldIn.getBlock(x, y, z).isReplaceable(worldIn, x, y, z) && World.doesBlockHaveSolidTopSurface(worldIn, x, y - 1, z);
    }
}
