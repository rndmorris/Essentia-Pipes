package dev.rndmorris.essentiapipes.items;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public class ItemBlockTinyJar extends ItemBlock {

    public ItemBlockTinyJar(Block p_i45328_1_) {
        super(p_i45328_1_);
        this.setMaxDamage(0);
    }

    @Override
    public int getMetadata(int metadata) {
        return metadata;
    }

    @Override
    public boolean placeBlockAt(ItemStack stack, EntityPlayer player, World world, int x, int y, int z, int side,
        float hitX, float hitY, float hitZ, int metadata) {
        return super.placeBlockAt(stack, player, world, x, y, z, side, hitX, hitY, hitZ, metadata);
    }
}
