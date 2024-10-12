package dev.rndmorris.essentiapipes;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

import dev.rndmorris.essentiapipes.blocks.BlockPipeSegment;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.api.research.ResearchCategories;
import thaumcraft.api.research.ResearchItem;
import thaumcraft.api.research.ResearchPage;

public class Research {

    public final static String CATEGORY = EssentiaPipes.modid("essentia_pipes");

    public static final String PIPES_BASIC = EssentiaPipes.modid("PIPES_BASIC");
    public static final String PIPES_THAUMIUM = EssentiaPipes.modid("PIPES_THAUMIUM");
    public static final String PIPES_VOIDMETAL = EssentiaPipes.modid("PIPES_VOIDMETAL");

    public static ResearchItem basicPipes;
    public static ResearchItem thaumiumPipes;
    public static ResearchItem voidmetalPipes;

    public static void postInit() {
        ResearchCategories.registerCategory(
            CATEGORY,
            new ResourceLocation("thaumcraft", "textures/blocks/alchemyblock.png"),
            new ResourceLocation("thaumcraft", "textures/gui/gui_researchback.png"));

        var previousResearch = "THAUMATORIUM";

        if (Config.pipeEnabledBasic) {
            basicPipes = new ResearchItem(
                PIPES_BASIC,
                CATEGORY,
                new AspectList().add(Aspect.METAL, 5)
                    .add(Aspect.MOTION, 3)
                    .add(Aspect.VOID, 1),
                0,
                -2,
                1,
                new ItemStack(Item.getItemFromBlock(BlockPipeSegment.pipe_segment)))
                .setPages(
                    new ResearchPage("tc.research_page.essentiapipes:PIPES_BASIC.1"),
                    new ResearchPage(Recipes.basicPipeRecipe),
                    new ResearchPage("tc.research_page.essentiapipes:PIPES_BASIC.2"),
                    new ResearchPage("tc.research_page.essentiapipes:PIPES_BASIC.3"),
                    new ResearchPage("tc.research_page.essentiapipes:PIPES_BASIC.4"),
                    new ResearchPage("tc.research_page.essentiapipes:PIPES_BASIC.5"),
                    new ResearchPage("tc.research_page.essentiapipes:PIPES_BASIC.6"),
                    new ResearchPage("tc.research_page.essentiapipes:PIPES_BASIC.7"))
                .setParentsHidden(previousResearch)
                .setAutoUnlock()
                .registerResearchItem();
            previousResearch = PIPES_BASIC;
        }

        if (Config.pipeEnabledThaumium) {
            thaumiumPipes = new ResearchItem(
                PIPES_THAUMIUM,
                CATEGORY,
                new AspectList().add(Aspect.METAL, 7)
                    .add(Aspect.MAGIC, 5)
                    .add(Aspect.VOID, 3),
                0,
                0,
                2,
                new ItemStack(Item.getItemFromBlock(BlockPipeSegment.pipe_segment_thaumium)))
                .setPages(
                    new ResearchPage("tc.research_page.essentiapipes:PIPES_THAUMIUM.1"),
                    new ResearchPage(Recipes.thaumiumPipeRecipe))
                .setParents(previousResearch, "ARMORFORTRESS")
                .setAutoUnlock()
                .registerResearchItem();
            previousResearch = PIPES_THAUMIUM;
        }

        if (Config.pipeEnabledVoidmetal) {
            voidmetalPipes = new ResearchItem(
                PIPES_VOIDMETAL,
                CATEGORY,
                new AspectList().add(Aspect.METAL, 9)
                    .add(Aspect.ELDRITCH, 7)
                    .add(Aspect.VOID, 5),
                0,
                2,
                3,
                new ItemStack(Item.getItemFromBlock(BlockPipeSegment.pipe_segment_voidmetal)))
                .setPages(
                    new ResearchPage("tc.research_page.essentiapipes:PIPES_VOIDMETAL.1"),
                    new ResearchPage(Recipes.voidmetalPipeRecipe),
                    new ResearchPage("tc.research_page.essentiapipes:PIPES_VOIDMETAL.2"))
                .setAutoUnlock()
                .setParents(previousResearch, "ESSENTIARESERVOIR", "HUNGRYCHEST")
                .registerResearchItem();
        }
    }

}
