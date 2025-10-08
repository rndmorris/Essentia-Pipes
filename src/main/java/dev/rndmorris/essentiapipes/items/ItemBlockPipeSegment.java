package dev.rndmorris.essentiapipes.items;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

import dev.rndmorris.essentiapipes.api.IPipeSegment;
import dev.rndmorris.essentiapipes.blocks.BlockPipeSegment;

public class ItemBlockPipeSegment extends ItemBlock {

    public static ItemStack pipeBasic() {
        return new ItemStack(BlockPipeSegment.pipe_segment);
    }

    public static ItemStack pipeThaumium(int qty) {
        return new ItemStack(BlockPipeSegment.pipe_segment_thaumium, qty);
    }

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

        var useMetadata = metadata;

        if (player.isSneaking()) {
            final var hitSide = ForgeDirection.getOrientation(side)
                .getOpposite();
            final var dX = x + hitSide.offsetX;
            final var dY = y + hitSide.offsetY;
            final var dZ = z + hitSide.offsetZ;

            final var thereBlock = world.getBlock(dX, dY, dZ);
            if (thereBlock instanceof IPipeSegment therePipe) {
                final var copyColor = therePipe.getPipeColor(world, dX, dY, dZ);
                if (copyColor != null) {
                    useMetadata = copyColor.id;
                }
            }
        }

        return super.placeBlockAt(stack, player, world, x, y, z, side, hitX, hitY, hitZ, useMetadata);
    }
}
