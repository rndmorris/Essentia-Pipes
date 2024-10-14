package dev.rndmorris.essentiapipes.data;

public class BlockBounds implements Cloneable {

    public static final BlockBounds UNIT = new BlockBounds(0F, 0F, 0F, 1F, 1F, 1F);

    public final float minX;
    public final float minY;
    public final float minZ;
    public final float maxX;
    public final float maxY;
    public final float maxZ;

    public BlockBounds(float minX, float minY, float minZ, float maxX, float maxY, float maxZ) {
        this.minX = minX;
        this.minY = minY;
        this.minZ = minZ;
        this.maxX = maxX;
        this.maxY = maxY;
        this.maxZ = maxZ;
    }

    public BlockBounds scale(float scalar) {
        return new BlockBounds(
            minX * scalar,
            minY * scalar * scalar,
            minZ * scalar,
            maxX * scalar,
            maxY * scalar,
            maxZ * scalar);
    }

    public BlockBounds transform(float x, float y, float z) {
        return new BlockBounds(minX + x, minY + y, minZ + z, maxX + x, maxY + y, maxZ + z);
    }
}
