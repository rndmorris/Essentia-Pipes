package dev.rndmorris.pressurizedessentia.items;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

import dev.rndmorris.pressurizedessentia.api.PipeHelper;
import dev.rndmorris.pressurizedessentia.blocks.BlockPipeSegment;

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
    public String getUnlocalizedName(ItemStack stack) {
        final var color = BlockPipeSegment.pipeColorFromMetadata(stack.getItemDamage());
        return super.getUnlocalizedName() + "." + color.id;
    }

    @Override
    public boolean placeBlockAt(ItemStack stack, EntityPlayer player, World world, int x, int y, int z, int side,
        float hitX, float hitY, float hitZ, int metadata) {

        final var on = ForgeDirection.getOrientation(side)
            .getOpposite();
        final int nX = x + on.offsetX, nY = y + on.offsetY, nZ = z + on.offsetZ;

        if (player.isSneaking()) {
            final var copyColor = PipeHelper.getPipeColor(world, nX, nY, nZ);
            if (copyColor != null) {
                return super.placeBlockAt(stack, player, world, x, y, z, side, hitX, hitY, hitZ, copyColor.id);
            }
        }

        return super.placeBlockAt(stack, player, world, x, y, z, side, hitX, hitY, hitZ, metadata);
    }
}
