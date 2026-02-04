package com.toomda.parasitusfix.Doors;

import com.toomda.parasitusfix.ParasitusFix;
import net.minecraft.block.Block;
import net.minecraft.block.BlockDoor;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
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
        if (!w.isBlockLoaded(anyHalf)) return false;
        IBlockState s = w.getBlockState(anyHalf);
        if (!(s.getBlock() instanceof BlockDoor)) return false;
        BlockPos bottom = (s.getValue(BlockDoor.HALF) == BlockDoor.EnumDoorHalf.UPPER) ? anyHalf.down() : anyHalf;
        BlockPos top = bottom.up();
        return w.isBlockLoaded(bottom) && w.isBlockLoaded(top)
                && w.getBlockState(bottom).getBlock() instanceof BlockDoor
                && w.getBlockState(top).getBlock()    instanceof BlockDoor;
    }


    private static boolean isSourceDoor(World w, BlockPos p) {
        IBlockState s = w.getBlockState(p);
        ResourceLocation id = Block.REGISTRY.getNameForObject(s.getBlock());
        return id != null && ParasitusDoors.SRC2MD_BLOCK.containsKey(id);
    }

    @SubscribeEvent
    public static void onWorldUnload(net.minecraftforge.event.world.WorldEvent.Unload e) {
        if (e.getWorld().isRemote) return;
        PENDING.remove(e.getWorld());
    }


    @SubscribeEvent
    public static void onPlace(BlockEvent.PlaceEvent e) {
        if (e.getWorld().isRemote) return;
        World w = (World) e.getWorld();
        BlockPos pos = e.getPos();
        if (!isSourceDoor(w, pos)) return;
        if (bothHalvesLoaded(w, pos)) replacePair(w, pos);
        else queue(w).add(pos.toImmutable());
    }

    @SubscribeEvent
    public static void onChunkLoad(ChunkEvent.Load e) {
        if (e.getWorld().isRemote) return;
        World w = e.getWorld();
        Chunk c = e.getChunk();

        BlockPos.MutableBlockPos p = new BlockPos.MutableBlockPos();
        for (int x=0;x<16;x++) for (int z=0;z<16;z++) for (int y=0;y<w.getHeight();y++) {
            p.setPos((c.x<<4)+x, y, (c.z<<4)+z);
            if (isSourceDoor(w, p)) queue(w).add(p.toImmutable());
        }
    }

    @net.minecraftforge.fml.common.eventhandler.SubscribeEvent
    public static void onPopulate(net.minecraftforge.event.terraingen.PopulateChunkEvent.Post e) {
        World w = e.getWorld();
        if (w.isRemote) return;
        int cx = e.getChunkX();
        int cz = e.getChunkZ();

        BlockPos.MutableBlockPos p = new BlockPos.MutableBlockPos();
        for (int x=0;x<16;x++) for (int z=0;z<16;z++) {
            for (int y=0;y<w.getHeight();y++) {
                p.setPos((cx<<4)+x, y, (cz<<4)+z);
                if (isSourceDoor(w, p)) queue(w).add(p.toImmutable());
            }
        }
    }

    @SubscribeEvent
    public static void onWorldTick(net.minecraftforge.fml.common.gameevent.TickEvent.WorldTickEvent e) {
        if (e.phase != net.minecraftforge.fml.common.gameevent.TickEvent.Phase.END) return;
        World w = e.world;
        if (w.isRemote) return;

        Set<BlockPos> q = PENDING.get(w);
        if (q == null || q.isEmpty()) return;

        final int budget = 512;

        // Take a snapshot and clear the live set.
        // Any new adds during replacePair() will go into q safely.
        List<BlockPos> snapshot = new ArrayList<>(q);
        q.clear();

        int processed = 0;
        for (BlockPos pos : snapshot) {
            if (processed >= budget) {
                // not processed yet -> keep for next tick
                q.add(pos);
                continue;
            }

            // If it's no longer a source door, drop it
            if (!isSourceDoor(w, pos)) continue;

            // If the other half isn't loaded yet, re-queue it
            if (!bothHalvesLoaded(w, pos)) {
                q.add(pos);
                continue;
            }

            // Do the swap
            replacePair(w, pos);
            processed++;
        }
    }


    private static void replacePair(World w, BlockPos anyHalf) {
        BlockPos top = anyHalf;
        BlockPos bottom = anyHalf;
        IBlockState s = w.getBlockState(anyHalf);
        if (s.getValue(BlockDoor.HALF) == BlockDoor.EnumDoorHalf.UPPER) bottom = anyHalf.down();
        else top = anyHalf.up();

        IBlockState srcBottom = w.getBlockState(bottom);
        ResourceLocation srcId = Block.REGISTRY.getNameForObject(srcBottom.getBlock());
        Block mdBlock = ParasitusDoors.SRC2MD_BLOCK.get(srcId);
        if (mdBlock == null) return;

        if (w.getBlockState(bottom).getBlock() == mdBlock || w.getBlockState(top).getBlock() == mdBlock) return;

        EnumFacing facing = getFacingFrom(srcBottom);
        BlockDoor.EnumHingePosition hinge = computeHinge(w, bottom, facing, mdBlock);

        IBlockState mdBottom = mapStateToMd(w, bottom, mdBlock, mdBlock.getDefaultState(), w.getBlockState(bottom), false, hinge);
        IBlockState mdTop    = mapStateToMd(w, bottom, mdBlock, mdBlock.getDefaultState(), w.getBlockState(top),    true,  hinge);

        final int SILENT = 2 | 16;
        w.setBlockState(bottom, mdBottom, SILENT);
        w.setBlockState(top,    mdTop,    SILENT);

        TileEntity te = w.getTileEntity(bottom);
        if (te instanceof net.malisis.doors.tileentity.DoorTileEntity) {
            net.malisis.doors.tileentity.DoorTileEntity dte = (net.malisis.doors.tileentity.DoorTileEntity) te;
            dte.setCentered(false);
            boolean isOpen = srcBottom.getValue(BlockDoor.OPEN);
            dte.setDoorState(isOpen ? net.malisis.doors.DoorState.OPENED : net.malisis.doors.DoorState.CLOSED);
            dte.updatePowered();
            dte.markDirty();
        }

        w.notifyNeighborsOfStateChange(bottom, mdBlock, false);
        w.notifyNeighborsOfStateChange(top,    mdBlock, false);
    }

    @SuppressWarnings({"unchecked","rawtypes"})
    private static IBlockState copyPropIfPresent(IBlockState from, IBlockState to, String name) {
        IProperty pFrom = from.getPropertyKeys().stream().filter(p -> p.getName().equals(name)).findFirst().orElse(null);
        IProperty pTo   = to.getPropertyKeys().stream().filter(p -> p.getName().equals(name)).findFirst().orElse(null);
        if (pFrom == null || pTo == null) return to;
        if (!pFrom.getValueClass().equals(pTo.getValueClass())) return to;
        Comparable val = from.getValue(pFrom);
        return to.withProperty(pTo, val);
    }

    private static IBlockState mapStateToMd(
            World w, BlockPos lowerPos, Block mdBlock,
            IBlockState mdDefault, IBlockState src, boolean upper,
            BlockDoor.EnumHingePosition hinge) {
        IBlockState md = mdDefault;

        md = copyPropIfPresent(src, md, "facing");
        md = copyPropIfPresent(src, md, "open");

        md = copyPropIfPresent(src, md, "half");
        for (IProperty<?> p : md.getPropertyKeys()) {
            if (p.getName().equals("half") || p.getName().equals("part")) {
                for (Object o : p.getAllowedValues()) {
                    String n = String.valueOf(o).toLowerCase();
                    boolean isUpper = n.contains("upper") || n.contains("top");
                    boolean isLower = n.contains("lower") || n.contains("bottom");
                    if ((upper && isUpper) || (!upper && isLower)) {
                        md = setProp(md, p, (Comparable) o);
                        break;
                    }
                }
            }
        }

        md = setHinge(md, hinge);
        return md;
    }

    @SuppressWarnings({"rawtypes","unchecked"})
    private static IBlockState setProp(IBlockState state, IProperty prop, Comparable value) {
        return state.withProperty(prop, value);
    }

    private static BlockDoor.EnumHingePosition computeHinge(World w, BlockPos lowerPos, EnumFacing facing, Block mdBlock) {
        IBlockState left = w.getBlockState(leftOf(lowerPos, facing));
        if (isSameMdDoor(left.getBlock(), mdBlock) && sameFacing(left, facing)) {
            return BlockDoor.EnumHingePosition.RIGHT;
        }

        IBlockState right = w.getBlockState(rightOf(lowerPos, facing));
        if (isSameMdDoor(right.getBlock(), mdBlock) && sameFacing(right, facing)) {
            return BlockDoor.EnumHingePosition.LEFT;
        }
        return BlockDoor.EnumHingePosition.LEFT;
    }

    @SuppressWarnings("unchecked")
    private static boolean sameFacing(IBlockState st, EnumFacing facing) {
        for (IProperty<?> p : st.getPropertyKeys()) {
            if (p.getName().equals("facing") && EnumFacing.class.isAssignableFrom(p.getValueClass())) {
                return facing == st.getValue((IProperty<EnumFacing>) p);
            }
        }
        return false;
    }


    private static EnumFacing getFacingFrom(IBlockState src) {
        IProperty<?> p = src.getPropertyKeys().stream()
                .filter(pp -> pp.getName().equals("facing"))
                .findFirst().orElse(null);
        if (p != null && EnumFacing.class.isAssignableFrom(p.getValueClass())) {
            return src.getValue((IProperty<EnumFacing>) p);
        }
        return EnumFacing.NORTH;
    }

    private static IBlockState setHinge(IBlockState state, BlockDoor.EnumHingePosition hinge) {
        for (IProperty<?> p : state.getPropertyKeys()) {
            if (p.getName().equals("hinge") && p.getValueClass() == BlockDoor.EnumHingePosition.class) {
                @SuppressWarnings("unchecked")
                IProperty<BlockDoor.EnumHingePosition> hp = (IProperty<BlockDoor.EnumHingePosition>) p;
                return state.withProperty(hp, hinge);
            }
        }
        return state;
    }

    private static BlockPos rightOf(BlockPos pos, EnumFacing facing) {
        return pos.offset(facing.rotateY());
    }

    private static BlockPos leftOf(BlockPos pos, EnumFacing facing) {
        return pos.offset(facing.rotateYCCW());
    }

    private static boolean isSameMdDoor(Block b, Block mdBlock) {
        return b == mdBlock;
    }

}
