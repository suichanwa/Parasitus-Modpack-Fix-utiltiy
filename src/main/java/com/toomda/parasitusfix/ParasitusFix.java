package com.toomda.parasitusfix;

import com.toomda.parasitusfix.Doors.ParasitusDoors;
import com.toomda.parasitusfix.commands.ParasitusFixCommand;
import com.toomda.parasitusfix.sevendaystomine.BandageInstantUse;
import com.toomda.parasitusfix.sevendaystomine.BarbedWireDurabilityFix;
import com.toomda.parasitusfix.sevendaystomine.BleedDamageLimiter;
import com.toomda.parasitusfix.sevendaystomine.BleedEffectClamp;
import com.toomda.parasitusfix.sevendaystomine.BleedingTamer;
import com.toomda.parasitusfix.sevendaystomine.CrawlerArmorFix;
import com.toomda.parasitusfix.sevendaystomine.EnchantItemFix;
import com.toomda.parasitusfix.sevendaystomine.SevenDaysBlockPatches;
import com.toomda.parasitusfix.sevendaystomine.SevenDaysChanceConfigGuard;
import com.toomda.parasitusfix.sevendaystomine.SevenDaysDamagePatches;
import com.toomda.parasitusfix.sevendaystomine.ZombieSpawnFix;
import com.toomda.parasitusfix.techguns.TechgunsAttackHelicopterTargetFix;
import com.toomda.parasitusfix.techguns.TechgunsAttackHelicopterNoDespawn;
import com.toomda.parasitusfix.techguns.TechgunsSoldierZombieTargetFix;
import com.toomda.parasitusfix.techguns.TechgunsGrinderDurabilityFix;
import com.toomda.parasitusfix.techguns.TechgunsBlockHardnessCap;
import com.toomda.parasitusfix.buildcraft.QuartzKinesisPipeCapRemoval;
import net.minecraft.init.Blocks;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import org.apache.logging.log4j.Logger;

@Mod(modid = ParasitusFix.MODID, name = ParasitusFix.NAME, version = ParasitusFix.VERSION, dependencies = "required-after:sevendaystomine")
public class ParasitusFix
{
    public static final String MODID = "parasitusfix";
    public static final String NAME = "ParasitusFix";
    public static final String VERSION = "1.1.02";

    private static Logger logger;

    public static org.apache.logging.log4j.Logger getLogger() { return logger; }

    @EventHandler
    public void preInit(FMLPreInitializationEvent event)
    {
        logger = event.getModLog();
        ParasitusDoors.registerAll();
    }

    @EventHandler
    public void init(FMLInitializationEvent event)
    {
        MinecraftForge.EVENT_BUS.register(new CrawlerArmorFix());

        if (Loader.isModLoaded("sevendaystomine")) {
            MinecraftForge.EVENT_BUS.register(new BleedingTamer());
            MinecraftForge.EVENT_BUS.register(new BleedDamageLimiter());
            MinecraftForge.EVENT_BUS.register(new BleedEffectClamp());
            MinecraftForge.EVENT_BUS.register(new BandageInstantUse());
            MinecraftForge.EVENT_BUS.register(new ZombieSpawnFix());
            MinecraftForge.EVENT_BUS.register(new BarbedWireDurabilityFix());
            MinecraftForge.EVENT_BUS.register(new EnchantItemFix());
        }
        if (Loader.isModLoaded("techguns")) {
            MinecraftForge.EVENT_BUS.register(new TechgunsAttackHelicopterTargetFix());
            MinecraftForge.EVENT_BUS.register(new TechgunsAttackHelicopterNoDespawn());
            MinecraftForge.EVENT_BUS.register(new TechgunsSoldierZombieTargetFix());
        }
        logger.info("DIRT BLOCK >> {}", Blocks.DIRT.getRegistryName());
    }

    @EventHandler
    public void postInit(FMLPostInitializationEvent event) {
        SevenDaysChanceConfigGuard.apply();
        SevenDaysDamagePatches.apply();
        SevenDaysBlockPatches.apply();
        if (Loader.isModLoaded("techguns")) {
            TechgunsBlockHardnessCap.apply();
            TechgunsGrinderDurabilityFix.apply();
        }
        if (Loader.isModLoaded("buildcrafttransport")) {
            QuartzKinesisPipeCapRemoval.apply();
        }
    }

    @EventHandler
    public void serverStarting(FMLServerStartingEvent event) {
        event.registerServerCommand(new ParasitusFixCommand());
        logger.info("ParasitusFix commands registered");
    }
}


