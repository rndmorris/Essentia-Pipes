package dev.rndmorris.essentiapipes.data;

import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.TreeSet;

public class StoragePhialSet {
    private final StoragePhial[] phials = new StoragePhial[] {
        new StoragePhial(),
        new StoragePhial(),
        new StoragePhial(),
        new StoragePhial(),
    };

    public AspectList toAspectList() {
        final var result = new AspectList();
        for (var phial : phials) {
            if (phial.amount > 0) {
                result.add(phial.aspect, phial.amount);
            }
        }
        return result;
    }

    /**
     * Add essentia to the phial set
     * @param aspect The aspect to add
     * @param amount The amount to add
     * @return The amount of leftover essentia (anything not added)
     */
    public int addEssentia(@Nonnull Aspect aspect, int amount) {
        final var addToPhials = new TreeSet<StoragePhial>();
        for (var phial : phials) {
            if (phial.canAcceptEssentia() && phial.canTakeAspect(aspect)) {
                addToPhials.add(phial);
            }
        }
        for (var phial : addToPhials.descendingSet()) {
            amount = phial.add(aspect, amount);
        }
        return amount;
    }

    public int takeEssentia(@Nullable Aspect aspect, int amount) {
        final var takeFromPhials = new TreeSet<StoragePhial>();
        for (var phial : phials) {
            if (phial.aspect == null) {
                continue;
            }
        }
    }
}
