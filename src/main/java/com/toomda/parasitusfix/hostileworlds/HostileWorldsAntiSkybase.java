package com.toomda.parasitusfix.hostileworlds;

import com.toomda.parasitusfix.ParasitusFix;
import com.toomda.parasitusfix.config.ParasitusFixConfig;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

public class HostileWorldsAntiSkybase {
    private static final String NBT_KEY_WARN_COUNT = "parasitusfix:hwSkyWarnCount";
    private static final String NBT_KEY_WAS_ABOVE = "parasitusfix:hwSkyWasAbove";
    private static final String NBT_KEY_PREV_ACTIVE = "parasitusfix:hwInvPrevActive";
    private static final String NBT_KEY_LAST_AIR_WAVE = "parasitusfix:hwInvLastAirWave";
    private static final String NBT_PERSISTED = "PlayerPersisted";

    private static final HwInvApi HW_INV = new HwInvApi();

    @SubscribeEvent
    public void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        EntityPlayer player = event.player;
        if (player == null || player.world.isRemote) return;

        ParasitusFixConfig.HostileWorldsInvasions cfg = ParasitusFixConfig.HOSTILE_WORLDS_INVASIONS;
        NBTTagCompound runtime = player.getEntityData();
        NBTTagCompound persisted = getPersisted(player);
        if (cfg.enableAntiSkybase) handleHeightWarning(player, cfg, runtime, persisted);
        if (cfg.enableAirInvasionFallback) handleAirWaveFallback(player, cfg, runtime, persisted);
    }

    private void handleHeightWarning(EntityPlayer player, ParasitusFixConfig.HostileWorldsInvasions cfg, NBTTagCompound runtime, NBTTagCompound persisted) {
        boolean above = player.posY > cfg.skyYLevel;
        boolean wasAbove = runtime.getBoolean(NBT_KEY_WAS_ABOVE);
        if (!above) {
            if (wasAbove) runtime.setBoolean(NBT_KEY_WAS_ABOVE, false);
            return;
        }
        if (wasAbove) return;

        int shown = persisted.getInteger(NBT_KEY_WARN_COUNT);
        if (shown < cfg.maxWarningMessages) {
            player.sendMessage(new TextComponentString(cfg.warningMessage));
            persisted.setInteger(NBT_KEY_WARN_COUNT, shown + 1);
        }
        runtime.setBoolean(NBT_KEY_WAS_ABOVE, true);
    }

    private void handleAirWaveFallback(EntityPlayer player, ParasitusFixConfig.HostileWorldsInvasions cfg, NBTTagCompound runtime, NBTTagCompound persisted) {
        if (player.dimension != 0 || player.posY <= cfg.skyYLevel) return;

        Object playerData = HW_INV.getPlayerData(player);
        if (playerData == null) return;

        boolean active = HW_INV.getFieldValue(playerData, "dataPlayerInvasionActive", false);
        boolean wasActive = runtime.getBoolean(NBT_KEY_PREV_ACTIVE);
        runtime.setBoolean(NBT_KEY_PREV_ACTIVE, active);
        if (!active) return;

        int wave = HW_INV.getFieldValue(playerData, "lastWaveNumber", -1);
        if (wave <= 0) return;

        if (persisted.getInteger(NBT_KEY_LAST_AIR_WAVE) == wave) return;

        boolean shouldSwitch = false;
        if (!wasActive) {
            int rangeMin = HW_INV.getSpawnRangeMinOr(cfg.scanRangeMinFallback);
            int rangeMax = HW_INV.getSpawnRangeMaxOr(cfg.scanRangeMaxFallback);
            shouldSwitch = !hasAnyGroundOrWaterSpawnSurface(player.world, player.getPosition(), rangeMin, rangeMax, cfg.scanStep);
        }

        if (!shouldSwitch) {
            int triesAny = HW_INV.getFieldValue(playerData, "triesSinceWorkingAnySpawn", 0);
            int triesSolid = HW_INV.getFieldValue(playerData, "triesSinceWorkingSolidGroundSpawn", 0);
            shouldSwitch = triesAny >= cfg.failedSpawnTriesForAirFallback
                    && triesSolid >= cfg.failedSpawnTriesForAirFallback;
        }

        if (!shouldSwitch) return;

        Object template = HW_INV.findTemplate(cfg.airInvasionTemplateName, cfg.airInvasionTemplateFallbackNames);
        if (template == null) {
            ParasitusFix.getLogger().warn(
                    "[HW_INV] Could not find air invasion template '{}' or configured fallbacks.",
                    cfg.airInvasionTemplateName
            );
            return;
        }

        if (!HW_INV.replaceInvasionProfile(playerData, template)) return;

        persisted.setInteger(NBT_KEY_LAST_AIR_WAVE, wave);
        String waveMessage = HW_INV.getTemplateWaveMessage(template);
        if (waveMessage != null && !waveMessage.isEmpty() && !"<NULL>".equals(waveMessage)) {
            player.sendMessage(new TextComponentString(waveMessage));
        }

        ParasitusFix.getLogger().info(
                "[HW_INV] Forced air invasion template '{}' for player '{}' (wave {}, y={}).",
                HW_INV.getTemplateName(template), player.getName(), wave, (int) player.posY
        );
    }

    private static NBTTagCompound getPersisted(EntityPlayer player) {
        NBTTagCompound entityData = player.getEntityData();
        if (!entityData.hasKey(NBT_PERSISTED, 10)) {
            entityData.setTag(NBT_PERSISTED, new NBTTagCompound());
        }
        return entityData.getCompoundTag(NBT_PERSISTED);
    }

    private static boolean hasAnyGroundOrWaterSpawnSurface(World world, BlockPos center, int minRange, int maxRange, int stepRaw) {
        int step = Math.max(1, stepRaw);
        int minSq = Math.max(0, minRange * minRange);
        int maxSq = Math.max(1, maxRange * maxRange);
        BlockPos.MutableBlockPos columnQuery = new BlockPos.MutableBlockPos();
        BlockPos.MutableBlockPos surface = new BlockPos.MutableBlockPos();
        BlockPos.MutableBlockPos scratch = new BlockPos.MutableBlockPos();

        for (int dx = -maxRange; dx <= maxRange; dx += step) {
            for (int dz = -maxRange; dz <= maxRange; dz += step) {
                int distSq = dx * dx + dz * dz;
                if (distSq < minSq || distSq > maxSq) continue;

                int x = center.getX() + dx;
                int z = center.getZ() + dz;
                int y = world.getTopSolidOrLiquidBlock(columnQuery.setPos(x, 0, z)).getY() - 1;
                if (y <= 0) continue;

                surface.setPos(x, y, z);
                IBlockState at = world.getBlockState(surface);

                if (at.getMaterial().blocksMovement()) {
                    scratch.setPos(x, y + 1, z);
                    if (world.isAirBlock(scratch)) {
                        scratch.setPos(x, y + 2, z);
                        if (world.isAirBlock(scratch)) return true;
                    }
                }

                if (at.getMaterial() == Material.WATER) {
                    scratch.setPos(x, y - 1, z);
                    if (world.getBlockState(scratch).getMaterial() == Material.WATER) {
                        scratch.setPos(x, y + 1, z);
                        if (!world.getBlockState(scratch).isTopSolid()) return true;
                    }
                }
            }
        }
        return false;
    }

    private static final class HwInvApi {
        private boolean resolved;
        private boolean available;

        private Capability<?> playerDataCapability;
        private Method methodDifficultyDataGet;
        private Method methodResetInvasion;
        private Method methodInitNewInvasion;
        private Field fieldListMobSpawnTemplates;
        private Field fieldTemplateName;
        private Field fieldTemplateWaveMessage;
        private Field fieldSpawnRangeMin;
        private Field fieldSpawnRangeMax;

        Object getPlayerData(EntityPlayer player) {
            ensureResolved();
            if (!available) return null;
            try {
                @SuppressWarnings("unchecked")
                Capability<Object> cap = (Capability<Object>) playerDataCapability;
                return player.getCapability(cap, null);
            } catch (Throwable t) {
                disable("[HW_INV] Failed to read player invasion capability.", t);
                return null;
            }
        }

        int getSpawnRangeMinOr(int fallback) {
            ensureResolved();
            return getStaticInt(fieldSpawnRangeMin, fallback);
        }

        int getSpawnRangeMaxOr(int fallback) {
            ensureResolved();
            return getStaticInt(fieldSpawnRangeMax, fallback);
        }

        <T> T getFieldValue(Object instance, String fieldName, T fallback) {
            ensureResolved();
            if (!available || instance == null) return fallback;
            try {
                Object value = instance.getClass().getField(fieldName).get(instance);
                if (value == null) return fallback;
                @SuppressWarnings("unchecked")
                T casted = (T) value;
                return casted;
            } catch (Throwable ignored) {
                return fallback;
            }
        }

        Object findTemplate(String primaryName, String[] fallbackNames) {
            ensureResolved();
            if (!available) return null;
            try {
                Object difficultyData = methodDifficultyDataGet.invoke(null);
                @SuppressWarnings("unchecked")
                List<Object> templates = (List<Object>) fieldListMobSpawnTemplates.get(difficultyData);
                Object match = findTemplateByName(templates, primaryName);
                if (match != null) return match;
                if (fallbackNames != null) {
                    for (String fallbackName : fallbackNames) {
                        match = findTemplateByName(templates, fallbackName);
                        if (match != null) return match;
                    }
                }
            } catch (Throwable t) {
                disable("[HW_INV] Failed to read invasion templates.", t);
            }
            return null;
        }

        String getTemplateName(Object template) {
            ensureResolved();
            if (!available || template == null) return "<unknown>";
            try {
                return String.valueOf(fieldTemplateName.get(template));
            } catch (Throwable ignored) {
                return "<unknown>";
            }
        }

        String getTemplateWaveMessage(Object template) {
            ensureResolved();
            if (!available || template == null) return null;
            try {
                Object value = fieldTemplateWaveMessage.get(template);
                return value == null ? null : String.valueOf(value);
            } catch (Throwable ignored) {
                return null;
            }
        }

        boolean replaceInvasionProfile(Object playerData, Object template) {
            ensureResolved();
            if (!available || playerData == null || template == null) return false;
            try {
                methodResetInvasion.invoke(playerData);
                methodInitNewInvasion.invoke(playerData, template);
                return true;
            } catch (Throwable t) {
                disable("[HW_INV] Failed to replace invasion profile.", t);
                return false;
            }
        }

        private void ensureResolved() {
            if (resolved) return;
            resolved = true;
            try {
                Class<?> invasionClass = Class.forName("com.corosus.inv.Invasion");
                Object capabilityObj = invasionClass.getField("PLAYER_DATA_INSTANCE").get(null);
                if (!(capabilityObj instanceof Capability)) {
                    throw new IllegalStateException("Invasion.PLAYER_DATA_INSTANCE is not a Capability");
                }
                playerDataCapability = (Capability<?>) capabilityObj;

                Class<?> playerDataClass = Class.forName("com.corosus.inv.capabilities.PlayerDataInstance");
                methodResetInvasion = playerDataClass.getMethod("resetInvasion");
                methodInitNewInvasion = findInitNewInvasion(playerDataClass);

                Class<?> difficultyDataReaderClass = Class.forName("CoroUtil.difficulty.data.DifficultyDataReader");
                methodDifficultyDataGet = difficultyDataReaderClass.getMethod("getData");
                Object difficultyData = methodDifficultyDataGet.invoke(null);
                fieldListMobSpawnTemplates = difficultyData.getClass().getField("listMobSpawnTemplates");

                Class<?> templateClass = Class.forName("CoroUtil.difficulty.data.spawns.DataMobSpawnsTemplate");
                fieldTemplateName = templateClass.getField("name");
                fieldTemplateWaveMessage = templateClass.getField("wave_message");

                Class<?> cfgAdvClass = Class.forName("com.corosus.inv.config.ConfigAdvancedOptions");
                fieldSpawnRangeMin = cfgAdvClass.getField("spawnRangeMin");
                fieldSpawnRangeMax = cfgAdvClass.getField("spawnRangeMax");

                available = true;
            } catch (Throwable t) {
                disable("[HW_INV] Anti-skybase integration unavailable.", t);
            }
        }

        private static Method findInitNewInvasion(Class<?> playerDataClass) throws NoSuchMethodException {
            for (Method method : playerDataClass.getMethods()) {
                if ("initNewInvasion".equals(method.getName()) && method.getParameterCount() == 1) {
                    return method;
                }
            }
            throw new NoSuchMethodException("PlayerDataInstance.initNewInvasion");
        }

        private Object findTemplateByName(List<Object> templates, String rawName) throws IllegalAccessException {
            if (rawName == null) return null;
            String name = rawName.trim();
            if (name.isEmpty()) return null;
            for (Object template : templates) {
                if (name.equals(String.valueOf(fieldTemplateName.get(template)))) return template;
            }
            return null;
        }

        private int getStaticInt(Field field, int fallback) {
            if (!available || field == null) return fallback;
            try {
                return field.getInt(null);
            } catch (Throwable ignored) {
                return fallback;
            }
        }

        private void disable(String message, Throwable t) {
            available = false;
            ParasitusFix.getLogger().warn(message, t);
        }
    }
}
