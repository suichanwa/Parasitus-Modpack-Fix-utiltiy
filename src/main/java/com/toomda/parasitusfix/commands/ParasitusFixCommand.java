package com.toomda.parasitusfix.commands;

import com.toomda.parasitusfix.ParasitusFix;
import net.minecraft.block.Block;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.item.Item;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

import javax.annotation.Nullable;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class ParasitusFixCommand extends CommandBase {

    @Override
    public String getName() {
        return "parasitusfix";
    }

    @Override
    public List<String> getAliases() {
        return Arrays.asList("pfix", "pfx");
    }

    @Override
    public String getUsage(ICommandSender sender) {
        return "/parasitusfix <subcommand> [args]\n" +
               "  info - Show mod status\n" +
               "  buildcraft - Check BuildCraft items and blocks";
    }

    @Override
    public int getRequiredPermissionLevel() {
        return 2;
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        if (args.length == 0) {
            sender.sendMessage(new TextComponentString("§6ParasitusFix Commands:"));
            sender.sendMessage(new TextComponentString("§7/parasitusfix info - Show mod status"));
            sender.sendMessage(new TextComponentString("§7/parasitusfix buildcraft - Check BuildCraft items"));
            return;
        }

        if (args[0].equalsIgnoreCase("info")) {
            sender.sendMessage(new TextComponentString("§6ParasitusFix v" + ParasitusFix.VERSION));
            sender.sendMessage(new TextComponentString("§7Active fixes loaded"));
            return;
        }

        if (args[0].equalsIgnoreCase("buildcraft")) {
            if (!Loader.isModLoaded("buildcrafttransport")) {
                sender.sendMessage(new TextComponentString("§cBuildCraft Transport is NOT loaded!"));
                return;
            }

            sender.sendMessage(new TextComponentString("§aBuildCraft Transport is loaded!"));
            sender.sendMessage(new TextComponentString("§6--- BuildCraft Items ---"));

            // List all BuildCraft items
            int count = 0;
            for (Item item : ForgeRegistries.ITEMS.getValuesCollection()) {
                ResourceLocation id = item.getRegistryName();
                if (id != null && id.getResourceDomain().contains("buildcraft")) {
                    sender.sendMessage(new TextComponentString("§7  " + id));
                    
                    // If it's a pipe item, show more info
                    if (id.getResourcePath().contains("pipe")) {
                        sender.sendMessage(new TextComponentString("§e    Class: " + item.getClass().getName()));
                        
                        // Try to find methods
                        try {
                            for (Method m : item.getClass().getDeclaredMethods()) {
                                if (m.getName().contains("pipe") || m.getName().contains("Pipe")) {
                                    sender.sendMessage(new TextComponentString("§e      Method: " + m.getName()));
                                }
                            }
                        } catch (Exception e) {
                            // Ignore
                        }
                    }
                    count++;
                }
            }
            
            sender.sendMessage(new TextComponentString("§7Found " + count + " BuildCraft items"));

            // List all BuildCraft blocks
            sender.sendMessage(new TextComponentString("§6--- BuildCraft Blocks ---"));
            count = 0;
            for (Block block : ForgeRegistries.BLOCKS.getValuesCollection()) {
                ResourceLocation id = block.getRegistryName();
                if (id != null && id.getResourceDomain().contains("buildcraft")) {
                    if (id.getResourcePath().contains("pipe")) {
                        sender.sendMessage(new TextComponentString("§7  " + id));
                        sender.sendMessage(new TextComponentString("§e    Class: " + block.getClass().getName()));
                        count++;
                    }
                }
            }
            
            sender.sendMessage(new TextComponentString("§7Found " + count + " BuildCraft pipe blocks"));

            // Try to inspect the quartz pipe specifically
            ResourceLocation quartzPipe = new ResourceLocation("buildcrafttransport", "pipe_quartz_power");
            Item quartzItem = ForgeRegistries.ITEMS.getValue(quartzPipe);
            
            if (quartzItem != null) {
                sender.sendMessage(new TextComponentString("§6--- Quartz Kinesis Pipe Details ---"));
                sender.sendMessage(new TextComponentString("§7Item class: " + quartzItem.getClass().getName()));
                
                // Try to get all fields from the item
                Class<?> itemClass = quartzItem.getClass();
                sender.sendMessage(new TextComponentString("§7Item fields:"));
                for (Field field : itemClass.getDeclaredFields()) {
                    try {
                        field.setAccessible(true);
                        Object value = field.get(quartzItem);
                        if (value != null) {
                            sender.sendMessage(new TextComponentString("§e  " + field.getName() + ": " + 
                                value.getClass().getSimpleName()));
                            
                            // If it's a pipe definition, inspect it
                            if (field.getName().toLowerCase().contains("pipe") || 
                                field.getName().toLowerCase().contains("definition")) {
                                inspectObject(sender, value, "    ");
                            }
                        }
                    } catch (Exception e) {
                        // Ignore
                    }
                }
            }
            
            return;
        }

        sender.sendMessage(new TextComponentString("§cUnknown command. Use /parasitusfix for help."));
    }

    private void inspectObject(ICommandSender sender, Object obj, String indent) {
        if (obj == null) return;
        
        Class<?> clazz = obj.getClass();
        sender.sendMessage(new TextComponentString("§e" + indent + "Object class: " + clazz.getName()));
        
        // Show all int/long fields that might be power limits
        for (Field field : clazz.getDeclaredFields()) {
            if (field.getType() == int.class || field.getType() == long.class) {
                try {
                    field.setAccessible(true);
                    Object value = field.get(obj);
                    sender.sendMessage(new TextComponentString("§a" + indent + "  " + 
                        field.getName() + " = " + value));
                } catch (Exception e) {
                    // Ignore
                }
            }
        }
        
        sender.sendMessage(new TextComponentString("§7" + indent + "Methods:"));
        for (Method method : clazz.getDeclaredMethods()) {
            if (method.getName().contains("create") || method.getName().contains("make") || 
                method.getName().contains("pipe") || method.getName().contains("Pipe")) {
                String params = "";
                for (Class<?> param : method.getParameterTypes()) {
                    params += param.getSimpleName() + ", ";
                }
                if (params.length() > 0) params = params.substring(0, params.length() - 2);
                
                sender.sendMessage(new TextComponentString("§7" + indent + "  " + 
                    method.getName() + "(" + params + ")"));
            }
        }
    }

    @Override
    public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, 
                                          String[] args, @Nullable BlockPos targetPos) {
        if (args.length == 1) {
            return getListOfStringsMatchingLastWord(args, "info", "buildcraft");
        }
        return Collections.emptyList();
    }
}
