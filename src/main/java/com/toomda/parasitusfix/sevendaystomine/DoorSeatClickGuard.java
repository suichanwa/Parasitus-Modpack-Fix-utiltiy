package com.toomda.parasitusfix.sevendaystomine;

import net.minecraft.block.Block;
import net.minecraft.block.BlockDoor;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class DoorSeatClickGuard {
    private static final int GUARD_TICKS = 2;
    private static final Map<UUID, Integer> LAST_DOOR_CLICK_TICK = new HashMap<>();

    public DoorSeatClickGuard() {}

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onRightClickBlock(PlayerInteractEvent.RightClickBlock e) {
        World world = e.getWorld();
        if (world.isRemote) return;

        Block block = e.getWorld().getBlockState(e.getPos()).getBlock();
        if (isSeatBlock(block) && isWithinGuardWindow(e.getEntityPlayer(), world)) {
            e.setCancellationResult(EnumActionResult.SUCCESS);
            e.setCanceled(true);
            return;
        }

        if (isDoorBlock(block)) {
            markDoorClick(e.getEntityPlayer(), world);
        }
    }

    private static boolean isWithinGuardWindow(EntityPlayer player, World world) {
        if (player == null || !(world instanceof WorldServer)) return false;
        Integer lastTick = LAST_DOOR_CLICK_TICK.get(player.getUniqueID());
        if (lastTick == null) return false;
        int now = ((WorldServer) world).getMinecraftServer().getTickCounter();
        return now - lastTick <= GUARD_TICKS;
    }

    private static void markDoorClick(EntityPlayer player, World world) {
        if (player == null || !(world instanceof WorldServer)) return;
        int now = ((WorldServer) world).getMinecraftServer().getTickCounter();
        LAST_DOOR_CLICK_TICK.put(player.getUniqueID(), now);
    }

    private static boolean isDoorBlock(Block block) {
        if (block instanceof BlockDoor) return true;
        ResourceLocation id = block.getRegistryName();
        if (id == null) return false;
        return "malisisdoors".equals(id.getResourceDomain()) || id.getResourcePath().contains("door");
    }

    private static boolean isSeatBlock(Block block) {
        ResourceLocation id = block.getRegistryName();
        if (id == null || !"sevendaystomine".equals(id.getResourceDomain())) return false;
        String path = id.getResourcePath();
        if (path.contains("chair") || path.contains("seat")) return true;
        String className = block.getClass().getSimpleName().toLowerCase(java.util.Locale.ROOT);
        return className.contains("chair") || className.contains("seat");
    }
}
