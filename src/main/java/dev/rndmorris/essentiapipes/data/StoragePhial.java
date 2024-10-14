package dev.rndmorris.essentiapipes.data;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.nbt.NBTTagCompound;

import thaumcraft.api.aspects.Aspect;

public class StoragePhial implements Comparable<StoragePhial> {

    public static final byte MAX_AMOUNT = 8;

    private byte amount;
    private Aspect aspect;

    public byte getAmount() {
        return amount;
    }

    public Aspect getAspect() {
        return aspect;
    }

    /**
     * Add essentia to the phial
     * 
     * @param aspect The aspect to add
     * @param amount The amount to add
     * @return The amount of leftover essentia (anything not added)
     */
    public int add(@Nonnull Aspect aspect, int amount) {
        if (this.aspect != null && this.aspect != aspect) {
            return amount;
        }
        this.aspect = aspect;
        final var added = (byte) Math.min(MAX_AMOUNT - this.amount, amount);
        this.amount += added;
        return (byte) (amount - added);
    }

    public boolean acceptsAspect(@Nonnull Aspect aspect) {
        return this.aspect == null || this.aspect == aspect;
    }

    public boolean isFull() {
        return amount >= MAX_AMOUNT;
    }

    public boolean isNotFull() {
        return !isFull();
    }

    /**
     * Take essentia from the phial
     * 
     * @param aspect The aspect to take
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

    public void readFromNBT(NBTTagCompound nbtTagCompound) {
        if (nbtTagCompound.hasKey("tag")) {
            final var tag = nbtTagCompound.getString("tag");
            if (!tag.equalsIgnoreCase("")) {
                aspect = Aspect.getAspect(tag);
            }
        }
        if (nbtTagCompound.hasKey("amount")) {
            amount = nbtTagCompound.getByte("amount");
        }
    }

    public void writeToNBT(NBTTagCompound nbtTagCompound) {
        nbtTagCompound.setString("tag", aspect != null ? aspect.getTag() : "");
        nbtTagCompound.setByte("amount", amount);
    }

    //
    // Comparable<>
    //

    @Override
    public int compareTo(StoragePhial that) {
        return Integer.compare(this.amount, that.amount);
    }
}
