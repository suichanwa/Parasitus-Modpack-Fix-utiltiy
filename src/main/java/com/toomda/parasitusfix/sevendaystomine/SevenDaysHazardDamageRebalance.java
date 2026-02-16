package com.toomda.parasitusfix.sevendaystomine;

import com.toomda.parasitusfix.config.ParasitusFixConfig;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.projectile.EntityThrowable;
import net.minecraft.util.DamageSource;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.ProjectileImpactEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import nuparu.sevendaystomine.entity.EntityFlame;

public class SevenDaysHazardDamageRebalance {
    private static final String MODID = "sevendaystomine";

    private static final String WOODEN_SPIKES = "wooden_spikes";
    private static final String WOODEN_SPIKES_BLOODED = "wooden_spikes_blooded";
    private static final String WOODEN_SPIKES_BROKEN = "wooden_spikes_broken";
    private static final String METAL_SPIKES = "metal_spikes";
    private static final String RAZOR_WIRE = "razor_wire";

    @SubscribeEvent
    public void onLivingHurt(LivingHurtEvent event) {
        if (event.getEntityLiving().world.isRemote) return;
        if (event.getSource() != DamageSource.CACTUS) return;

        HazardType hazard = findHazardType(event.getEntityLiving());
        if (hazard == HazardType.NONE) return;

        ParasitusFixConfig.SevenDaysCombat cfg = ParasitusFixConfig.COMBAT;
        switch (hazard) {
            case METAL_SPIKES:
                event.setAmount(Math.max(0.0F, cfg.metalSpikesDamage));
                break;
            case WOODEN_SPIKES:
                event.setAmount(Math.max(0.0F, cfg.woodenSpikesDamage));
                break;
            case RAZOR_WIRE:
                event.setAmount(Math.max(0.0F, cfg.barbedWireDamage));
                break;
            default:
                break;
        }
    }

    @SubscribeEvent
    public void onProjectileImpact(ProjectileImpactEvent event) {
        if (event.getEntity().world.isRemote) return;
        if (!(event.getEntity() instanceof EntityFlame)) return;

        EntityFlame flame = (EntityFlame) event.getEntity();
        if (!(flame instanceof EntityThrowable)) return;

        EntityThrowable throwable = (EntityThrowable) flame;
        if (throwable.getThrower() != null) return;

        RayTraceResult hit = event.getRayTraceResult();
        if (hit == null || hit.entityHit == null || !(hit.entityHit instanceof EntityLivingBase)) return;

        EntityLivingBase target = (EntityLivingBase) hit.entityHit;
        int seconds = Math.max(0, ParasitusFixConfig.COMBAT.flameTurretFireSeconds);
        target.setFire(seconds);

        flame.setDead();
        event.setCanceled(true);
    }

    private static HazardType findHazardType(EntityLivingBase living) {
        World world = living.world;
        AxisAlignedBB bb = living.getEntityBoundingBox().grow(0.05D);

        int minX = MathHelper.floor(bb.minX);
        int minY = MathHelper.floor(bb.minY);
        int minZ = MathHelper.floor(bb.minZ);
        int maxX = MathHelper.floor(bb.maxX);
        int maxY = MathHelper.floor(bb.maxY);
        int maxZ = MathHelper.floor(bb.maxZ);

        HazardType found = HazardType.NONE;
        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = minZ; z <= maxZ; z++) {
                    ResourceLocation id = world.getBlockState(new BlockPos(x, y, z)).getBlock().getRegistryName();
                    if (id == null || !MODID.equals(id.getResourceDomain())) continue;

                    String path = id.getResourcePath();
                    if (METAL_SPIKES.equals(path)) {
                        return HazardType.METAL_SPIKES;
                    }
                    if (WOODEN_SPIKES.equals(path) || WOODEN_SPIKES_BLOODED.equals(path) || WOODEN_SPIKES_BROKEN.equals(path)) {
                        found = HazardType.WOODEN_SPIKES;
                        continue;
                    }
                    if (RAZOR_WIRE.equals(path) && found == HazardType.NONE) {
                        found = HazardType.RAZOR_WIRE;
                    }
                }
            }
        }
        return found;
    }

    private enum HazardType {
        NONE,
        WOODEN_SPIKES,
        METAL_SPIKES,
        RAZOR_WIRE
    }
}
