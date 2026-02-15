package com.toomda.parasitusfix;

import com.toomda.parasitusfix.commands.ParasitusFixCommand;
import com.toomda.parasitusfix.general.MoltenMetalFluids;
import com.toomda.parasitusfix.sevendaystomine.BandageInstantUse;
import com.toomda.parasitusfix.sevendaystomine.BarbedWireDurabilityFix;
import com.toomda.parasitusfix.sevendaystomine.BleedDamageLimiter;
import com.toomda.parasitusfix.sevendaystomine.BleedEffectClamp;
import com.toomda.parasitusfix.sevendaystomine.BleedingTamer;
import com.toomda.parasitusfix.sevendaystomine.CrawlerArmorFix;
import com.toomda.parasitusfix.sevendaystomine.EnchantItemFix;
import com.toomda.parasitusfix.sevendaystomine.FlamethrowerTrapFuelSwap;
import com.toomda.parasitusfix.sevendaystomine.SevenDaysBlockPatches;
import com.toomda.parasitusfix.sevendaystomine.SevenDaysChanceConfigGuard;
import com.toomda.parasitusfix.sevendaystomine.SevenDaysDamagePatches;
import com.toomda.parasitusfix.sevendaystomine.SevenDaysZombieEndSpawnRestrict;
import com.toomda.parasitusfix.techguns.TechgunsAttackHelicopterTargetFix;
import com.toomda.parasitusfix.techguns.TechgunsAttackHelicopterNoDespawn;
import com.toomda.parasitusfix.techguns.TechgunsSoldierZombieTargetFix;
import com.toomda.parasitusfix.techguns.TechgunsGrinderDurabilityFix;
import com.toomda.parasitusfix.techguns.TechgunsBlockHardnessCap;
import com.toomda.parasitusfix.Doors.ParasitusDoors;
import com.toomda.parasitusfix.buildcraft.BuildCraftOreProcessing;
import com.toomda.parasitusfix.buildcraft.QuartzKinesisPipeCapRemoval;
import net.minecraft.entity.Entity;
import net.minecraft.init.Blocks;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingSpawnEvent;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.eventhandler.Event;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.apache.logging.log4j.Logger;
import techguns.entities.npcs.ZombieFarmer;
import techguns.entities.npcs.ZombieMiner;
import techguns.entities.npcs.ZombiePigmanSoldier;
import techguns.entities.npcs.ZombiePoliceman;
import techguns.entities.npcs.ZombieSoldier;

@Mod(modid = ParasitusFix.MODID, name = ParasitusFix.NAME, version = ParasitusFix.VERSION, dependencies = "required-after:sevendaystomine")
public class ParasitusFix
{
    public static final String MODID = "parasitusfix";
    public static final String NAME = "ParasitusFix";
    public static final String VERSION = "1.3";

    private static Logger logger;

    public static org.apache.logging.log4j.Logger getLogger() { return logger; }

    @EventHandler
    public void preInit(FMLPreInitializationEvent event)
    {
        logger = event.getModLog();
        if (Loader.isModLoaded("buildcrafttransport") || Loader.isModLoaded("bcoreprocessing")) {
            MoltenMetalFluids.apply();
        }
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
            MinecraftForge.EVENT_BUS.register(new BarbedWireDurabilityFix());
            MinecraftForge.EVENT_BUS.register(new EnchantItemFix());
            MinecraftForge.EVENT_BUS.register(new SevenDaysZombieEndSpawnRestrict());
            if (Loader.isModLoaded("buildcraftenergy")) {
                MinecraftForge.EVENT_BUS.register(new FlamethrowerTrapFuelSwap());
                logger.info("Flamethrower trap accepts BuildCraft heavy oil (cool)");
            }
        }
        if (Loader.isModLoaded("techguns")) {
            MinecraftForge.EVENT_BUS.register(new TechgunsAttackHelicopterTargetFix());
            MinecraftForge.EVENT_BUS.register(new TechgunsAttackHelicopterNoDespawn());
            MinecraftForge.EVENT_BUS.register(new TechgunsSoldierZombieTargetFix());
            MinecraftForge.EVENT_BUS.register(new TechgunsZombieEndSpawnRestrict());
            ParasitusDoors.registerAll();
        }
        logger.info("DIRT BLOCK >> {}", Blocks.DIRT.getRegistryName());
    }

    @EventHandler
    public void postInit(FMLPostInitializationEvent event) {
        if (Loader.isModLoaded("sevendaystomine")) {
        }
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
        if (Loader.isModLoaded("bcoreprocessing")) {
            BuildCraftOreProcessing.apply();
        }
    }

    @EventHandler
    public void serverStarting(FMLServerStartingEvent event) {
        event.registerServerCommand(new ParasitusFixCommand());
        logger.info("ParasitusFix commands registered");
    }

    private static final class TechgunsZombieEndSpawnRestrict {
        private static final int END_DIMENSION_ID = 1;

        @SubscribeEvent
        public void onCheckSpawn(LivingSpawnEvent.CheckSpawn event) {
            if (!isTechgunsZombie(event.getEntity())) return;

            World world = event.getWorld();
            if (world.provider.getDimension() == END_DIMENSION_ID) {
                event.setResult(Event.Result.DENY);
                ParasitusFix.getLogger().debug("Blocked Techguns zombie spawn in The End");
            }
        }

        @SubscribeEvent
        public void onSpecialSpawn(LivingSpawnEvent.SpecialSpawn event) {
            if (!isTechgunsZombie(event.getEntity())) return;

            World world = event.getWorld();
            if (world.provider.getDimension() == END_DIMENSION_ID) {
                event.setCanceled(true);
                ParasitusFix.getLogger().debug("Blocked Techguns zombie special spawn in The End");
            }
        }

        private static boolean isTechgunsZombie(Entity entity) {
            return entity instanceof ZombieSoldier
                || entity instanceof ZombiePoliceman
                || entity instanceof ZombieFarmer
                || entity instanceof ZombieMiner
                || entity instanceof ZombiePigmanSoldier;
        }
    }
}
