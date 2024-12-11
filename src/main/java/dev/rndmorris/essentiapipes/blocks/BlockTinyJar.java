package dev.rndmorris.essentiapipes.blocks;

import static dev.rndmorris.essentiapipes.EssentiaPipes.modid;

import java.util.ArrayList;
import java.util.Random;

import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import cpw.mods.fml.common.registry.GameRegistry;
import dev.rndmorris.essentiapipes.EssentiaPipes;
import dev.rndmorris.essentiapipes.data.BlockBounds;
import dev.rndmorris.essentiapipes.items.ItemBlockTinyJar;
import dev.rndmorris.essentiapipes.tile.TileEntityTinyJar;
import thaumcraft.common.blocks.CustomStepSound;

public class BlockTinyJar extends BlockContainer {

    public static final String ID = modid("tiny_jar");

    public static int renderId;

    public static void preInit() {
        GameRegistry.registerBlock(new BlockTinyJar(), ItemBlockTinyJar.class, ID);
        GameRegistry.registerTileEntity(TileEntityTinyJar.class, TileEntityTinyJar.ID);
    }

    public final IIcon[] icon = new IIcon[1];

    public BlockTinyJar() {
        super(Material.glass);
        this.setHardness(.3F);
        this.setStepSound(new CustomStepSound("jar", 1, 1));
        this.setCreativeTab(EssentiaPipes.proxy.getCreativeTab());
        this.setLightLevel(0.66F);
    }

    @Override
    public TileEntity createNewTileEntity(World worldIn, int meta) {
        return new TileEntityTinyJar();
    }

    @Override
    public int getRenderBlockPass() {
        return 1;
    }

    @Override
    public void onPostBlockPlaced(World worldIn, int x, int y, int z, int meta) {
        if (!canBlockStay(worldIn, x, y, z)) {
            worldIn.setBlockToAir(x, y, z);
        }
    }

    @Override
    public int getRenderType() {
        return renderId;
    }

    @Override
    public boolean isOpaqueCube() {
        return false;
    }

    @Override
    public boolean canBlockStay(World worldIn, int x, int y, int z) {
        return worldIn.getBlockMetadata(x, y, z) != 0;
    }

    @Override
    public void updateTick(World worldIn, int x, int y, int z, Random random) {
        if (!canBlockStay(worldIn, x, y, z)) {
            worldIn.setBlockToAir(x, y, z);
        }
    }

    @Override
    public void registerBlockIcons(IIconRegister reg) {
        icon[0] = reg.registerIcon("minecraft:dirt");
    }

    @Override
    public void setBlockBoundsBasedOnState(IBlockAccess worldIn, int x, int y, int z) {
        final var metadata = worldIn.getBlockMetadata(x, y, z);
        final var corners = Position.corners(metadata);

        if (corners.length < 1) {
            Position.NIL.bounds.apply(this);
            return;
        }

        var minX = (float) corners[0].bounds.minX;
        var minY = (float) corners[0].bounds.minY;
        var minZ = (float) corners[0].bounds.minZ;
        var maxX = (float) corners[0].bounds.maxX;
        var maxY = (float) corners[0].bounds.maxY;
        var maxZ = (float) corners[0].bounds.maxZ;

        for (var index = 1; index < corners.length; ++index) {
            final var pos = corners[index];
            minX = Float.min(minX, (float) pos.bounds.minX);
            minY = Float.min(minY, (float) pos.bounds.minY);
            minZ = Float.min(minZ, (float) pos.bounds.minZ);
            maxX = Float.max(maxX, (float) pos.bounds.maxX);
            maxY = Float.max(maxY, (float) pos.bounds.maxY);
            maxZ = Float.max(maxZ, (float) pos.bounds.maxZ);
        }

        final var px = 1F / 16F;
        setBlockBounds(minX - px, minY, minZ - px, maxX + px, maxY + px, maxZ + px);
    }

    public enum Position {

        NIL(-1),
        NW(0b0001),
        NE(0b0010),
        SW(0b0100),
        SE(0b1000),;

        public static final byte bitFlagNW = 0b0001;
        public static final byte bitFlagNE = 0b0010;
        public static final byte bitFlagSW = 0b0100;
        public static final byte bitFlagSE = 0b1000;

        public static Position[] corners() {
            return new Position[] { NW, NE, SW, SE, };
        }

        public static Position[] corners(int metadata) {
            final var result = new ArrayList<Position>();
            for (var corner : corners()) {
                if ((corner.bitFlag & metadata) == corner.bitFlag) {
                    result.add(corner);
                }
            }
            return result.toArray(new Position[0]);
        }

        public final byte bitFlag;
        public final BlockBounds bounds;

        Position(int bitFlag) {
            final var base = BlockBounds.inPixels(0, 0, 0, 5, 6, 5);
            this.bitFlag = (byte) bitFlag;
            final var inset = 2;
            final var rInset = 16 - (5 + inset);
            bounds = switch (bitFlag) {
                case bitFlagNW -> base.shiftPixels(inset, 0, inset);
                case bitFlagNE -> base.shiftPixels(rInset, 0, inset);
                case bitFlagSW -> base.shiftPixels(inset, 0, rInset);
                case bitFlagSE -> base.shiftPixels(rInset, 0, rInset);
                default -> base.shiftPixels(5.5, 0, 5.5);
            };
        }
    }
}
