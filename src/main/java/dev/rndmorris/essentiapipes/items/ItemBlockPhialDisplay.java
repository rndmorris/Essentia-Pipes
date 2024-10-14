package dev.rndmorris.essentiapipes.items;

import net.minecraft.block.Block;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;

import dev.rndmorris.essentiapipes.blocks.BlockPhialDisplay;

public class ItemBlockPhialDisplay extends ItemBlock {

    public ItemBlockPhialDisplay(Block p_i45328_1_) {
        super(p_i45328_1_);
        this.setMaxDamage(0);
    }

    @Override
    public IIcon getIcon(ItemStack stack, int pass) {
        var block = Block.getBlockFromItem(stack.getItem());
        if (block instanceof BlockPhialDisplay display) {
            return display.icons.phial;
        }
        return super.getIcon(stack, pass);
    }
}
