package dev.rndmorris.essentiapipes;

import net.minecraft.item.ItemStack;

import dev.rndmorris.essentiapipes.blocks.BlockPipeSegment;
import thaumcraft.api.ItemApi;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.api.crafting.CrucibleRecipe;
import thaumcraft.api.crafting.InfusionRecipe;

public class Recipes {

    public static CrucibleRecipe basicPipeRecipe;
    public static InfusionRecipe thaumiumPipeRecipe;
    public static InfusionRecipe voidmetalPipeRecipe;

    public static void postInit() {
        final var essentiaTube = ItemApi.getBlock("blockTube", 0);
        final var basicPipe = new ItemStack(BlockPipeSegment.pipe_segment, 1);
        basicPipeRecipe = new CrucibleRecipe(
            Research.PIPES_BASIC,
            basicPipe,
            essentiaTube,
            new AspectList().add(Aspect.METAL, 3)
                .add(Aspect.VOID, 3)
                .add(Aspect.MOTION, 2)
                .add(Aspect.SENSES, 1));

        final var twoThaumiumPipes = new ItemStack(BlockPipeSegment.pipe_segment_thaumium, 2);
        final var thaumiumBlock = ItemApi.getBlock("blockCosmeticSolid", 4);
        final var bellows = ItemApi.getBlock("blockWoodenDevice", 0);
        thaumiumPipeRecipe = new InfusionRecipe(
            Research.PIPES_THAUMIUM,
            twoThaumiumPipes,
            5,
            new AspectList().add(Aspect.METAL, 8)
                .add(Aspect.MAGIC, 4)
                .add(Aspect.VOID, 2),
            thaumiumBlock,
            new ItemStack[] { bellows, basicPipe, basicPipe, basicPipe, basicPipe, bellows, basicPipe, basicPipe,
                basicPipe, basicPipe, });

        final var thaumiumPipe = new ItemStack(BlockPipeSegment.pipe_segment_thaumium);
        final var hungryChest = ItemApi.getBlock("blockChestHungry", 0);
        final var voidmetalIngot = ItemApi.getItem("itemResource", 16);
        final var voidmetalPipe = new ItemStack(BlockPipeSegment.pipe_segment_voidmetal, 2);
        voidmetalPipeRecipe = new InfusionRecipe(
            Research.PIPES_VOIDMETAL,
            voidmetalPipe,
            10,
            new AspectList().add(Aspect.METAL, 16)
                .add(Aspect.ELDRITCH, 16)
                .add(Aspect.VOID, 8)
                .add(Aspect.HUNGER, 8),
            hungryChest,
            new ItemStack[] { bellows, thaumiumPipe, voidmetalIngot, thaumiumPipe, voidmetalIngot, thaumiumPipe,
                bellows, thaumiumPipe, voidmetalIngot, thaumiumPipe, voidmetalIngot, thaumiumPipe, });
    }
}
