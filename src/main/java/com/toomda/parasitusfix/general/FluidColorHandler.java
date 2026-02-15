package com.toomda.parasitusfix.general;

import buildcraft.lib.fluid.BCFluid;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;

import java.util.HashMap;
import java.util.Map;

public final class FluidColorHandler {
    private static final Map<String, Integer> OVERRIDES = new HashMap<>();

    private FluidColorHandler() {}

    public static int resolveColor(String fluidName, int fallbackArgb) {
        return OVERRIDES.getOrDefault(fluidName, fallbackArgb);
    }

    public static void setOverride(String fluidName, int argb) {
        OVERRIDES.put(fluidName, argb);

        Fluid f = FluidRegistry.getFluid(fluidName);
        if (f instanceof BCFluid) {
            ((BCFluid) f).setColour(argb);
        }
    }
}
