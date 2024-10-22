package dev.rndmorris.essentiapipes.data;

public class BlockBounds {

    public static final BlockBounds UNIT = new BlockBounds(0F, 0F, 0F, 1F, 1F, 1F);

    public final float diffX;
    public final float diffY;
    public final float diffZ;

    public final float minX;
    public final float minY;
    public final float minZ;
    public final float maxX;
    public final float maxY;
    public final float maxZ;

    public BlockBounds(float maxX, float maxY, float maxZ) {
        this(0, 0, 0, maxX, maxY, maxZ);
    }

    public BlockBounds(float minX, float minY, float minZ, float maxX, float maxY, float maxZ) {
        this.minX = minX;
        this.minY = minY;
        this.minZ = minZ;
        this.maxX = maxX;
        this.maxY = maxY;
        this.maxZ = maxZ;

        diffX = maxX - minX;
        diffY = maxY - minY;
        diffZ = maxZ - minZ;
    }

    public BlockBounds multiply(float scalar) {
        return new BlockBounds(
            minX * scalar,
            minY * scalar,
            minZ * scalar,
            maxX * scalar,
            maxY * scalar,
            maxZ * scalar);
    }

    public BlockBounds expand(float by) {
        return expand(by, by, by); // https://youtu.be/Eo-KmOd3i7s
    }

    public BlockBounds expand(float x, float y, float z) {
        return new BlockBounds(minX - x, minY - y, minZ - z, maxX + x, maxY + y, maxZ + z);
    }

    public BlockBounds transform(float x, float y, float z) {
        return new BlockBounds(minX + x, minY + y, minZ + z, maxX + x, maxY + y, maxZ + z);
    }

    @Override
    public String toString() {
        return "BlockBounds{" + "diffX="
            + diffX
            + ", diffY="
            + diffY
            + ", diffZ="
            + diffZ
            + ", minX="
            + minX
            + ", minY="
            + minY
            + ", minZ="
            + minZ
            + ", maxX="
            + maxX
            + ", maxY="
            + maxY
            + ", maxZ="
            + maxZ
            + '}';
    }
}
