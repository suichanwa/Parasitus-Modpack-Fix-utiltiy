package com.toomda.parasitusfix.buildcraft;

import com.toomda.parasitusfix.ParasitusFix;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Iterator;
import java.util.List;

public final class QuartzKinesisPipeCapRemoval {
    private static final String TARGET_PIPE_CLASS = "buildcraft.transport.pipe.PipePowerQuartz";
    private static final int UNLIMITED_TRANSFER = Integer.MAX_VALUE / 2;

    private QuartzKinesisPipeCapRemoval() {}

    public static void apply() {
        try {
            Class<?> pipeClass = Class.forName(TARGET_PIPE_CLASS);
            
            ParasitusFix.getLogger().info("Found BuildCraft PipePowerQuartz class: {}", pipeClass.getName());
            
            Field maxPowerField = findMaxPowerField(pipeClass);
            if (maxPowerField == null) {
                ParasitusFix.getLogger().warn("Could not find power limit field in PipePowerQuartz");
                return;
            }

            Class<?> declaringClass = maxPowerField.getDeclaringClass();
            String fieldName = maxPowerField.getName();
            
            if (!declaringClass.getName().equals(TARGET_PIPE_CLASS)) {
                ParasitusFix.getLogger().warn(
                    "Field '{}' is declared in parent class '{}' instead of '{}'. " +
                    "Modifying it would affect all power pipes! Aborting for safety.",
                    fieldName, declaringClass.getName(), TARGET_PIPE_CLASS
                );
                return;
            }

            maxPowerField.setAccessible(true);
            removeFinal(maxPowerField);
            
            int oldValue = maxPowerField.getInt(null);
            
            if (oldValue != 10240 && oldValue != 1024 && oldValue > 0 && oldValue < 1000000) {
                ParasitusFix.getLogger().warn(
                    "Unexpected power limit value: {} (expected 10240 for Quartz Kinesis Pipe). " +
                    "Proceeding with caution...", oldValue
                );
            }
            
            // Set the new unlimited value
            maxPowerField.setInt(null, UNLIMITED_TRANSFER);
            
            // Verify the change
            int newValue = maxPowerField.getInt(null);
            if (newValue == UNLIMITED_TRANSFER) {
                ParasitusFix.getLogger().info(
                    "Successfully removed Quartz Kinesis Pipe power limit: {} RF/t -> {} RF/t (unlimited)", 
                    oldValue, newValue
                );
            } else {
                ParasitusFix.getLogger().error(
                    "Failed to update power limit! Expected: {}, Got: {}", 
                    UNLIMITED_TRANSFER, newValue
                );
            }
            
        } catch (ClassNotFoundException e) {
            ParasitusFix.getLogger().info(
                "BuildCraft PipePowerQuartz not found - " +
                "this is normal in development environment. Fix will apply in production."
            );
        } catch (Throwable t) {
            ParasitusFix.getLogger().error("Failed to remove Quartz Kinesis Pipe power limit", t);
        }
    }

    private static Field findMaxPowerField(Class<?> pipeClass) {
        String[] possibleNames = {"maxPower", "MAX_POWER", "powerCapacity", "maxPowerFlow"};
        
        for (String name : possibleNames) {
            try {
                Field field = pipeClass.getDeclaredField(name);
                if (field.getType() == int.class || field.getType() == long.class) {
                    ParasitusFix.getLogger().info("Found power field by name: {}", name);
                    return field;
                }
            } catch (NoSuchFieldException ignored) {}
        }
        
        for (Field field : pipeClass.getDeclaredFields()) {
            if (!Modifier.isStatic(field.getModifiers())) continue;
            if (field.getType() != int.class && field.getType() != long.class) continue;
            
            String name = field.getName().toLowerCase();
            if (name.contains("max") || name.contains("capacity") || 
                name.contains("limit") || name.contains("power")) {
                
                ParasitusFix.getLogger().info("Found potential power field: {}", field.getName());
                return field;
            }
        }
        
        return null;
    }

    private static void removeFinal(Field field) throws Exception {
        Field modifiersField = Field.class.getDeclaredField("modifiers");
        modifiersField.setAccessible(true);
        modifiersField.setInt(field, field.getModifiers() & ~Modifier.FINAL);
    }

    @Mod.EventBusSubscriber(modid = ParasitusFix.MODID, value = Side.CLIENT)
    public static class TooltipHandler {
        private static final String QUARTZ_PIPE_ITEM = "buildcrafttransport:pipe_quartz_power";

        @SideOnly(Side.CLIENT)
        @SubscribeEvent(priority = EventPriority.LOW)
        public static void onTooltip(ItemTooltipEvent event) {
            ItemStack stack = event.getItemStack();
            if (stack.isEmpty()) return;

            Item item = stack.getItem();
            ResourceLocation itemId = item.getRegistryName();
            if (itemId == null) return;

            if (QUARTZ_PIPE_ITEM.equals(itemId.toString())) {
                List<String> tooltip = event.getToolTip();
                
                Iterator<String> iterator = tooltip.iterator();
                boolean first = true;
                while (iterator.hasNext()) {
                    String line = iterator.next();
                    if (first) {
                        first = false;
                        continue;
                    }
                    
                    String stripped = line.replaceAll("§.", "").toLowerCase();
                    if (stripped.contains("rf") || 
                        stripped.contains("redstone flux") ||
                        stripped.contains("limit") ||
                        stripped.contains("max") ||
                        stripped.contains("/t") ||
                        stripped.contains("per tick")) {
                        iterator.remove();
                    }
                }

                tooltip.add("No limit in redstone flux sent");
            }
        }
    }
}