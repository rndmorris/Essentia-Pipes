package dev.rndmorris.pressurizedessentia.client;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;

import dev.rndmorris.pressurizedessentia.EssentiaPipes;
import dev.rndmorris.pressurizedessentia.blocks.BlockPipeSegment;

public class CreativeTab extends CreativeTabs {

    public CreativeTab() {
        super(EssentiaPipes.MODID);
    }

    @Override
    public Item getTabIconItem() {
        return Item.getItemFromBlock(BlockPipeSegment.pipe_segment);
    }
}
