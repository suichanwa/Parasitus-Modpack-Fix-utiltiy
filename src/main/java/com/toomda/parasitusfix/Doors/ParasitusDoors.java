package com.toomda.parasitusfix.Doors;

import com.toomda.parasitusfix.ParasitusFix;
import net.malisis.doors.DoorDescriptor;
import net.minecraft.block.Block;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.malisis.doors.DoorRegistry;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

import java.util.*;

public class ParasitusDoors {
    public static final Map<ResourceLocation, Block> SRC2MD_BLOCK = new HashMap<>();
    public static final Map<Block, Item> MD2SRC_ITEM = new HashMap<>();
    public static class DoorSpec {
        public final ResourceLocation sourceId;
        public final String regPath;
        public final String texMod;
        public final String texBase;
        public final String movementId;
        public final String soundId;

        public DoorSpec(String srcMod, String srcPath,
                        String regPath, String texMod, String texBase,
                        String movementId, String soundId) {
            this.sourceId = new ResourceLocation(srcMod, srcPath);
            this.regPath = regPath;
            this.texMod = texMod;
            this.texBase = texBase;
            this.movementId = movementId;
            this.soundId = soundId;
        }
    }
    private static final List<DoorSpec> SPECS = Arrays.asList(
            new DoorSpec("pvj", "fir_door",
                    "door_fir", "parasitusfix", "door_fir",
                    "rotating_door", "vanilla_door"),
            new DoorSpec("pvj", "pine_door",
                    "door_pine", "parasitusfix", "door_pine",
                    "rotating_door", "vanilla_door"),
            new DoorSpec("pvj", "palm_door",
                    "door_palm", "parasitusfix", "door_palm",
                    "rotating_door", "vanilla_door"),
            new DoorSpec("pvj", "willow_door",
                    "door_willow", "parasitusfix", "door_willow",
                    "rotating_door", "vanilla_door"),
            new DoorSpec("pvj", "mangrove_door",
                    "door_mangrove", "parasitusfix", "door_mangrove",
                    "rotating_door", "vanilla_door"),
            new DoorSpec("pvj", "redwood_door",
                    "door_redwood", "parasitusfix", "door_redwood",
                    "rotating_door", "vanilla_door"),
            new DoorSpec("pvj", "baobab_door",
                    "door_baobab", "parasitusfix", "door_baobab",
                    "rotating_door", "vanilla_door"),
            new DoorSpec("pvj", "cottonwood_door",
                    "door_cottonwood", "parasitusfix", "door_cottonwood",
                    "rotating_door", "vanilla_door"),
            new DoorSpec("pvj", "aspen_door",
                    "door_aspen", "parasitusfix", "door_aspen",
                    "rotating_door", "vanilla_door"),
            new DoorSpec("pvj", "maple_door",
                    "door_maple", "parasitusfix", "door_maple",
                    "rotating_door", "vanilla_door"),
            new DoorSpec("pvj", "juniper_door",
                    "door_juniper", "parasitusfix", "door_juniper",
                    "rotating_door", "vanilla_door"),
            new DoorSpec("pvj", "cherry_blossom_door",
                                 "door_cherry_blossom", "parasitusfix", "door_cherry_blossom",
                                 "rotating_door", "vanilla_door"),
            new DoorSpec("pvj", "jacaranda_door",
                    "door_jacaranda", "parasitusfix", "door_jacaranda",
                    "rotating_door", "vanilla_door")
    );

    public static void registerAll() {
        for (DoorSpec s : SPECS) {
            DoorDescriptor d = new DoorDescriptor();
            d.setName(s.regPath);
            d.setRegistryName(ParasitusFix.MODID + ":" + s.regPath);

            d.setMovement(DoorRegistry.getMovement(s.movementId));
            d.setSound(DoorRegistry.getSound(s.soundId));
            d.setOpeningTime(8);
            d.setDoubleDoor(true);
            d.setProximityDetection(false);
            d.setRedstoneBehavior(DoorDescriptor.RedstoneBehavior.STANDARD);

            d.setTextureName(s.texMod, s.texBase);

            d.create();
            d.register();

            Block mdBlock = d.getBlock();
            SRC2MD_BLOCK.put(s.sourceId, mdBlock);

            Block srcBlock = ForgeRegistries.BLOCKS.getValue(s.sourceId);
            Item srcItem = srcBlock != null ? Item.getItemFromBlock(srcBlock) : Items.AIR;
            if (srcItem != null && srcItem != Items.AIR) {
                MD2SRC_ITEM.put(mdBlock, srcItem);
            }
        }
    }
}

