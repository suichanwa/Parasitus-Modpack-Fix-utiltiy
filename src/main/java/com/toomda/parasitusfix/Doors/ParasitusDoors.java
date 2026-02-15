package com.toomda.parasitusfix.Doors;

import com.toomda.parasitusfix.ParasitusFix;
import net.malisis.doors.DoorDescriptor;
import net.malisis.doors.DoorRegistry;
import java.util.Arrays;
import java.util.List;

public class ParasitusDoors {
    private static boolean registered = false;

    public static class DoorSpec {
        public final String regPath;
        public final String texBase;
        public final String movementId;
        public final String soundId;

        public DoorSpec(String regPath, String texBase,
                        String movementId, String soundId) {
            this.regPath = regPath;
            this.texBase = texBase;
            this.movementId = movementId;
            this.soundId = soundId;
        }
    }

    private static final List<DoorSpec> SPECS = Arrays.asList(
            new DoorSpec("door_bunker", "bunker_door",
                    "rotating_door", "vanilla_door"),
            new DoorSpec("door_fir", "door_fir",
                    "rotating_door", "vanilla_door"),
            new DoorSpec("door_pine", "door_pine",
                    "rotating_door", "vanilla_door"),
            new DoorSpec("door_palm", "door_palm",
                    "rotating_door", "vanilla_door"),
            new DoorSpec("door_willow", "door_willow",
                    "rotating_door", "vanilla_door"),
            new DoorSpec("door_mangrove", "door_mangrove",
                    "rotating_door", "vanilla_door"),
            new DoorSpec("door_redwood", "door_redwood",
                    "rotating_door", "vanilla_door"),
            new DoorSpec("door_baobab", "door_baobab",
                    "rotating_door", "vanilla_door"),
            new DoorSpec("door_cottonwood", "door_cottonwood",
                    "rotating_door", "vanilla_door"),
            new DoorSpec("door_aspen", "door_aspen",
                    "rotating_door", "vanilla_door"),
            new DoorSpec("door_maple", "door_maple",
                    "rotating_door", "vanilla_door"),
            new DoorSpec("door_juniper", "door_juniper",
                    "rotating_door", "vanilla_door"),
            new DoorSpec("door_cherry_blossom", "door_cherry_blossom",
                                 "rotating_door", "vanilla_door"),
            new DoorSpec("door_jacaranda", "door_jacaranda",
                    "rotating_door", "vanilla_door")
    );

    public static void registerAll() {
        if (registered) {
            return;
        }
        registered = true;

        for (DoorSpec s : SPECS) {
            DoorDescriptor d = new DoorDescriptor();
            d.setName(s.regPath);
            d.setRegistryName(ParasitusFix.MODID + ":" + s.regPath);
            d.setUnlocalizedName(s.texBase);

            d.setMovement(DoorRegistry.getMovement(s.movementId));
            d.setSound(DoorRegistry.getSound(s.soundId));
            d.setOpeningTime(8);
            d.setDoubleDoor(true);
            d.setProximityDetection(false);
            d.setRedstoneBehavior(DoorDescriptor.RedstoneBehavior.STANDARD);

            d.setTextureName(ParasitusFix.MODID, s.texBase);

            d.create();
            d.register();
        }
    }
}
