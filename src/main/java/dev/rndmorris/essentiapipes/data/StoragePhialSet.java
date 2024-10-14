package dev.rndmorris.essentiapipes.data;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.TreeSet;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;

import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;

public class StoragePhialSet {

    private final int maxPhials;
    private final List<StoragePhial> phials;

    public StoragePhialSet(int maxPhials) {
        this.maxPhials = maxPhials;
        this.phials = new ArrayList<>(maxPhials);
    }

    public void addPhial() {
        if (phials.size() >= maxPhials) {
            return;
        }
        phials.add(new StoragePhial());
    }

    public boolean allPhialsFull() {
        for (var phial : phials) {
            if (phial.isNotFull()) {
                return false;
            }
        }
        return true;
    }

    public Aspect randomStoredAspect(Random rand) {
        final var nonEmptyPhials = new ArrayList<StoragePhial>(maxPhials);
        for (var phial : phials) {
            if (phial.getAmount() > 0) {
                nonEmptyPhials.add(phial);
            }
        }
        return nonEmptyPhials.get(rand.nextInt(nonEmptyPhials.size()))
            .getAspect();
    }

    public int remainingPhialCapacity() {
        return maxPhials - phials.size();
    }

    public int totalAmountStored() {
        return totalAmountStored(null);
    }

    public int totalAmountStored(@Nullable Aspect aspect) {
        var result = 0;
        for (var phial : phials) {
            if (aspect == null || aspect == phial.getAspect()) {
                result += phial.getAmount();
            }
        }
        return result;
    }

    public AspectList toAspectList() {
        final var result = new AspectList();
        for (var phial : phials) {
            if (phial.getAmount() > 0) {
                result.add(phial.getAspect(), phial.getAmount());
            }
        }
        return result;
    }

    public boolean acceptsAspect(Aspect aspect) {
        for (var phial : phials) {
            if (phial.isFull()) {
                continue;
            }
            if (phial.getAspect() == null || phial.getAspect() == aspect) {
                return true;
            }
        }
        return false;
    }

    /**
     * Add essentia to the phial set
     * 
     * @param aspect The aspect to add
     * @param amount The amount to add
     * @return The amount of leftover essentia (anything not added)
     */
    public int addEssentia(@Nonnull Aspect aspect, int amount) {
        final var addToPhials = new TreeSet<StoragePhial>();
        for (var phial : phials) {
            if (phial.isNotFull() && phial.acceptsAspect(aspect)) {
                addToPhials.add(phial);
            }
        }
        for (var phial : addToPhials.descendingSet()) {
            amount = phial.add(aspect, amount);
            if (amount <= 0) {
                break;
            }
        }
        return amount;
    }

    /**
     * Take essenttia from the phial set
     * 
     * @param aspect The aspect to take
     * @param amount The amount to take
     * @return The amount of essentia taken
     */
    public int takeEssentia(@Nullable Aspect aspect, int amount) {
        final var takeFromPhials = new TreeSet<StoragePhial>();
        final var takeAspect = aspect != null ? aspect : lowestStoredAspect();
        for (var phial : phials) {
            if (phial.getAspect() == takeAspect && phial.getAmount() > 0) {
                takeFromPhials.add(phial);
            }
        }
        var taken = 0;
        for (var phial : takeFromPhials) {
            var take = amount - taken;
            taken += phial.take(takeAspect, take);
            if (taken == amount) {
                break;
            }
        }
        return taken;
    }

    private Aspect lowestStoredAspect() {
        Aspect result = null;
        int lowest = Integer.MAX_VALUE;
        for (var aspect : toAspectList().aspects.entrySet()) {
            if (aspect.getValue() < lowest) {
                result = aspect.getKey();
                lowest = aspect.getValue();
            }
        }
        return result;
    }

    public void readFromNBT(NBTTagCompound nbtTagCompound) {
        phials.clear();
        final var tagList = nbtTagCompound.getTagList("PhialAspects", 10);
        for (var index = 0; index < tagList.tagCount() && phials.size() < maxPhials; ++index) {
            final var newPhial = new StoragePhial();
            final var phialTag = tagList.getCompoundTagAt(index);
            newPhial.readFromNBT(phialTag);
            phials.add(newPhial);
        }
    }

    public void writeToNBT(NBTTagCompound nbtTagCompound) {
        var tagList = new NBTTagList();

        for (var phial : phials) {
            final var phialTag = new NBTTagCompound();
            phial.writeToNBT(phialTag);
            tagList.appendTag(phialTag);
        }

        nbtTagCompound.setTag("PhialAspects", tagList);
    }
}
