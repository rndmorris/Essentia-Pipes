package dev.rndmorris.essentiapipes.items;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

import dev.rndmorris.essentiapipes.api.IPipeSegment;
import dev.rndmorris.essentiapipes.api.WorldCoordinate;

public class ItemBlockPipeSegment extends ItemBlock {

    public ItemBlockPipeSegment(Block p_i45328_1_) {
        super(p_i45328_1_);
        this.setMaxDamage(0);
        this.setHasSubtypes(true);
    }

    @Override
    public int getMetadata(int metadata) {
        return metadata;
    }

    @Override
    public boolean placeBlockAt(ItemStack stack, EntityPlayer player, World world, int x, int y, int z, int side,
        float hitX, float hitY, float hitZ, int metadata) {

        final var here = new WorldCoordinate(world.provider.dimensionId, x, y, z);
        final var there = here.shift(
            ForgeDirection.getOrientation(side)
                .getOpposite());

        if (player.isSneaking()) {
            final var therePipe = there.getBlock(IPipeSegment.class);
            if (therePipe != null) {
                final var copyColor = therePipe.getPipeColor(there);
                if (copyColor != null) {
                    return super.placeBlockAt(stack, player, world, x, y, z, side, hitX, hitY, hitZ, copyColor.id);
                }
            }
        }

        return super.placeBlockAt(stack, player, world, x, y, z, side, hitX, hitY, hitZ, metadata);
    }
}
