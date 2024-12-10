package dev.rndmorris.essentiapipes.blocks;

import static dev.rndmorris.essentiapipes.EssentiaPipes.modid;

import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

import cpw.mods.fml.common.registry.GameRegistry;
import dev.rndmorris.essentiapipes.EssentiaPipes;
import dev.rndmorris.essentiapipes.items.ItemBlockTinyJar;
import dev.rndmorris.essentiapipes.tile.TileEntityTinyJar;
import thaumcraft.common.blocks.CustomStepSound;

public class BlockTinyJar extends BlockContainer {

    public static final String ID = modid("tiny_jar");

    public static void preInit() {
        GameRegistry.registerBlock(new BlockTinyJar(), ItemBlockTinyJar.class, ID);
        GameRegistry.registerTileEntity(TileEntityTinyJar.class, TileEntityTinyJar.ID);
    }

    public BlockTinyJar() {
        super(Material.glass);
        this.setHardness(.3F);
        this.setStepSound(new CustomStepSound("jar", 1.0F, 1.0F));
        this.setCreativeTab(EssentiaPipes.proxy.getCreativeTab());
        this.setLightLevel(0.66F);
    }

    @Override
    public TileEntity createTileEntity(World world, int metadata) {
        return new TileEntityTinyJar();
    }

    @Override
    public TileEntity createNewTileEntity(World worldIn, int meta) {
        return createTileEntity(worldIn, meta);
    }
}
