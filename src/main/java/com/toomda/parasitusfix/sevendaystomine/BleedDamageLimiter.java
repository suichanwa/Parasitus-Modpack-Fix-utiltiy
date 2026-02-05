package com.toomda.parasitusfix.sevendaystomine;

import com.toomda.parasitusfix.ParasitusFix;
import com.toomda.parasitusfix.config.ParasitusFixConfig;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.DamageSource;
import net.minecraft.world.WorldServer;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import nuparu.sevendaystomine.potions.Potions;
import nuparu.sevendaystomine.util.DamageSources;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

@Mod.EventBusSubscriber(modid = ParasitusFix.MODID)
public final class BleedDamageLimiter {
    private static final String MODID = "sevendaystomine";
    private static final Map<UUID, Integer> LAST_BLEED_TICK = new HashMap<>();
    private static final Map<UUID, Integer> ALLOW_BLEED_TICK = new HashMap<>();

    private BleedDamageLimiter() {}

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onBleedAttack(LivingAttackEvent e) {
        if (shouldCancelBleedEvent(e.getEntityLiving(), e.getSource(), e.getAmount(), e.isCanceled())) {
            e.setCanceled(true);
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onBleedHurt(LivingHurtEvent e) {
        if (shouldCancelBleedEvent(e.getEntityLiving(), e.getSource(), e.getAmount(), e.isCanceled())) {
            e.setCanceled(true);
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onBleedDamage(LivingDamageEvent e) {
        if (shouldCancelBleedEvent(e.getEntityLiving(), e.getSource(), e.getAmount(), e.isCanceled())) {
            e.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onBleedTick(LivingEvent.LivingUpdateEvent e) {
        if (!isModLoaded()) return;

        EntityLivingBase target = e.getEntityLiving();
        WorldServer ws = getServerWorld(target);
        if (ws == null || !(target instanceof EntityPlayer)) return;

        int interval = getIntervalTicks();
        if (interval <= 1) return;

        if (!target.isPotionActive(Potions.bleeding)) {
            clearBleedState(target.getUniqueID());
            return;
        }

        int now = ws.getMinecraftServer().getTickCounter();
        UUID id = target.getUniqueID();
        Integer last = LAST_BLEED_TICK.get(id);
        if (last == null) {
            LAST_BLEED_TICK.put(id, now);
            return;
        }
        if (now - last < interval) return;

        LAST_BLEED_TICK.put(id, now);
        ALLOW_BLEED_TICK.put(id, now);
        target.attackEntityFrom(DamageSources.bleeding, 1.0F);
    }

    @SubscribeEvent
    public static void onDeath(LivingDeathEvent e) {
        EntityLivingBase target = e.getEntityLiving();
        if (target instanceof EntityPlayer) {
            clearBleedState(target.getUniqueID());
        }
    }

    private static boolean shouldCancelBleedEvent(EntityLivingBase target, DamageSource src, float amount, boolean canceled) {
        if (!shouldProcessDamageEvent(amount, canceled)) return false;
        if (!(target instanceof EntityPlayer)) return false;
        if (src == null || !isBleedDamageSource(src)) return false;
        if (!target.isPotionActive(Potions.bleeding)) return false;

        WorldServer ws = getServerWorld(target);
        if (ws == null) return false;

        int interval = getIntervalTicks();
        if (interval <= 1) return false;

        int now = ws.getMinecraftServer().getTickCounter();
        UUID id = target.getUniqueID();
        Integer allowed = ALLOW_BLEED_TICK.get(id);
        if (allowed != null && allowed == now) {
            ALLOW_BLEED_TICK.remove(id);
            return false;
        }

        return true;
    }

    private static boolean isBleedDamageSource(DamageSource src) {
        if (src == DamageSources.bleeding) return true;
        String t = src.getDamageType();
        if (t == null) return false;
        return t.toLowerCase(Locale.ROOT).contains("bleed");
    }

    private static int getIntervalTicks() {
        return ParasitusFixConfig.BLEEDING.damageIntervalTicks;
    }

    private static void clearBleedState(UUID id) {
        LAST_BLEED_TICK.remove(id);
        ALLOW_BLEED_TICK.remove(id);
    }

    private static WorldServer getServerWorld(EntityLivingBase target) {
        if (target == null) return null;
        if (target.world.isRemote) return null;
        if (!(target.world instanceof WorldServer)) return null;
        return (WorldServer) target.world;
    }

    private static boolean shouldProcessDamageEvent(float amount, boolean canceled) {
        return isModLoaded() && !canceled && amount > 0;
    }

    private static boolean isModLoaded() {
        return Loader.isModLoaded(MODID);
    }
}
