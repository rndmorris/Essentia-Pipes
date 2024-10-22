package dev.rndmorris.essentiapipes.data;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;

import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;

public class StoragePhialSet {

    private final int maxPhials;
    private int phialCount = 0;
    private final StoragePhial[] phials;

    public StoragePhialSet(int maxPhials) {
        this.maxPhials = maxPhials;
        this.phials = new StoragePhial[maxPhials];
    }

    public StoragePhial[] getPhials() {
        return this.phials;
    }

    public boolean canAddPhial() {
        return phialCount < maxPhials;
    }

    public boolean hasPhialAt(int index) {
        if (inBounds(index)) {
            return phials[index] != null;
        }
        return false;
    }

    private boolean inBounds(int index) {
        return 0 <= index && index < phials.length;
    }

    public StoragePhial getPhial(int index) {
        if (inBounds(index)) {
            return phials[index];
        }
        return null;
    }

    public boolean setPhial(int index, StoragePhial newPhial) {
        if (inBounds(index)) {
            phials[index] = newPhial;
            return true;
        }
        return false;
    }

    public boolean addPhial() {
        return addPhial(null, 0);
    }

    public boolean addPhial(Aspect aspect, int amount) {
        for (var index = 0; index < phials.length; ++index) {
            if (phials[index] == null) {
                phials[index] = new StoragePhial(aspect, amount);
                phialCount += 1;
                return true;
            }
        }
        return false;
    }

    public boolean anyPhialNotFull() {
        for (var phial : phials) {
            if (phial == null) {
                continue;
            }
            if (phial.isNotFull()) {
                return true;
            }
        }
        return false;
    }

    public Aspect firstStoredAspect() {
        for (var phial : phials) {
            if (phial == null) {
                continue;
            }
            if (phial.getAmount() > 0) {
                return phial.getAspect();
            }
        }
        return null;
    }

    public List<Aspect> getPreferredAspects() {
        final var result = new ArrayList<Aspect>(4);
        for (var phial : phials) {
            if (phial == null || phial.isFull() || phial.isEmpty()) {
                continue;
            }
            final var aspect = phial.getAspect();
            if (!result.contains(aspect)) {
                result.add(aspect);
            }
        }

        return result;
    }

    public int totalAmountStored() {
        return totalAmountStored(null);
    }

    public int totalAmountStored(@Nullable Aspect aspect) {
        var result = 0;
        for (var phial : phials) {
            if (phial == null) {
                continue;
            }
            if (aspect == null || aspect == phial.getAspect()) {
                result += phial.getAmount();
            }
        }
        return result;
    }

    public AspectList toAspectList() {
        final var result = new AspectList();
        for (var phial : phials) {
            if (phial == null) {
                continue;
            }
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
            if (phial == null) {
                continue;
            }
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
            if (phial == null) {
                continue;
            }
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
        final var tagList = nbtTagCompound.getTagList("PhialAspects", 10);
        for (var index = 0; index < tagList.tagCount(); ++index) {
            final var phial = phials[index];
            final var phialTag = tagList.getCompoundTagAt(index);
            phials[index] = StoragePhial.readFromNBT(phial, phialTag);
        }
    }

    public void writeToNBT(NBTTagCompound nbtTagCompound) {
        var tagList = new NBTTagList();

        for (var phial : phials) {
            final var phialTag = new NBTTagCompound();
            StoragePhial.writeToNbt(phial, phialTag);
            tagList.appendTag(phialTag);
        }

        nbtTagCompound.setTag("PhialAspects", tagList);
    }
}
