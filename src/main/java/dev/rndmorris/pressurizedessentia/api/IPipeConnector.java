package dev.rndmorris.pressurizedessentia.api;

import net.minecraft.world.World;

import dev.rndmorris.pressurizedessentia.Vec;
import thaumcraft.api.aspects.AspectList;

public interface IPipeConnector extends IPipeSegment {

    AspectList wantsEssentia(World world, Vec pos);

}
