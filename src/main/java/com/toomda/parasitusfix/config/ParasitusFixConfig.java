package com.toomda.parasitusfix.config;

import com.toomda.parasitusfix.ParasitusFix;
import net.minecraftforge.common.config.Config;

@Config(modid = ParasitusFix.MODID)
public final class ParasitusFixConfig {

    @Config.Name("7DTM Tools")
    public static final SevenDaysTools TOOLS = new SevenDaysTools();

    @Config.Name("Bleeding")
    public static final Bleeding BLEEDING = new Bleeding();

    public static final class SevenDaysTools {

        @Config.Comment("Scrap pickaxe base damage")
        public float scrapPickaxeDamage = 3.0F;

        @Config.Comment("Scrap axe base damage")
        public float scrapAxeDamage = 3.5F;

        @Config.Comment("Scrap shovel base damage")
        public float scrapShovelDamage = 2.5F;

        @Config.Comment("Scrap hoe base damage")
        public float scrapHoeDamage = 2.0F;

        @Config.Comment("Copper pickaxe base damage")
        public float copperPickaxeDamage = 4.0F;

        @Config.Comment("Copper axe base damage")
        public float copperAxeDamage = 4.5F;

        @Config.Comment("Copper shovel base damage")
        public float copperShovelDamage = 3.5F;

        @Config.Comment("Copper hoe base damage")
        public float copperHoeDamage = 3.0F;

        @Config.Comment("Copper sword base damage")
        public float copperSwordDamage = 5.0F;

        @Config.Comment("Bronze pickaxe base damage")
        public float bronzePickaxeDamage = 4.5F;

        @Config.Comment("Bronze axe base damage")
        public float bronzeAxeDamage = 5.0F;

        @Config.Comment("Bronze shovel base damage")
        public float bronzeShovelDamage = 4.0F;

        @Config.Comment("Bronze hoe base damage")
        public float bronzeHoeDamage = 3.5F;

        @Config.Comment("Bronze sword base damage")
        public float bronzeSwordDamage = 5.5F;
    }

    public static final class Bleeding {

        @Config.Comment("Absolute damage needed for a single hit to possibly cause bleeding")
        public float singleHitAbs = 6.0F;

        @Config.Comment("Ratio of max health needed for a single hit to possibly cause bleeding")
        public float singleHitRatio = 0.2199999988079071F;

        @Config.Comment("Chance for bleeding on a big single hit (0.0 - 1.0)")
        public double singleHitChance = 0.10D;

        @Config.Comment("Time window (in ticks) to accumulate damage pressure")
        public int windowTicks = 40;

        @Config.Comment("Total accumulated damage needed to trigger bleeding")
        public float sumThreshold = 8.0F;

        @Config.Comment("Number of hits in window needed to trigger bleeding")
        public int hitsThreshold = 3;

        @Config.Comment("Chance for bleeding when pressure threshold is met (0.0 - 1.0)")
        public double pressureChance = 0.30D;

        @Config.Comment("Minimum bleed duration in seconds")
        public int minDurationSeconds = 5;

        @Config.Comment("Maximum bleed duration in seconds")
        public int maxDurationSeconds = 7;

        @Config.Comment("Damage value that maps to maximum bleeding duration")
        public float maxDurationDamage = 10.0F;

        @Config.Comment("Minimum ticks between bleeding damage applications (1 = no throttling)")
        public int damageIntervalTicks = 20;

        @Config.Comment("Use a random interval between min/max for bleeding damage ticks")
        public boolean nondeterministicBleedingRate = false;

        @Config.Comment("Minimum ticks between bleeding damage when nondeterministic rate is enabled")
        public int minDamageIntervalTicks = 15;

        @Config.Comment("Maximum ticks between bleeding damage when nondeterministic rate is enabled")
        public int maxDamageIntervalTicks = 25;

        @Config.Comment("Bleeding potion amplifier")
        public int amplifier = 0;
    }
}
