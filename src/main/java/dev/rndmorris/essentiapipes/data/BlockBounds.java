package dev.rndmorris.essentiapipes.data;

import net.minecraft.block.Block;
import net.minecraft.util.AxisAlignedBB;

import thaumcraft.codechicken.lib.vec.Cuboid6;

public class BlockBounds {

    public static final BlockBounds UNIT = new BlockBounds();

    private static double pixel(double pixels) {
        return pixels / 16;
    }

    public static BlockBounds inPixels(double minX, double minY, double minZ, double maxX, double maxY, double maxZ) {
        return new BlockBounds(pixel(minX), pixel(minY), pixel(minZ), pixel(maxX), pixel(maxY), pixel(maxZ));
    }

    public final double minX;
    public final double minY;
    public final double minZ;
    public final double maxX;
    public final double maxY;
    public final double maxZ;

    public BlockBounds() {
        this(0, 0, 0);
    }

    public BlockBounds(double insetX, double insetY, double insetZ) {
        this(insetX, insetY, insetZ, 1 - insetX, 1 - insetY, 1 - insetZ);
    }

    public BlockBounds(double minX, double minY, double minZ, double maxX, double maxY, double maxZ) {
        this.minX = minX;
        this.minY = minY;
        this.minZ = minZ;
        this.maxX = maxX;
        this.maxY = maxY;
        this.maxZ = maxZ;
    }

    public BlockBounds expand(double minX, double minY, double minZ, double maxX, double maxY, double maxZ) {
        return new BlockBounds(
            this.minX - minX,
            this.minY - minY,
            this.minZ - minZ,
            this.maxX + maxX,
            this.maxY + maxY,
            this.maxZ + maxZ);
    }

    public BlockBounds expand(double x, double y, double z) {
        return expand(x, y, z, x, y, z);
    }

    public BlockBounds expandPixels(double minX, double minY, double minZ, double maxX, double maxY, double maxZ) {
        return expand(pixel(minX), pixel(minY), pixel(minZ), pixel(maxX), pixel(maxY), pixel(maxZ));
    }

    public BlockBounds expandPixels(double x, double y, double z) {
        return expand(pixel(x), pixel(y), pixel(z));
    }

    public BlockBounds translate(double shiftX, double shiftY, double shiftZ) {
        return new BlockBounds(
            minX + shiftX,
            minY + shiftY,
            minZ + shiftZ,
            maxX + shiftX,
            maxY + shiftY,
            maxZ + shiftZ);
    }

    public BlockBounds translatePixels(double shiftX, double shiftY, double shiftZ) {
        return translate(pixel(shiftX), pixel(shiftY), pixel(shiftZ));
    }

    public <B extends Block> B apply(B block) {
        block.setBlockBounds((float) minX, (float) minY, (float) minZ, (float) maxX, (float) maxY, (float) maxZ);
        return block;
    }

    public Cuboid6 toCuboid6() {
        return new Cuboid6(minX, minY, minZ, maxX, maxY, maxZ);
    }

    public AxisAlignedBB toAxisAlignedBB() {
        return AxisAlignedBB.getBoundingBox(minX, minY, minZ, maxX, maxY, maxZ);
    }

}
