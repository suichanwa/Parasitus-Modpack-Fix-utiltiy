package com.toomda.parasitusfix.Doors;

import com.toomda.parasitusfix.ParasitusFix;
import com.toomda.parasitusfix.config.ParasitusFixConfig;
import net.minecraft.block.Block;
import net.minecraft.block.BlockDoor;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.event.world.ChunkEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.*;

@Mod.EventBusSubscriber(modid = ParasitusFix.MODID)
public class DoorSwap {
    private static final Map<World, Set<BlockPos>> PENDING = new HashMap<>();

    private static Set<BlockPos> queue(World w) {
        return PENDING.computeIfAbsent(w, k -> new HashSet<>());
    }

    private static boolean bothHalvesLoaded(World w, BlockPos anyHalf) {
        IBlockState state = w.getBlockState(anyHalf);
        if (!(state.getBlock() instanceof BlockDoor)) return false;

        BlockDoor.EnumDoorHalf half = state.getValue(BlockDoor.HALF);
        BlockPos other = half == BlockDoor.EnumDoorHalf.LOWER 
            ? anyHalf.up() 
            : anyHalf.down();

        return w.isBlockLoaded(other);
    }

    private static boolean isSourceDoor(World w, BlockPos p) {
        Block block = w.getBlockState(p).getBlock();
        ResourceLocation id = block.getRegistryName();
        return id != null && ParasitusDoors.SRC2MD_BLOCK.containsKey(id);
    }

    @SubscribeEvent
    public static void onWorldUnload(net.minecraftforge.event.world.WorldEvent.Unload e) {
        PENDING.remove(e.getWorld());
    }

    @SubscribeEvent
    public static void onPlace(BlockEvent.EntityPlaceEvent e) {
        if (!ParasitusFixConfig.DOORS.enablePlayerPlacementSwap) {
            return;
        }

        World w = e.getWorld();
        if (w.isRemote) return;

        Block block = e.getPlacedBlock().getBlock();
        ResourceLocation id = block.getRegistryName();
        if (id == null) return;

        Block replacement = ParasitusDoors.SRC2MD_BLOCK.get(id);
        if (replacement == null) return;

        queue(w).add(e.getPos());
    }

    @SubscribeEvent
    public static void onChunkLoad(ChunkEvent.Load e) {
        // Only scan existing chunks if world replacement is enabled
        if (!ParasitusFixConfig.DOORS.enableWorldDoorReplacement) {
            return;
        }

        World w = e.getWorld();
        if (w.isRemote) return;

        Chunk chunk = e.getChunk();
        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                for (int y = 0; y < 256; y++) {
                    BlockPos pos = new BlockPos(chunk.x * 16 + x, y, chunk.z * 16 + z);
                    if (isSourceDoor(w, pos)) {
                        queue(w).add(pos);
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public static void onPopulate(net.minecraftforge.event.terraingen.PopulateChunkEvent.Post e) {
        // Only scan newly generated chunks if world replacement is enabled
        if (!ParasitusFixConfig.DOORS.enableWorldDoorReplacement) {
            return;
        }

        World w = e.getWorld();
        if (w.isRemote) return;

        int chunkX = e.getChunkX() * 16;
        int chunkZ = e.getChunkZ() * 16;

        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                for (int y = 0; y < 256; y++) {
                    BlockPos pos = new BlockPos(chunkX + x, y, chunkZ + z);
                    if (isSourceDoor(w, pos)) {
                        queue(w).add(pos);
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public static void onWorldTick(net.minecraftforge.fml.common.gameevent.TickEvent.WorldTickEvent e) {
        if (e.phase != net.minecraftforge.fml.common.gameevent.TickEvent.Phase.END) return;
        if (e.world.isRemote) return;

        Set<BlockPos> pending = queue(e.world);
        if (pending.isEmpty()) return;

        Iterator<BlockPos> it = pending.iterator();
        int processed = 0;
        while (it.hasNext() && processed < 50) {
            BlockPos pos = it.next();
            it.remove();

            if (bothHalvesLoaded(e.world, pos) && isSourceDoor(e.world, pos)) {
                replacePair(e.world, pos);
            }
            processed++;
        }
    }

    @SubscribeEvent
    public static void onHarvest(BlockEvent.HarvestDropsEvent e) {
        // Check config before swapping drops
        if (!ParasitusFixConfig.DOORS.enableDropSwap) {
            return;
        }

        World w = e.getWorld();
        if (w.isRemote) return;

        Block block = e.getState().getBlock();
        Item correctItem = ParasitusDoors.MD2SRC_ITEM.get(block);
        if (correctItem == null) return;

        e.getDrops().clear();
        e.getDrops().add(new ItemStack(correctItem, 1));
    }

    private static void replacePair(World w, BlockPos anyHalf) {
        IBlockState state = w.getBlockState(anyHalf);
        Block srcBlock = state.getBlock();
        
        ResourceLocation srcId = srcBlock.getRegistryName();
        if (srcId == null) return;

        Block replacement = ParasitusDoors.SRC2MD_BLOCK.get(srcId);
        if (replacement == null) return;

        BlockDoor.EnumDoorHalf half = state.getValue(BlockDoor.HALF);
        BlockPos lower = half == BlockDoor.EnumDoorHalf.LOWER ? anyHalf : anyHalf.down();
        BlockPos upper = lower.up();

        IBlockState lowerState = w.getBlockState(lower);
        IBlockState upperState = w.getBlockState(upper);

        if (lowerState.getBlock() != srcBlock || upperState.getBlock() != srcBlock) {
            return;
        }

        IBlockState newLower = replacement.getDefaultState();
        IBlockState newUpper = replacement.getDefaultState();

        for (IProperty<?> prop : lowerState.getPropertyKeys()) {
            if (newLower.getPropertyKeys().contains(prop)) {
                newLower = copyProperty(lowerState, newLower, prop);
            }
        }

        for (IProperty<?> prop : upperState.getPropertyKeys()) {
            if (newUpper.getPropertyKeys().contains(prop)) {
                newUpper = copyProperty(upperState, newUpper, prop);
            }
        }

        w.setBlockState(lower, newLower, 3);
        w.setBlockState(upper, newUpper, 3);
    }

    @SuppressWarnings("unchecked")
    private static <T extends Comparable<T>> IBlockState copyProperty(
            IBlockState from, IBlockState to, IProperty<?> prop) {
        IProperty<T> p = (IProperty<T>) prop;
        return to.withProperty(p, from.getValue(p));
    }
}