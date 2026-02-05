package com.toomda.parasitusfix.config;

import com.toomda.parasitusfix.ParasitusFix;
import net.minecraftforge.common.config.Config;

@Config(modid = ParasitusFix.MODID)
public final class ParasitusFixConfig {

    @Config.Name("Bleeding")
    public static final Bleeding BLEEDING = new Bleeding();

    public static final class Bleeding {

        @Config.Comment("Absolute damage needed for a single hit to possibly cause bleeding")
        public float singleHitAbs = 6.0F;

        @Config.Comment("Ratio of max health needed for a single hit to possibly cause bleeding")
        public float singleHitRatio = 0.22F;

        @Config.Comment("Chance for bleeding on a big single hit (0.0 - 1.0)")
        public double singleHitChance = 0.25D;

        @Config.Comment("Time window (in ticks) to accumulate damage pressure")
        public int windowTicks = 40;

        @Config.Comment("Total accumulated damage needed to trigger bleeding")
        public float sumThreshold = 6.0F;

        @Config.Comment("Number of hits in window needed to trigger bleeding")
        public int hitsThreshold = 3;

        @Config.Comment("Chance for bleeding when pressure threshold is met (0.0 - 1.0)")
        public double pressureChance = 0.60D;

        @Config.Comment("Minimum bleed duration in seconds")
        public int minDurationSeconds = 3;

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
