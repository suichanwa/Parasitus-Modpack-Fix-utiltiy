package com.toomda.parasitusfix.general;

import com.toomda.parasitusfix.ParasitusFix;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.color.BlockColors;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.ColorHandlerEvent;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * Makes Dynamic Trees (superior-trees) leaves change color based on OverLast seasons.
 * OverLast normally only changes vanilla leaves, so we need to hook into its season system
 * and apply the same tinting to Dynamic Trees leaves.
 * 
 * This uses block color handlers to tint leaves based on the current season.
 */
@Mod.EventBusSubscriber(modid = ParasitusFix.MODID, value = Side.CLIENT)
public class DynamicTreesSeasonalLeaves {
    
    private static final String MOD_OVERLAST = "overlast";
    private static final String DYNAMIC_LEAVES_CLASS = "com.ferreusveritas.dynamictrees.blocks.BlockDynamicLeaves";
    
    private static boolean initialized = false;
    private static Object worldSeasonInstance = null;
    private static Method getSeasonMethod = null;
    private static Class<?> dynamicLeavesBlockClass = null;
    private static boolean dynamicLeavesClassResolved = false;
    private static int currentSeason = -1;
    private static int tickCounter = 0;
    
    // Season enum values from OverLast
    private static final int SEASON_SPRING = 0;
    private static final int SEASON_SUMMER = 1;
    private static final int SEASON_AUTUMN = 2;
    private static final int SEASON_WINTER = 3;
    
    // Color tints for each season (ARGB format)
    // These colors match the textures you have: red for autumn, blue for winter
    private static final int COLOR_SPRING = 0xFFFFFFFF; // White (no tint, default green)
    private static final int COLOR_SUMMER = 0xFFFFFFFF; // White (no tint, default green)
    private static final int COLOR_AUTUMN = 0xFFFF6B6B; // Reddish tint
    private static final int COLOR_WINTER = 0xFF87CEEB; // Sky blue tint
    
    /**
     * Initialize the reflection hooks into OverLast's season system
     */
    private static void initialize() {
        if (initialized) return;
        initialized = true;
        
        if (!Loader.isModLoaded(MOD_OVERLAST)) {
            ParasitusFix.getLogger().info("[DynamicTreesSeasons] OverLast not loaded, seasonal leaves disabled");
            return;
        }

        resolveDynamicLeavesClass();
        if (dynamicLeavesBlockClass == null) {
            ParasitusFix.getLogger().info("[DynamicTreesSeasons] Dynamic Trees leaves class not found, seasonal leaves disabled");
            return;
        }
        
        try {
            // Access OverLast's WorldSeason class
            Class<?> worldSeasonClass = Class.forName("com.overlast.season.WorldSeason");
            Field instanceField = worldSeasonClass.getDeclaredField("instance");
            instanceField.setAccessible(true);
            worldSeasonInstance = instanceField.get(null);
            
            // Get the method to retrieve current season
            getSeasonMethod = worldSeasonClass.getDeclaredMethod("getSeason");
            getSeasonMethod.setAccessible(true);
            
            ParasitusFix.getLogger().info("[DynamicTreesSeasons] Successfully hooked into OverLast season system");
        } catch (Exception e) {
            ParasitusFix.getLogger().error("[DynamicTreesSeasons] Failed to hook into OverLast: " + e.getMessage());
            worldSeasonInstance = null;
        }
    }

    private static void resolveDynamicLeavesClass() {
        if (dynamicLeavesClassResolved) {
            return;
        }

        dynamicLeavesClassResolved = true;
        try {
            dynamicLeavesBlockClass = Class.forName(DYNAMIC_LEAVES_CLASS);
        } catch (ClassNotFoundException e) {
            dynamicLeavesBlockClass = null;
        }
    }

    private static boolean isDynamicLeavesBlock(Block block) {
        resolveDynamicLeavesClass();

        if (dynamicLeavesBlockClass != null && dynamicLeavesBlockClass.isAssignableFrom(block.getClass())) {
            return true;
        }

        // Fallback for forks/addons that may not expose the same class hierarchy.
        ResourceLocation blockName = block.getRegistryName();
        if (blockName == null) {
            return false;
        }

        String domain = blockName.getResourceDomain();
        String path = blockName.getResourcePath();
        return path.contains("leaves")
            && (domain.contains("dynamictree") || domain.contains("dramatic") || domain.contains("superior"));
    }
    
    /**
     * Get the current season from OverLast
     * @return season index (0-3) or -1 if unavailable
     */
    private static int getCurrentSeason() {
        if (worldSeasonInstance == null || getSeasonMethod == null) {
            return -1;
        }
        
        try {
            Object seasonEnum = getSeasonMethod.invoke(worldSeasonInstance);
            if (seasonEnum != null) {
                return ((Enum<?>) seasonEnum).ordinal();
            }
        } catch (Exception e) {
            ParasitusFix.getLogger().error("[DynamicTreesSeasons] Error getting season: " + e.getMessage());
        }
        
        return -1;
    }
    
    /**
     * Get the color tint for the current season
     * @param season Season index
     * @return ARGB color value
     */
    private static int getSeasonColor(int season) {
        switch (season) {
            case SEASON_AUTUMN:
                return COLOR_AUTUMN;
            case SEASON_WINTER:
                return COLOR_WINTER;
            case SEASON_SPRING:
                return COLOR_SPRING;
            case SEASON_SUMMER:
                return COLOR_SUMMER;
            default:
                return 0xFFFFFFFF; // White (no tint)
        }
    }
    
    /**
     * Check for season changes
     */
    @SubscribeEvent
    @SideOnly(Side.CLIENT)
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        
        // Initialize on first tick
        if (!initialized) {
            initialize();
        }
        
        // Only check every 20 ticks (1 second)
        tickCounter++;
        if (tickCounter < 20) return;
        tickCounter = 0;
        
        // Check if season has changed
        int newSeason = getCurrentSeason();
        if (newSeason == -1) return;
        
        if (newSeason != currentSeason) {
            currentSeason = newSeason;
            ParasitusFix.getLogger().info("[DynamicTreesSeasons] Season changed to: " + getSeasonName(currentSeason));
            
            // Trigger a world render update
            Minecraft mc = Minecraft.getMinecraft();
            if (mc.world != null && mc.renderGlobal != null) {
                mc.renderGlobal.loadRenderers();
            }
        }
    }
    
    /**
     * Register color handlers for Dynamic Trees leaves
     */
    @SubscribeEvent
    @SideOnly(Side.CLIENT)
    public static void onBlockColors(ColorHandlerEvent.Block event) {
        if (!Loader.isModLoaded(MOD_OVERLAST)) {
            return;
        }
        
        BlockColors blockColors = event.getBlockColors();
        
        // Register color handler for all Dynamic Trees leaf blocks (including addon leaves).
        for (Block block : ForgeRegistries.BLOCKS) {
            if (isDynamicLeavesBlock(block)) {
                blockColors.registerBlockColorHandler(
                    (state, worldIn, pos, tintIndex) -> {
                        int season = getCurrentSeason();
                        return getSeasonColor(season);
                    },
                    block
                );
                ParasitusFix.getLogger().info("[DynamicTreesSeasons] Registered color handler for: " + block.getRegistryName());
            }
        }
    }
    
    /**
     * Get the name of a season for logging
     */
    private static String getSeasonName(int season) {
        switch (season) {
            case SEASON_SPRING: return "Spring";
            case SEASON_SUMMER: return "Summer";
            case SEASON_AUTUMN: return "Autumn";
            case SEASON_WINTER: return "Winter";
            default: return "Unknown";
        }
    }
}
