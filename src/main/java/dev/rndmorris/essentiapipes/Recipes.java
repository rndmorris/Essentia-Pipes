package dev.rndmorris.essentiapipes;

import net.minecraft.item.ItemStack;

import dev.rndmorris.essentiapipes.blocks.BlockPipeSegment;
import thaumcraft.api.ItemApi;
import thaumcraft.api.ThaumcraftApi;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.api.crafting.CrucibleRecipe;
import thaumcraft.api.crafting.InfusionRecipe;

public class Recipes {

    public static CrucibleRecipe basicPipeRecipe;
    public static InfusionRecipe thaumiumPipeRecipe;
    public static InfusionRecipe voidmetalPipeRecipe;

    public static void postInit() {
        ItemStack previousPipe = ItemApi.getBlock("blockTube", 0);
        final var thaumiumBlock = ItemApi.getBlock("blockCosmeticSolid", 4);
        final var bellows = ItemApi.getBlock("blockWoodenDevice", 0);

        if (Config.pipeEnabledBasic) {
            final var basicPipe = new ItemStack(BlockPipeSegment.pipe_segment, 1);
            basicPipeRecipe = ThaumcraftApi.addCrucibleRecipe(
                Research.PIPES_BASIC,
                basicPipe,
                previousPipe,
                new AspectList().add(Aspect.METAL, 3)
                    .add(Aspect.VOID, 3)
                    .add(Aspect.MOTION, 2)
                    .add(Aspect.SENSES, 1));
            previousPipe = basicPipe;
        }

        if (Config.pipeEnabledThaumium) {
            final var twoThaumiumPipes = new ItemStack(BlockPipeSegment.pipe_segment_thaumium, 2);
            thaumiumPipeRecipe = ThaumcraftApi.addInfusionCraftingRecipe(
                Research.PIPES_THAUMIUM,
                twoThaumiumPipes,
                3,
                new AspectList().add(Aspect.METAL, 8)
                    .add(Aspect.MAGIC, 4)
                    .add(Aspect.VOID, 2),
                thaumiumBlock,
                new ItemStack[] { bellows, previousPipe, previousPipe, previousPipe, previousPipe, bellows,
                    previousPipe, previousPipe, previousPipe, previousPipe, });
            previousPipe = new ItemStack(BlockPipeSegment.pipe_segment_thaumium, 1);
        }

        if (Config.pipeEnabledVoidmetal) {
            final var hungryChest = ItemApi.getBlock("blockChestHungry", 0);
            final var voidmetalIngot = ItemApi.getItem("itemResource", 16);
            final var twoVoidmetalPipes = new ItemStack(BlockPipeSegment.pipe_segment_voidmetal, 2);
            voidmetalPipeRecipe = ThaumcraftApi.addInfusionCraftingRecipe(
                Research.PIPES_VOIDMETAL,
                twoVoidmetalPipes,
                7,
                new AspectList().add(Aspect.METAL, 16)
                    .add(Aspect.ELDRITCH, 16)
                    .add(Aspect.VOID, 8)
                    .add(Aspect.HUNGER, 8),
                hungryChest,
                new ItemStack[] { bellows, previousPipe, voidmetalIngot, previousPipe, voidmetalIngot, previousPipe,
                    bellows, previousPipe, voidmetalIngot, previousPipe, voidmetalIngot, previousPipe, });
        }
    }
}
