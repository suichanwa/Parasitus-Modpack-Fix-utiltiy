package com.toomda.parasitusfix.sevendaystomine;

import com.toomda.parasitusfix.ParasitusFix;
import net.minecraft.entity.Entity;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.living.LivingSpawnEvent;
import net.minecraftforge.fml.common.eventhandler.Event;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import nuparu.sevendaystomine.entity.EntityZombieBase;

public class SevenDaysZombieEndSpawnRestrict {
    private static final int END_DIMENSION_ID = 1;

    @SubscribeEvent
    public void onCheckSpawn(LivingSpawnEvent.CheckSpawn event) {
        if (!isSevenDaysZombie(event.getEntity())) return;

        World world = event.getWorld();
        if (world.provider.getDimension() == END_DIMENSION_ID) {
            event.setResult(Event.Result.DENY);
            ParasitusFix.getLogger().debug("[7DTM] Blocked zombie spawn in The End");
        }
    }

    @SubscribeEvent
    public void onSpecialSpawn(LivingSpawnEvent.SpecialSpawn event) {
        if (!isSevenDaysZombie(event.getEntity())) return;

        World world = event.getWorld();
        if (world.provider.getDimension() == END_DIMENSION_ID) {
            event.setCanceled(true);
            ParasitusFix.getLogger().debug("[7DTM] Blocked zombie special spawn in The End");
        }
    }

    @SubscribeEvent
    public void onJoinWorld(EntityJoinWorldEvent event) {
        if (!isSevenDaysZombie(event.getEntity())) return;

        World world = event.getWorld();
        if (world.provider.getDimension() == END_DIMENSION_ID) {
            event.setCanceled(true);
            ParasitusFix.getLogger().debug("[7DTM] Blocked zombie join-world spawn in The End");
        }
    }

    private static boolean isSevenDaysZombie(Entity entity) {
        return entity instanceof EntityZombieBase;
    }
}
