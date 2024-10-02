package dev.rndmorris.pressurizedessentia.items;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

import dev.rndmorris.pressurizedessentia.Vec;
import dev.rndmorris.pressurizedessentia.api.IPressurizedPipe;

public class PressurizedPipeItemBlock extends ItemBlock {

    public PressurizedPipeItemBlock(Block p_i45328_1_) {
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

        final var placeAt = new Vec(x, y, z);
        final var placeOn = placeAt.neighbor(
            ForgeDirection.getOrientation(side)
                .getOpposite());

        if (player.isSneaking() && tryGetPipe(world, placeOn) instanceof IPressurizedPipe pipe) {
            final var copiedColor = pipe.getPipeColor(world, placeOn.x(), placeOn.y(), placeOn.z());
            return super.placeBlockAt(stack, player, world, x, y, z, side, hitX, hitY, hitZ, copiedColor.id);
        }

        return super.placeBlockAt(stack, player, world, x, y, z, side, hitX, hitY, hitZ, metadata);
    }

    private Object tryGetPipe(IBlockAccess world, Vec atPos) {
        final var block = world.getBlock(atPos.x(), atPos.y(), atPos.z());
        return block instanceof IPressurizedPipe pipe ? pipe : null;
    }
}
