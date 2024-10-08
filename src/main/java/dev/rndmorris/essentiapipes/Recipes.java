package dev.rndmorris.essentiapipes;

import dev.rndmorris.essentiapipes.blocks.BlockPipeSegment;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import thaumcraft.api.ItemApi;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.api.crafting.CrucibleRecipe;

public class Recipes {

    public static CrucibleRecipe basicPipeRecipe;

    public static void postInit() {
        basicPipeRecipe = new CrucibleRecipe(Research.PIPES_BASIC, new ItemStack(Item.getItemFromBlock(BlockPipeSegment.pipe_segment)), ItemApi.getBlock("blockTube", 0), new AspectList().add(Aspect.METAL, 3).add(Aspect.VOID, 3).add(Aspect.MOTION, 2).add(Aspect.SENSES, 1));
    }
}
