package dev.rndmorris.essentiapipes.data;

import thaumcraft.api.aspects.Aspect;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class StoragePhial implements Comparable<StoragePhial> {
    public static final byte MAX_AMOUNT = 8;

    public Aspect aspect;
    public byte amount;

    /**
     * Add essentia to the phial
     * @param aspect The aspect to add
     * @param amount The amount to add
     * @return The amount of leftover essentia (anything not added)
     */
    public int add(@Nonnull Aspect aspect, int amount) {
        if (this.aspect != null && this.aspect != aspect) {
            return amount;
        }
        final var added = (byte) Math.min(MAX_AMOUNT - this.amount, amount);
        this.amount += added;
        return (byte)(amount - added);
    }

    public boolean canTakeAspect(@Nonnull Aspect aspect) {
        return this.aspect == null || this.aspect == aspect;
    }

    public boolean canAcceptEssentia() {
        return (MAX_AMOUNT - amount) > 0;
    }

    /**
     * Take essentia from the phial
     * @param amount The amount to take
     * @return The amount of essentia taken
     */
    public int take(@Nullable Aspect aspect, int amount) {
        if (aspect != null && this.aspect != aspect) {
            return 0;
        }
        final var taken = (byte) Math.min(this.amount, amount);
        this.amount -= taken;
        if (this.amount <= 0) {
            this.amount = 0;
            this.aspect = null;
        }
        return taken;
    }

    //
    // Comparable<>
    //

    @Override
    public int compareTo(StoragePhial that) {
        return Integer.compare(this.amount, that.amount);
    }
}
