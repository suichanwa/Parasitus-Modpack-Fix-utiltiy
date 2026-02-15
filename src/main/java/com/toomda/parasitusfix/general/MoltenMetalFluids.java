package com.toomda.parasitusfix.general;

//import buildcraft fluidtemp class
import buildcraft.lib.fluid.BCFluid;
import com.toomda.parasitusfix.ParasitusFix;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.FluidRegistry;

/**
 * Molten Metal Fluids - Forge 1.12.2
 *
 * Registers cool, hot, and searing variants of molten metals,
 * modelled after BuildCraft's oil temperature system.
 *
 * Usage: Call MoltenMetalFluids.registerAll() in your preInit phase.
 */
public class MoltenMetalFluids {
    private static boolean applied = false;

    public static BCFluid moltenDiamondSteel_cool;
    public static BCFluid moltenDiamondSteel_hot;
    public static BCFluid moltenDiamondSteel_searing;

    public static BCFluid moltenUranium_cool;
    public static BCFluid moltenUranium_hot;
    public static BCFluid moltenUranium_searing;

    public static BCFluid moltenObsidianSteel_cool;
    public static BCFluid moltenObsidianSteel_hot;
    public static BCFluid moltenObsidianSteel_searing;

    public static BCFluid moltenSteel_cool;
    public static BCFluid moltenSteel_hot;
    public static BCFluid moltenSteel_searing;

    // Bronze
    public static BCFluid moltenBronze_cool;
    public static BCFluid moltenBronze_hot;
    public static BCFluid moltenBronze_searing;

    // Brass
    public static BCFluid moltenBrass_cool;
    public static BCFluid moltenBrass_hot;
    public static BCFluid moltenBrass_searing;

    // Titanium
    public static BCFluid moltenTitanium_cool;
    public static BCFluid moltenTitanium_hot;
    public static BCFluid moltenTitanium_searing;

    // Zinc
    public static BCFluid moltenZinc_cool;
    public static BCFluid moltenZinc_hot;
    public static BCFluid moltenZinc_searing;

    // Lead
    public static BCFluid moltenLead_cool;
    public static BCFluid moltenLead_hot;
    public static BCFluid moltenLead_searing;

    // Tin
    public static BCFluid moltenTin_cool;
    public static BCFluid moltenTin_hot;
    public static BCFluid moltenTin_searing;

    // Copper
    public static BCFluid moltenCopper_cool;
    public static BCFluid moltenCopper_hot;
    public static BCFluid moltenCopper_searing;

    // ─────────────────────────────────────────────────────────────
    //  Temperature values  (Kelvin, matching BC's scale)
    //    cool    ~  600 K  (just melted, barely flowing)
    //    hot     ~ 1300 K  (actively molten)
    //    searing ~ 2500 K  (superheated / blast-furnace tier)
    // ─────────────────────────────────────────────────────────────
    private static final int TEMP_COOL    =  600;
    private static final int TEMP_HOT     = 1300;
    private static final int TEMP_SEARING = 2500;

    // Viscosity (mB/t flow speed; higher = thicker)
    private static final int VISCOSITY_COOL    = 10000;
    private static final int VISCOSITY_HOT     =  4000;
    private static final int VISCOSITY_SEARING =  1000;

    // Luminosity
    private static final int LUMINOSITY_COOL    =  0;
    private static final int LUMINOSITY_HOT     = 12;
    private static final int LUMINOSITY_SEARING = 15;

    // Ingot palette sampled from your sheet (order reference).
    private static final int INGOT_STEEL          = 0xFFC1C4CB;
    private static final int INGOT_COPPER         = 0xFFC88133;
    private static final int INGOT_TIN            = 0xFFA4D2CC;
    private static final int INGOT_BRONZE         = 0xFF8E571A;
    private static final int INGOT_BRASS          = 0xFFD09A54;
    private static final int INGOT_LEAD           = 0xFF66798A;
    private static final int INGOT_ZINC           = 0xFFD8C496;
    private static final int INGOT_OBSIDIAN_STEEL = 0xFF2C2340;
    private static final int INGOT_TITANIUM       = 0xFFC6CBD4;
    private static final int INGOT_DIAMOND_STEEL  = 0xFF8BBBE1;
    private static final int INGOT_URANIUM        = 0xFF6DB76E;

    private static final float TINT_COOL = 0.72F;
    private static final float TINT_HOT = 1.00F;
    private static final float TINT_SEARING = 1.25F;

    public static void apply() {
        if (applied) {
            return;
        }
        applied = true;
        registerAll();
        ParasitusFix.getLogger().info("Registered molten metal BC fluids.");
    }

    public static void setColorOverride(String baseName, String tempName, int argb) {
        FluidColorHandler.setOverride(baseName + "." + tempName, argb);
    }

    /** Call this in your mod's apply/init hook. */
    public static void registerAll() {
        // Ingot order:
        // steel, copper, tin, bronze
        // brass, lead, zinc, obsidian_steel
        // iron, gold, titanium, diamond_steel
        // (this class currently registers no molten_iron / molten_gold)

        moltenSteel_cool    = register("molten_steel", "cool", colorFor("cool", INGOT_STEEL));
        moltenSteel_hot     = register("molten_steel", "hot", colorFor("hot", INGOT_STEEL));
        moltenSteel_searing = register("molten_steel", "searing", colorFor("searing", INGOT_STEEL));

        moltenCopper_cool    = register("molten_copper", "cool", colorFor("cool", INGOT_COPPER));
        moltenCopper_hot     = register("molten_copper", "hot", colorFor("hot", INGOT_COPPER));
        moltenCopper_searing = register("molten_copper", "searing", colorFor("searing", INGOT_COPPER));

        moltenTin_cool    = register("molten_tin", "cool", colorFor("cool", INGOT_TIN));
        moltenTin_hot     = register("molten_tin", "hot", colorFor("hot", INGOT_TIN));
        moltenTin_searing = register("molten_tin", "searing", colorFor("searing", INGOT_TIN));

        moltenBronze_cool    = register("molten_bronze", "cool", colorFor("cool", INGOT_BRONZE));
        moltenBronze_hot     = register("molten_bronze", "hot", colorFor("hot", INGOT_BRONZE));
        moltenBronze_searing = register("molten_bronze", "searing", colorFor("searing", INGOT_BRONZE));

        moltenBrass_cool    = register("molten_brass", "cool", colorFor("cool", INGOT_BRASS));
        moltenBrass_hot     = register("molten_brass", "hot", colorFor("hot", INGOT_BRASS));
        moltenBrass_searing = register("molten_brass", "searing", colorFor("searing", INGOT_BRASS));

        moltenLead_cool    = register("molten_lead", "cool", colorFor("cool", INGOT_LEAD));
        moltenLead_hot     = register("molten_lead", "hot", colorFor("hot", INGOT_LEAD));
        moltenLead_searing = register("molten_lead", "searing", colorFor("searing", INGOT_LEAD));

        moltenZinc_cool    = register("molten_zinc", "cool", colorFor("cool", INGOT_ZINC));
        moltenZinc_hot     = register("molten_zinc", "hot", colorFor("hot", INGOT_ZINC));
        moltenZinc_searing = register("molten_zinc", "searing", colorFor("searing", INGOT_ZINC));

        moltenObsidianSteel_cool    = register("molten_obsidian_steel", "cool", colorFor("cool", INGOT_OBSIDIAN_STEEL));
        moltenObsidianSteel_hot     = register("molten_obsidian_steel", "hot", colorFor("hot", INGOT_OBSIDIAN_STEEL));
        moltenObsidianSteel_searing = register("molten_obsidian_steel", "searing", colorFor("searing", INGOT_OBSIDIAN_STEEL));

        moltenTitanium_cool    = register("molten_titanium", "cool", colorFor("cool", INGOT_TITANIUM));
        moltenTitanium_hot     = register("molten_titanium", "hot", colorFor("hot", INGOT_TITANIUM));
        moltenTitanium_searing = register("molten_titanium", "searing", colorFor("searing", INGOT_TITANIUM));

        moltenDiamondSteel_cool    = register("molten_diamond_steel", "cool", colorFor("cool", INGOT_DIAMOND_STEEL));
        moltenDiamondSteel_hot     = register("molten_diamond_steel", "hot", colorFor("hot", INGOT_DIAMOND_STEEL));
        moltenDiamondSteel_searing = register("molten_diamond_steel", "searing", colorFor("searing", INGOT_DIAMOND_STEEL));

        // Extra (outside your 12-ingot order)
        moltenUranium_cool    = register("molten_uranium", "cool", colorFor("cool", INGOT_URANIUM));
        moltenUranium_hot     = register("molten_uranium", "hot", colorFor("hot", INGOT_URANIUM));
        moltenUranium_searing = register("molten_uranium", "searing", colorFor("searing", INGOT_URANIUM));
    }

    // ─────────────────────────────────────────────────────────────
    //  Internal helper
    // ─────────────────────────────────────────────────────────────

    /**
     * Builds and registers a single temperature-variant molten fluid.
     *
     * @param baseName  e.g. "molten_copper"
     * @param tempName  "cool" | "hot" | "searing"
     * @param color     packed ARGB int used for the still/flowing texture tint
     * @return the registered BCFluid
     */
    private static BCFluid register(String baseName, String tempName, int color) {

        // Fluid name format:  molten_copper.cool  (dots are fine, BC uses them too)
        String fluidName = baseName + "." + tempName;
        int resolvedColor = FluidColorHandler.resolveColor(fluidName, color);

        int temp, viscosity, luminosity;
        switch (tempName) {
            case "hot":
                temp       = TEMP_HOT;
                viscosity  = VISCOSITY_HOT;
                luminosity = LUMINOSITY_HOT;
                break;
            case "searing":
                temp       = TEMP_SEARING;
                viscosity  = VISCOSITY_SEARING;
                luminosity = LUMINOSITY_SEARING;
                break;
            default: // "cool"
                temp       = TEMP_COOL;
                viscosity  = VISCOSITY_COOL;
                luminosity = LUMINOSITY_COOL;
                break;
        }

        // Texture locations — put your still/flowing textures here.
        // Using a generic "molten" texture tinted by colour is the
        // simplest approach; swap for per-metal textures if desired.
        ResourceLocation still   = new ResourceLocation("yourmod", "blocks/fluid_molten_still");
        ResourceLocation flowing = new ResourceLocation("yourmod", "blocks/fluid_molten_flowing");

        BCFluid fluid = new BCFluid(fluidName, still, flowing);
        fluid.setColour(resolvedColor);
        fluid.setHeat(temp);
        fluid.setHeatable(true);
        fluid.setTemperature(temp);
        fluid.setViscosity(viscosity);
        fluid.setLuminosity(luminosity);
        fluid.setDensity(3000); // heavier than water (1000)
        fluid.setGaseous(false);

        // Searing metals glow like lava
        if ("searing".equals(tempName)) {
            fluid.setLuminosity(15).setTemperature(temp);
        }

        FluidRegistry.registerFluid(fluid);
        FluidRegistry.addBucketForFluid(fluid);   // optional: gives a bucket item

        return fluid;
    }

    private static int colorFor(String tempName, int ingotColor) {
        switch (tempName) {
            case "hot":
                return tint(ingotColor, TINT_HOT);
            case "searing":
                return tint(ingotColor, TINT_SEARING);
            default:
                return tint(ingotColor, TINT_COOL);
        }
    }

    private static int tint(int argb, float factor) {
        int a = (argb >>> 24) & 0xFF;
        int r = (argb >>> 16) & 0xFF;
        int g = (argb >>> 8) & 0xFF;
        int b = argb & 0xFF;

        r = clampChannel((int) (r * factor));
        g = clampChannel((int) (g * factor));
        b = clampChannel((int) (b * factor));

        return (a << 24) | (r << 16) | (g << 8) | b;
    }

    private static int clampChannel(int value) {
        if (value < 0) return 0;
        if (value > 255) return 255;
        return value;
    }
}
