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
    private static final Map<UUID, Integer> NEXT_BLEED_TICK = new HashMap<>();
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

        if (!isLimiterEnabled()) return;

        if (!target.isPotionActive(Potions.bleeding)) {
            clearBleedState(target.getUniqueID());
            return;
        }

        int now = ws.getMinecraftServer().getTickCounter();
        UUID id = target.getUniqueID();
        Integer next = NEXT_BLEED_TICK.get(id);
        if (next == null || now >= next) {
            ALLOW_BLEED_TICK.put(id, now);
            target.attackEntityFrom(DamageSources.bleeding, 1.0F);

            int interval = nextIntervalTicks(ws);
            if (interval > 1) {
                NEXT_BLEED_TICK.put(id, now + interval);
            } else {
                NEXT_BLEED_TICK.remove(id);
            }
        }
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

        WorldServer ws = getServerWorld(target);
        if (ws == null) return false;

        if (!isLimiterEnabled()) return false;
        if (!target.isPotionActive(Potions.bleeding)) return false;

        int now = ws.getMinecraftServer().getTickCounter();
        UUID id = target.getUniqueID();
        Integer allowed = ALLOW_BLEED_TICK.get(id);
        if (allowed != null) {
            if (allowed == now) return false;
            if (allowed < now) ALLOW_BLEED_TICK.remove(id);
        }

        return true;
    }

    private static boolean isBleedDamageSource(DamageSource src) {
        if (src == DamageSources.bleeding) return true;
        String t = src.getDamageType();
        if (t == null) return false;
        return t.toLowerCase(Locale.ROOT).contains("bleed");
    }

    private static boolean isLimiterEnabled() {
        if (!isNondeterministicEnabled()) {
            return getDeterministicIntervalTicks() > 1;
        }
        IntervalRange range = getIntervalRange();
        return range.max > 1;
    }

    private static int nextIntervalTicks(WorldServer ws) {
        if (!isNondeterministicEnabled()) return getDeterministicIntervalTicks();
        IntervalRange range = getIntervalRange();
        if (range.max <= 1) return range.max;
        int span = range.max - range.min + 1;
        return range.min + ws.rand.nextInt(span);
    }

    private static int getDeterministicIntervalTicks() {
        return ParasitusFixConfig.BLEEDING.damageIntervalTicks;
    }

    private static boolean isNondeterministicEnabled() {
        return ParasitusFixConfig.BLEEDING.nondeterministicBleedingRate;
    }

    private static IntervalRange getIntervalRange() {
        int min = Math.max(1, ParasitusFixConfig.BLEEDING.minDamageIntervalTicks);
        int max = Math.max(1, ParasitusFixConfig.BLEEDING.maxDamageIntervalTicks);
        if (max < min) max = min;
        return new IntervalRange(min, max);
    }

    private static void clearBleedState(UUID id) {
        NEXT_BLEED_TICK.remove(id);
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

    private static final class IntervalRange {
        final int min;
        final int max;

        private IntervalRange(int min, int max) {
            this.min = min;
            this.max = max;
        }
    }
}
