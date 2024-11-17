package dev.rndmorris.essentiapipes;

import net.minecraft.item.ItemStack;

import dev.rndmorris.essentiapipes.blocks.BlockPipeSegment;
import thaumcraft.api.ItemApi;
import thaumcraft.api.ThaumcraftApi;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.api.crafting.CrucibleRecipe;

public class Recipes {

    public static CrucibleRecipe basicPipeRecipe;
    public static CrucibleRecipe thaumiumPipeRecipe;
    public static CrucibleRecipe voidmetalPipeRecipe;

    public static void postInit() {
        ItemStack previousPipe = ItemApi.getBlock("blockTube", 0);

        if (Config.pipeEnabledBasic) {
            final var basicPipe = new ItemStack(BlockPipeSegment.pipe_segment, 1);
            basicPipeRecipe = ThaumcraftApi.addCrucibleRecipe(
                Research.PIPES_BASIC,
                basicPipe,
                previousPipe,
                new AspectList().add(Aspect.METAL, 1)
                    .add(Aspect.VOID, 1));
            previousPipe = basicPipe;
        }

        if (Config.pipeEnabledThaumium) {
            final var thaumiumPipe = new ItemStack(BlockPipeSegment.pipe_segment_thaumium, 1);
            thaumiumPipeRecipe = ThaumcraftApi.addCrucibleRecipe(
                Research.PIPES_THAUMIUM,
                thaumiumPipe,
                previousPipe,
                new AspectList().add(Aspect.MAGIC, 1)
                    .add(Aspect.METAL, 1)
                    .add(Aspect.VOID, 1));
            previousPipe = thaumiumPipe;
        }

        if (Config.pipeEnabledVoidmetal) {
            final var voidmetalPipe = new ItemStack(BlockPipeSegment.pipe_segment_voidmetal, 1);
            voidmetalPipeRecipe = ThaumcraftApi.addCrucibleRecipe(
                Research.PIPES_THAUMIUM,
                voidmetalPipe,
                previousPipe,
                new AspectList().add(Aspect.ELDRITCH, 1)
                    .add(Aspect.METAL, 1)
                    .add(Aspect.VOID, 1));
            previousPipe = voidmetalPipe;
        }
    }
}
