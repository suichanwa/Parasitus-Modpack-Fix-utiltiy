package com.toomda.parasitusfix.sevendaystomine;

import com.toomda.parasitusfix.ParasitusFix;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.living.LivingSpawnEvent;
import net.minecraftforge.fml.common.eventhandler.Event;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import nuparu.sevendaystomine.entity.EntityZombieBase;

public class SevenDaysZombieEndSpawnRestrict {
    private static final int END_DIMENSION_ID = 1;
    private static final int MAX_BLOCK_LIGHT_FOR_SPAWN = 7;

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onCheckSpawn(LivingSpawnEvent.CheckSpawn event) {
        if (!isSevenDaysZombie(event.getEntity())) return;

        World world = event.getWorld();
        if (world.isRemote) return;
        if (world.provider.getDimension() == END_DIMENSION_ID) {
            event.setResult(Event.Result.DENY);
            ParasitusFix.getLogger().debug("[7DTM] Blocked zombie spawn in The End");
            return;
        }

        if (event.isSpawner()) return;

        BlockPos pos = new BlockPos(event.getX(), event.getY(), event.getZ());
        if (isBlockedByLight(world, pos)) {
            event.setResult(Event.Result.DENY);
            ParasitusFix.getLogger().debug("[7DTM] Blocked zombie spawn in lit area (block light {})", getBlockLight(world, pos));
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onSpecialSpawn(LivingSpawnEvent.SpecialSpawn event) {
        if (!isSevenDaysZombie(event.getEntity())) return;

        World world = event.getWorld();
        if (world.isRemote) return;
        if (world.provider.getDimension() == END_DIMENSION_ID) {
            event.setCanceled(true);
            ParasitusFix.getLogger().debug("[7DTM] Blocked zombie special spawn in The End");
            return;
        }

        BlockPos pos = new BlockPos(event.getX(), event.getY(), event.getZ());
        if (isBlockedByLight(world, pos)) {
            event.setCanceled(true);
            ParasitusFix.getLogger().debug("[7DTM] Blocked zombie special spawn in lit area (block light {})", getBlockLight(world, pos));
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onJoinWorld(EntityJoinWorldEvent event) {
        if (!isSevenDaysZombie(event.getEntity())) return;

        World world = event.getWorld();
        if (world.isRemote) return;
        if (world.provider.getDimension() == END_DIMENSION_ID) {
            event.setCanceled(true);
            ParasitusFix.getLogger().debug("[7DTM] Blocked zombie join-world spawn in The End");
            return;
        }

        if (event.getEntity().ticksExisted > 1) return;

        BlockPos pos = event.getEntity().getPosition();
        if (isBlockedByLight(world, pos)) {
            event.setCanceled(true);
            ParasitusFix.getLogger().debug("[7DTM] Blocked zombie join-world spawn in lit area (block light {})", getBlockLight(world, pos));
        }
    }

    private static boolean isSevenDaysZombie(Entity entity) {
        return entity instanceof EntityZombieBase;
    }

    private static boolean isBlockedByLight(World world, BlockPos pos) {
        return world.isBlockLoaded(pos) && getBlockLight(world, pos) > MAX_BLOCK_LIGHT_FOR_SPAWN;
    }

    private static int getBlockLight(World world, BlockPos pos) {
        return world.getLightFor(EnumSkyBlock.BLOCK, pos);
    }
}
