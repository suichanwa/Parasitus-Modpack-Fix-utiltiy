package com.toomda.parasitusfix.sevendaystomine;

import com.toomda.parasitusfix.ParasitusFix;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.UniversalBucket;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import nuparu.sevendaystomine.init.ModBlocks;
import nuparu.sevendaystomine.tileentity.TileEntityFlamethrower;

import java.util.ArrayList;
import java.util.List;

public class FlamethrowerTrapFuelSwap {
    private static final String OIL_HEAVY_FLUID_NAME = "oil_heavy";
    private static final int SLOT_BUCKET = 0;
    private static Fluid oilHeavy;
    private static boolean oilHeavyResolved;

    private static Fluid getOilHeavy() {
        if (oilHeavyResolved) return oilHeavy;
        oilHeavyResolved = true;
        oilHeavy = FluidRegistry.getFluid(OIL_HEAVY_FLUID_NAME);
        if (oilHeavy == null) {
            oilHeavy = FluidRegistry.getFluid("buildcraftenergy:" + OIL_HEAVY_FLUID_NAME);
        }
        if (oilHeavy == null) {
            ParasitusFix.getLogger().warn("[7DTM] Heavy oil fluid not found; flamethrower fuel swap disabled.");
        }
        return oilHeavy;
    }

    @SubscribeEvent
    public void onWorldLoad(WorldEvent.Load event) {
        if (event.getWorld().isRemote) return;
        getOilHeavy();
    }

    @SubscribeEvent
    public void onWorldTick(TickEvent.WorldTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        World world = event.world;
        if (world.isRemote) return;

        Fluid targetFluid = getOilHeavy();
        if (targetFluid == null) return;

        List<TileEntity> tiles = world.loadedTileEntityList;
        if (tiles.isEmpty()) return;

        for (TileEntity te : new ArrayList<>(tiles)) {
            if (!(te instanceof TileEntityFlamethrower)) continue;
            tryFillWithOil((TileEntityFlamethrower) te, targetFluid);
        }
    }

    private void tryFillWithOil(TileEntityFlamethrower flame, Fluid targetFluid) {
        FluidTank tank = flame.getTank();
        if (tank == null) return;

        FluidStack current = tank.getFluid();
        Fluid currentFluid = current == null ? null : current.getFluid();
        int currentAmount = current == null ? 0 : current.amount;

        if (currentAmount > 0 && currentFluid != targetFluid) {
            return;
        }

        IItemHandler handler = flame.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null);
        if (!(handler instanceof IItemHandlerModifiable)) return;

        ItemStack stack = handler.getStackInSlot(SLOT_BUCKET);
        if (stack.isEmpty()) return;

        Item item = stack.getItem();
        if (!(item instanceof UniversalBucket)) return;

        FluidStack bucketFluid = ((UniversalBucket) item).getFluid(stack);
        if (bucketFluid == null || bucketFluid.getFluid() != targetFluid) return;

        int capacity = tank.getCapacity();
        int free = capacity - currentAmount;
        if (bucketFluid.amount > free) return;

        int newAmount = currentAmount + bucketFluid.amount;
        tank.setFluid(new FluidStack(targetFluid, newAmount));

        ((IItemHandlerModifiable) handler).setStackInSlot(SLOT_BUCKET, new ItemStack(Items.BUCKET));
        flame.markDirty();
        flame.getWorld().notifyNeighborsOfStateChange(flame.getPos(), ModBlocks.FLAMETHOWER, false);
    }
}
