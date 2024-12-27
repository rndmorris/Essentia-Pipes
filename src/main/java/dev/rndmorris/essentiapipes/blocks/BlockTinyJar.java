package dev.rndmorris.essentiapipes.blocks;

import static dev.rndmorris.essentiapipes.EssentiaPipes.modid;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.IIcon;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.client.event.DrawBlockHighlightEvent;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import dev.rndmorris.essentiapipes.EssentiaPipes;
import dev.rndmorris.essentiapipes.data.BlockBounds;
import dev.rndmorris.essentiapipes.items.ItemBlockTinyJar;
import dev.rndmorris.essentiapipes.tile.TileEntityTinyJar;
import thaumcraft.codechicken.lib.raytracer.ExtendedMOP;
import thaumcraft.codechicken.lib.raytracer.IndexedCuboid6;
import thaumcraft.codechicken.lib.raytracer.RayTracer;
import thaumcraft.codechicken.lib.vec.BlockCoord;
import thaumcraft.codechicken.lib.vec.Vector3;
import thaumcraft.common.blocks.CustomStepSound;

public class BlockTinyJar extends BlockContainer {

    public static final String ID = modid("tiny_jar");

    public static BlockTinyJar instance;
    public static int renderId;

    public static void preInit() {
        GameRegistry.registerBlock(instance = new BlockTinyJar(), ItemBlockTinyJar.class, ID);
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
    public void onNeighborBlockChange(World worldIn, int x, int y, int z, Block neighbor) {
        if (worldIn.getBlockMetadata(x, y, z) == 0) {
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
    public boolean renderAsNormalBlock() {
        return false;
    }

    @Override
    public void registerBlockIcons(IIconRegister reg) {
        icon[0] = reg.registerIcon("minecraft:dirt");
    }

    @Override
    public void setBlockBoundsBasedOnState(IBlockAccess worldIn, int x, int y, int z) {
        final var metadata = worldIn.getBlockMetadata(x, y, z);
        final var corners = JarPositions.occupiedPlaces(metadata);

        if (corners.isEmpty()) {
            JarPositions.NIL.place.apply(this);
            return;
        }

        var minX = (float) corners.get(0).place.minX;
        var minY = (float) corners.get(0).place.minY;
        var minZ = (float) corners.get(0).place.minZ;
        var maxX = (float) corners.get(0).place.maxX;
        var maxY = (float) corners.get(0).place.maxY;
        var maxZ = (float) corners.get(0).place.maxZ;

        for (var index = 1; index < corners.size(); ++index) {
            final var pos = corners.get(index);
            minX = Float.min(minX, (float) pos.place.minX);
            minY = Float.min(minY, (float) pos.place.minY);
            minZ = Float.min(minZ, (float) pos.place.minZ);
            maxX = Float.max(maxX, (float) pos.place.maxX);
            maxY = Float.max(maxY, (float) pos.place.maxY);
            maxZ = Float.max(maxZ, (float) pos.place.maxZ);
        }

        setBlockBounds(minX, minY, minZ, maxX, maxY, maxZ);
    }

    @Override
    public boolean removedByPlayer(World world, EntityPlayer player, int x, int y, int z, boolean willHarvest) {
        final var doDrops = !player.capabilities.isCreativeMode;
        if (!player.isSneaking()) {
            return removeTinyJars(
                world,
                doDrops,
                x,
                y,
                z,
                JarPositions.allPlaces()
                    .toArray(new JarPositions[0]));
        }

        final var targetJar = getTargetedTinyJar(world, x, y, z, player);
        if (targetJar == null) {
            return false;
        }
        return removeTinyJars(world, doDrops, x, y, z, targetJar);
    }

    private boolean removeTinyJars(World world, boolean dropBlocks, int x, int y, int z, JarPositions... jars) {
        var metadata = world.getBlockMetadata(x, y, z);

        for (var jar : jars) {
            if (!jar.hasFlag(metadata)) {
                continue;
            }
            if (dropBlocks) {
                // to-do: do drops
            }
            // to-do: update tile
            metadata = jar.unsetFlag(metadata);
        }

        if (metadata == 0) {
            return world.setBlockToAir(x, y, z);
        }
        world.setBlockMetadataWithNotify(x, y, z, metadata, 1 & 2);
        return false;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public AxisAlignedBB getSelectedBoundingBoxFromPool(World world, int x, int y, int z) {
        final var player = Minecraft.getMinecraft().thePlayer;
        if (player.isSneaking()) {
            return getTinyJarBB(world, x, y, z);
        }

        final var metadata = world.getBlockMetadata(x, y, z);
        final var corners = JarPositions.occupiedPlaces(metadata);

        if (corners.isEmpty()) {
            return JarPositions.NIL.place.toAxisAlignedBB();
        }

        var minX = (float) corners.get(0).place.minX;
        var minY = (float) corners.get(0).place.minY;
        var minZ = (float) corners.get(0).place.minZ;
        var maxX = (float) corners.get(0).place.maxX;
        var maxY = (float) corners.get(0).place.maxY;
        var maxZ = (float) corners.get(0).place.maxZ;

        for (var index = 1; index < corners.size(); ++index) {
            final var pos = corners.get(index);
            minX = Float.min(minX, (float) pos.place.minX);
            minY = Float.min(minY, (float) pos.place.minY);
            minZ = Float.min(minZ, (float) pos.place.minZ);
            maxX = Float.max(maxX, (float) pos.place.maxX);
            maxY = Float.max(maxY, (float) pos.place.maxY);
            maxZ = Float.max(maxZ, (float) pos.place.maxZ);
        }

        final var px = 1F / 16F;
        return AxisAlignedBB.getBoundingBox(
            (x + minX) - px,
            y + minY,
            (z + minZ) - px,
            (x + maxX) + px,
            (y + maxY) + px,
            (z + maxZ) + px);
    }

    @SideOnly(Side.CLIENT)
    private AxisAlignedBB getTinyJarBB(World world, int x, int y, int z) {
        final var targetedJar = getTargetedTinyJar(world, x, y, z, Minecraft.getMinecraft().thePlayer);

        if (targetedJar != null) {
            return targetedJar.place.translate(x, y, z)
                .expandPixels(1, 0, 1, 1, 1, 1)
                .toAxisAlignedBB();
        }

        return AxisAlignedBB.getBoundingBox(0, 0, 0, 0, 0, 0);
    }

    private JarPositions getTargetedTinyJar(World world, int x, int y, int z, EntityPlayer player) {
        final var metadata = world.getBlockMetadata(x, y, z);
        final var corners = JarPositions.occupiedPlaces(metadata);
        final var cuboids = new ArrayList<IndexedCuboid6>(4);

        for (var index = 0; index < corners.size(); ++index) {
            cuboids.add(
                new IndexedCuboid6(
                    index,
                    corners.get(index).place.translate(x, y, z)
                        .toCuboid6()));
        }
        final var startVec = RayTracer.getStartVec(player);
        final var endVec = RayTracer.getEndVec(player);

        final var hit = RayTracer.instance()
            .rayTraceCuboids(new Vector3(startVec), new Vector3(endVec), cuboids, new BlockCoord(x, y, z), this);
        if (hit instanceof ExtendedMOP mop) {
            return corners.get((int) mop.data);
        }
        return null;
    }

    @Override
    public void addCollisionBoxesToList(World worldIn, int x, int y, int z, AxisAlignedBB mask,
        List<AxisAlignedBB> list, Entity collider) {
        final var metadata = worldIn.getBlockMetadata(x, y, z);
        for (var corner : JarPositions.occupiedPlaces(metadata)) {
            final var cornerBB = corner.place.translate(x, y, z)
                .toAxisAlignedBB();
            if (mask.intersectsWith(cornerBB)) {
                list.add(cornerBB);
            }
        }
    }

    @SideOnly(Side.CLIENT)
    @SubscribeEvent
    public void onDrawBlockHighlight(DrawBlockHighlightEvent event) {
        final var target = event.target;
        if (target.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK
            && event.player.worldObj.getBlock(target.blockX, target.blockY, target.blockZ) == this
            && event.player.isSneaking()) {
            RayTracer.retraceBlock(event.player.worldObj, event.player, target.blockX, target.blockY, target.blockZ);
        }
    }

    @Override
    public MovingObjectPosition collisionRayTrace(World worldIn, int x, int y, int z, Vec3 startVec, Vec3 endVec) {
        final var metadata = worldIn.getBlockMetadata(x, y, z);
        final var corners = JarPositions.allPlaces();
        final var cuboids = new ArrayList<IndexedCuboid6>(4);

        for (var index = 0; index < corners.size(); ++index) {
            final var pos = corners.get(index);
            if (pos.hasFlag(metadata)) {
                cuboids.add(
                    new IndexedCuboid6(
                        index,
                        pos.place.translate(x, y, z)
                            .toCuboid6()));
            }
        }

        return RayTracer.instance()
            .rayTraceCuboids(new Vector3(startVec), new Vector3(endVec), cuboids, new BlockCoord(x, y, z), this);
    }

    public List<JarPositions> emptyJarSpaces(IBlockAccess world, int x, int y, int z) {
        if (!(world.getBlock(x, y, z) instanceof BlockTinyJar)) {
            return new ArrayList<>();
        }
        final var metadata = world.getBlockMetadata(x, y, z);
        return JarPositions.emptyPlaces(metadata);
    }

    public List<JarPositions> occupiedJarSpaces(IBlockAccess world, int x, int y, int z) {
        if (!(world.getBlock(x, y, z) instanceof BlockTinyJar)) {
            return new ArrayList<>();
        }
        final var metadata = world.getBlockMetadata(x, y, z);
        return JarPositions.occupiedPlaces(metadata);
    }

    public enum JarPositions {

        NIL(-1),
        NW(0b0001),
        NE(0b0010),
        SW(0b0100),
        SE(0b1000),;

        public static final byte bitFlagNW = 0b0001;
        public static final byte bitFlagNE = 0b0010;
        public static final byte bitFlagSW = 0b0100;
        public static final byte bitFlagSE = 0b1000;

        public static List<JarPositions> allPlaces() {
            final var result = new ArrayList<JarPositions>(4);
            Collections.addAll(result, NW, NE, SW, SE);
            return result;
        }

        public static List<JarPositions> occupiedPlaces(int metadata) {
            final var result = new ArrayList<JarPositions>();
            for (var corner : allPlaces()) {
                if (corner.hasFlag(metadata)) {
                    result.add(corner);
                }
            }
            return result;
        }

        public static List<JarPositions> emptyPlaces(int metadata) {
            final var result = new ArrayList<JarPositions>();
            for (var corner : allPlaces()) {
                if (!corner.hasFlag(metadata)) {
                    result.add(corner);
                }
            }
            return result;
        }

        public final byte bitFlag;
        public final BlockBounds place;

        JarPositions(int bitFlag) {
            final var base = BlockBounds.inPixels(0, 0, 0, 5, 6, 5);
            this.bitFlag = (byte) bitFlag;
            final var inset = 2;
            final var rInset = 16 - (5 + inset);
            place = switch (bitFlag) {
                case bitFlagNW -> base.translatePixels(inset, 0, inset);
                case bitFlagNE -> base.translatePixels(rInset, 0, inset);
                case bitFlagSW -> base.translatePixels(inset, 0, rInset);
                case bitFlagSE -> base.translatePixels(rInset, 0, rInset);
                default -> base.translatePixels(5.5, 0, 5.5);
            };
        }

        public boolean hasFlag(int metadata) {
            if (bitFlag <= 0) {
                return false;
            }
            return (metadata & bitFlag) == bitFlag;
        }

        public int setFlag(int metadata) {
            if (bitFlag <= 0) {
                return metadata;
            }
            return metadata | bitFlag;
        }

        public int unsetFlag(int metadata) {
            if (bitFlag <= 0) {
                return metadata;
            }
            return metadata & ~(bitFlag);
        }
    }
}
