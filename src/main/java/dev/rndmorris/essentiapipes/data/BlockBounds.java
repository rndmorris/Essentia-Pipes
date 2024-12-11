package dev.rndmorris.essentiapipes.data;

import net.minecraft.block.Block;

public class BlockBounds {

    public static final BlockBounds UNIT = new BlockBounds();

    private static double pixel(double pixels) {
        return pixels / 16;
    }

    public static BlockBounds inPixels(int insetX, int insetY, int insetZ) {
        return new BlockBounds(insetX, insetY, insetZ, 1 - insetX, 1 - insetY, 1 - insetZ);
    }

    public static BlockBounds inPixels(int minX, int minY, int minZ, int maxX, int maxY, int maxZ) {
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

    public BlockBounds shift(double shiftX, double shiftY, double shiftZ) {
        return new BlockBounds(
            minX + shiftX,
            minY + shiftY,
            minZ + shiftZ,
            maxX + shiftX,
            maxY + shiftY,
            maxZ + shiftZ);
    }

    public BlockBounds shiftPixels(int shiftX, int shiftY, int shiftZ) {
        return shift(pixel(shiftX), pixel(shiftY), pixel(shiftZ));
    }

    public BlockBounds shiftPixels(double shiftX, double shiftY, double shiftZ) {
        return shift(pixel(shiftX), pixel(shiftY), pixel(shiftZ));
    }

    public <B extends Block> B apply(B block) {
        block.setBlockBounds((float) minX, (float) minY, (float) minZ, (float) maxX, (float) maxY, (float) maxZ);
        return block;
    }
}
