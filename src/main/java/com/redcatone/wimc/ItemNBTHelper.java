package com.redcatone.wimc;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;

public final class ItemNBTHelper {

    /**
     * Checks if an ItemStack has a Tag Compound
     **/
    public static boolean detectNBT(ItemStack stack) {
        return stack.hasTag();
    }

    /**
     * Tries to initialize an NBT Tag Compound in an ItemStack,
     * this will not do anything if the stack already has a tag
     * compound
     **/
    public static void initNBT(ItemStack stack) {
        if (!detectNBT(stack))
            injectNBT(stack, new CompoundNBT());
    }

    /**
     * Injects an NBT Tag Compound to an ItemStack, no checks
     * are made previously
     **/
    public static void injectNBT(ItemStack stack, CompoundNBT nbt) {
        stack.setTag(nbt);
    }

    /**
     * Gets the CompoundNBT in an ItemStack. Tries to init it
     * previously in case there isn't one present
     **/
    public static CompoundNBT getNBT(ItemStack stack) {
        initNBT(stack);
        return stack.getTag();
    }

    // GETTERS ///////////////////////////////////////////////////////////////////


    public static boolean verifyExistence(ItemStack stack, String tag) {
        return !stack.isEmpty() && detectNBT(stack) && getNBT(stack).contains(tag);
    }

    @Deprecated
    public static boolean verifyExistance(ItemStack stack, String tag) {
        return verifyExistence(stack, tag);
    }

    /**
     * If nullifyOnFail is true it'll return null if it doesn't find any
     * compounds, otherwise it'll return a new one.
     **/
    public static CompoundNBT getCompound(ItemStack stack, String tag, boolean nullifyOnFail) {
        return verifyExistence(stack, tag) ? getNBT(stack).getCompound(tag) : nullifyOnFail ? null : new CompoundNBT();
    }
}
