package com.toomda.parasitusfix.sevendaystomine;

import com.toomda.parasitusfix.ParasitusFix;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.furnace.FurnaceFuelBurnTimeEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class SevenDaysCoalBurnTimeFix {
    private static final int COAL_BURN_TICKS = 1600;
    private static final int COAL_BLOCK_BURN_TICKS = 16000;
    private static final int CHARCOAL_BURN_TICKS = 1600;
    private static final int CHARCOAL_BLOCK_BURN_TICKS = 16000;

    private static final ResourceLocation SDTM_COAL_ID = new ResourceLocation("sevendaystomine", "coal");
    private static final ResourceLocation SDTM_CHARCOAL_ID = new ResourceLocation("sevendaystomine", "charcoal");
    private static final ResourceLocation SDTM_CHARCOAL_BLOCK_ID = new ResourceLocation("sevendaystomine", "charcoal_block");

    public SevenDaysCoalBurnTimeFix() {
        ParasitusFix.getLogger().info("[7DTM] Coal burn-time fix active (coal/charcoal=1600, coal blocks=16000)");
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onFurnaceFuelBurnTime(FurnaceFuelBurnTimeEvent event) {
        ItemStack stack = event.getItemStack();
        if (stack.isEmpty()) return;

        int fixedBurnTime = getFixedBurnTime(stack);
        if (fixedBurnTime > 0) {
            event.setBurnTime(fixedBurnTime);
        }
    }

    private int getFixedBurnTime(ItemStack stack) {
        Item item = stack.getItem();

        if (item == Items.COAL) {
            // metadata 0 = coal, 1 = charcoal
            return stack.getMetadata() == 1 ? CHARCOAL_BURN_TICKS : COAL_BURN_TICKS;
        }

        if (item == Item.getItemFromBlock(Blocks.COAL_BLOCK)) {
            return COAL_BLOCK_BURN_TICKS;
        }

        ResourceLocation registryName = item.getRegistryName();
        if (registryName == null) {
            return 0;
        }

        if (registryName.equals(SDTM_COAL_ID)) {
            return COAL_BURN_TICKS;
        }
        if (registryName.equals(SDTM_CHARCOAL_ID)) {
            return CHARCOAL_BURN_TICKS;
        }
        if (registryName.equals(SDTM_CHARCOAL_BLOCK_ID)) {
            return CHARCOAL_BLOCK_BURN_TICKS;
        }

        return 0;
    }
}
