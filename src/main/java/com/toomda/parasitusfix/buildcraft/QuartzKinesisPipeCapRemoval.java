package com.toomda.parasitusfix.buildcraft;

import com.toomda.parasitusfix.ParasitusFix;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

public final class QuartzKinesisPipeCapRemoval {
    private static final String PIPE_CLASS = "buildcraft.transport.pipe.PipePowerQuartz";
    private static final int UNLIMITED_TRANSFER = Integer.MAX_VALUE / 2; // Safe upper limit

    private QuartzKinesisPipeCapRemoval() {}

    public static void apply() {
        try {
            Class<?> pipeClass = Class.forName(PIPE_CLASS);
            
            Field maxPowerField = findMaxPowerField(pipeClass);
            if (maxPowerField == null) {
                ParasitusFix.getLogger().warn("Could not find maxPower field in PipePowerQuartz");
                return;
            }

            maxPowerField.setAccessible(true);
            
            boolean isStatic = Modifier.isStatic(maxPowerField.getModifiers());
            Class<?> declaringClass = maxPowerField.getDeclaringClass();
            
            ParasitusFix.getLogger().info("Found field '{}' in class '{}' (static: {})", 
                maxPowerField.getName(), declaringClass.getName(), isStatic);
            
            if (!declaringClass.getName().equals(PIPE_CLASS)) {
                ParasitusFix.getLogger().warn("Field '{}' is declared in parent class '{}' - this would affect all pipes! Aborting.", 
                    maxPowerField.getName(), declaringClass.getName());
                return;
            }
            
            removeFinal(maxPowerField);
            
            int oldValue = maxPowerField.getInt(null);
            maxPowerField.setInt(null, UNLIMITED_TRANSFER);
            
            ParasitusFix.getLogger().info("Quartz Kinesis Pipe power limit removed: {} -> {}", oldValue, UNLIMITED_TRANSFER);
            
        } catch (ClassNotFoundException e) {
            ParasitusFix.getLogger().warn("BuildCraft PipePowerQuartz class not found - is BuildCraft installed?");
        } catch (Throwable t) {
            ParasitusFix.getLogger().warn("Failed to remove Quartz Kinesis Pipe power limit", t);
        }
    }

    private static Field findMaxPowerField(Class<?> pipeClass) {
        String[] possibleNames = {"maxPower", "MAX_POWER", "powerCapacity", "maxPowerFlow"};
        
        for (String name : possibleNames) {
            try {
                return pipeClass.getDeclaredField(name);
            } catch (NoSuchFieldException ignored) {}
        }
        
        for (Field field : pipeClass.getDeclaredFields()) {
            if (field.getType() == int.class || field.getType() == long.class) {
                String name = field.getName().toLowerCase();
                if (name.contains("max") || name.contains("capacity") || name.contains("limit")) {
                    return field;
                }
            }
        }
        
        return null;
    }

    private static void removeFinal(Field field) throws Exception {
        Field modifiersField = Field.class.getDeclaredField("modifiers");
        modifiersField.setAccessible(true);
        modifiersField.setInt(field, field.getModifiers() & ~Modifier.FINAL);
    }
}