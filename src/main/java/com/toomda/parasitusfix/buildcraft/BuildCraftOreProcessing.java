package com.toomda.parasitusfix.buildcraft;

import buildcraft.api.recipes.BuildcraftRecipeRegistry;
import buildcraft.api.recipes.IRefineryRecipeManager;
import com.toomda.parasitusfix.ParasitusFix;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.oredict.OreDictionary;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

public final class BuildCraftOreProcessing {
    private static final String BCOP_MODID = "bcoreprocessing";
    private static final String RECIPES_CLASS = "net.ndrei.bcoreprocessing.api.recipes.OreProcessingRecipes";
    private static final String MOLTEN_PREFIX = "molten_";
    private static final String GASEOUS_LAVA_SEARING = "bcop-gaseous_lava-searing";

    private static final int BATCH_MB = 1000;
    private static final int PROCESS_TICKS = 40;
    private static final int RESIDUE_SEARING = 50;
    private static final int RESIDUE_HOT = 25;
    private static final int RESIDUE_COOL = 10;
    private static final int ORE_RESIDUE_MB = 125;
    private static final int HEAT_COOL = 0;
    private static final int HEAT_HOT = 1;
    private static final int HEAT_SEARING = 2;

    private static boolean applied = false;

    private static final List<MetalSpec> METALS = Arrays.asList(
        new MetalSpec("steel", "oreSteel", "ingotSteel"),
        new MetalSpec("copper", "oreCopper", "ingotCopper"),
        new MetalSpec("tin", "oreTin", "ingotTin"),
        new MetalSpec("bronze", "oreBronze", "ingotBronze"),
        new MetalSpec("brass", "oreBrass", "ingotBrass"),
        new MetalSpec("lead", "oreLead", "ingotLead"),
        new MetalSpec("zinc", "oreZinc", "ingotZinc"),
        new MetalSpec("obsidian_steel", "oreObsidianSteel", "ingotObsidianSteel"),
        new MetalSpec("titanium", "oreTitanium", "ingotTitanium"),
        new MetalSpec("diamond_steel", "oreDiamondSteel", "ingotDiamondSteel"),
        new MetalSpec("uranium", "oreUranium", "ingotUranium")
    );

    private BuildCraftOreProcessing() {
    }

    public static void apply() {
        if (applied) {
            return;
        }
        applied = true;

        if (!Loader.isModLoaded(BCOP_MODID)) {
            return;
        }

        try {
            Class<?> recipesClass = Class.forName(RECIPES_CLASS);
            Object recipesInstance = recipesClass.getField("INSTANCE").get(null);
            Object fluidManager = recipesClass.getMethod("getFluidProcessorRecipes").invoke(recipesInstance);
            Object oreManager = recipesClass.getMethod("getOreProcessorRecipes").invoke(recipesInstance);
            if (fluidManager == null || oreManager == null) {
                ParasitusFix.getLogger().warn("[BCOP] Recipe managers are not available.");
                return;
            }

            Fluid residueFluid = FluidRegistry.getFluid(GASEOUS_LAVA_SEARING);
            if (residueFluid == null) {
                ParasitusFix.getLogger().warn("[BCOP] Missing residue fluid '{}'; skipping integration.", GASEOUS_LAVA_SEARING);
                return;
            }

            Method registerFluidRecipe = findMethod(fluidManager, "registerSimpleRecipe", 4);
            Method registerOreRecipe = findMethod(oreManager, "registerSimpleRecipe", 3);
            Class<?> pairClass = Class.forName("kotlin.Pair");
            Constructor<?> pairCtor = pairClass.getConstructor(Object.class, Object.class);

            int fluidCount = 0;
            int oreCount = 0;
            for (MetalSpec metal : METALS) {
                ItemStack ingot = firstOreStack(metal.ingotDict);
                if (ingot.isEmpty()) {
                    continue;
                }

                String fluidBase = MOLTEN_PREFIX + metal.fluidBase;
                if (registerFluidRecipe(registerFluidRecipe, fluidManager, fluidBase + ".searing", ingot, 1, residueFluid, RESIDUE_SEARING)) fluidCount++;
                if (registerFluidRecipe(registerFluidRecipe, fluidManager, fluidBase + ".hot", ingot, 2, residueFluid, RESIDUE_HOT)) fluidCount++;
                if (registerFluidRecipe(registerFluidRecipe, fluidManager, fluidBase + ".cool", ingot, 3, residueFluid, RESIDUE_COOL)) fluidCount++;

                ItemStack ore = firstOreStack(metal.oreDict);
                if (ore.isEmpty()) {
                    continue;
                }

                Fluid searing = FluidRegistry.getFluid(fluidBase + ".searing");
                if (searing == null) {
                    continue;
                }

                if (registerOreRecipe(registerOreRecipe, oreManager, pairCtor, ore, searing, residueFluid)) {
                    oreCount++;
                }
            }

            int heatExchangeCount = registerHeatExchangerRecipes();
            ParasitusFix.getLogger().info(
                "[BCOP] Registered {} fluid recipes, {} ore recipes, and {} heat-exchanger recipes for custom molten fluids.",
                fluidCount, oreCount, heatExchangeCount
            );
        } catch (Throwable t) {
            ParasitusFix.getLogger().warn("[BCOP] Failed to register custom molten fluid integration.", t);
        }
    }

    private static int registerHeatExchangerRecipes() {
        IRefineryRecipeManager refinery = BuildcraftRecipeRegistry.refineryRecipes;
        if (refinery == null) {
            ParasitusFix.getLogger().warn("[BCOP] BuildCraft refinery recipe manager not available; skipping heat-exchanger integration.");
            return 0;
        }

        int added = 0;
        for (MetalSpec metal : METALS) {
            String fluidBase = MOLTEN_PREFIX + metal.fluidBase;
            Fluid cool = FluidRegistry.getFluid(fluidBase + ".cool");
            Fluid hot = FluidRegistry.getFluid(fluidBase + ".hot");
            Fluid searing = FluidRegistry.getFluid(fluidBase + ".searing");

            added += registerHeatPair(refinery, cool, hot, HEAT_COOL, HEAT_HOT);
            added += registerHeatPair(refinery, hot, searing, HEAT_HOT, HEAT_SEARING);
        }

        return added;
    }

    private static int registerHeatPair(IRefineryRecipeManager refinery, Fluid fromLower, Fluid fromHigher, int lowerHeat, int higherHeat) {
        if (fromLower == null || fromHigher == null) {
            return 0;
        }

        int added = 0;
        if (addHeatable(refinery, fromLower, fromHigher, lowerHeat, higherHeat)) {
            added++;
        }
        if (addCoolable(refinery, fromHigher, fromLower, higherHeat, lowerHeat)) {
            added++;
        }
        return added;
    }

    private static boolean addHeatable(IRefineryRecipeManager refinery, Fluid in, Fluid out, int heatFrom, int heatTo) {
        try {
            refinery.addHeatableRecipe(
                new FluidStack(in, BATCH_MB),
                new FluidStack(out, BATCH_MB),
                heatFrom,
                heatTo
            );
            return true;
        } catch (Throwable t) {
            ParasitusFix.getLogger().debug("[BCOP] Failed heatable registration '{} -> {}'.", in.getName(), out.getName(), t);
            return false;
        }
    }

    private static boolean addCoolable(IRefineryRecipeManager refinery, Fluid in, Fluid out, int heatFrom, int heatTo) {
        try {
            refinery.addCoolableRecipe(
                new FluidStack(in, BATCH_MB),
                new FluidStack(out, BATCH_MB),
                heatFrom,
                heatTo
            );
            return true;
        } catch (Throwable t) {
            ParasitusFix.getLogger().debug("[BCOP] Failed coolable registration '{} -> {}'.", in.getName(), out.getName(), t);
            return false;
        }
    }

    private static boolean registerFluidRecipe(
        Method method,
        Object manager,
        String fluidName,
        ItemStack ingotTemplate,
        int ingotCount,
        Fluid residue,
        int residueAmount
    ) {
        Fluid inputFluid = FluidRegistry.getFluid(fluidName);
        if (inputFluid == null) {
            return false;
        }

        ItemStack output = ingotTemplate.copy();
        output.setCount(ingotCount);

        try {
            method.invoke(
                manager,
                new FluidStack(inputFluid, BATCH_MB),
                output,
                new FluidStack(residue, residueAmount),
                PROCESS_TICKS
            );
            return true;
        } catch (Throwable t) {
            ParasitusFix.getLogger().debug("[BCOP] Failed fluid recipe for '{}'.", fluidName, t);
            return false;
        }
    }

    private static boolean registerOreRecipe(
        Method method,
        Object manager,
        Constructor<?> pairCtor,
        ItemStack oreInput,
        Fluid outputFluid,
        Fluid residue
    ) {
        try {
            Object fluids = pairCtor.newInstance(
                new FluidStack(outputFluid, BATCH_MB),
                new FluidStack(residue, ORE_RESIDUE_MB)
            );
            method.invoke(manager, oreInput.copy(), fluids, PROCESS_TICKS);
            return true;
        } catch (Throwable t) {
            ParasitusFix.getLogger().debug("[BCOP] Failed ore recipe for '{}'.", outputFluid.getName(), t);
            return false;
        }
    }

    private static ItemStack firstOreStack(String oreDictName) {
        List<ItemStack> ores = OreDictionary.getOres(oreDictName);
        if (ores == null || ores.isEmpty()) {
            return ItemStack.EMPTY;
        }

        ItemStack stack = ores.get(0);
        return stack == null ? ItemStack.EMPTY : stack.copy();
    }

    private static Method findMethod(Object target, String name, int params) throws NoSuchMethodException {
        for (Method m : target.getClass().getMethods()) {
            if (name.equals(m.getName()) && m.getParameterTypes().length == params) {
                return m;
            }
        }
        throw new NoSuchMethodException(target.getClass().getName() + "#" + name + "/" + params);
    }

    private static final class MetalSpec {
        final String fluidBase;
        final String oreDict;
        final String ingotDict;

        MetalSpec(String fluidBase, String oreDict, String ingotDict) {
            this.fluidBase = fluidBase;
            this.oreDict = oreDict;
            this.ingotDict = ingotDict;
        }
    }
}
