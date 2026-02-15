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
        public final String texMod;
        public final String texBase;
        public final String movementId;
        public final String soundId;

        public DoorSpec(String regPath, String texMod, String texBase,
                        String movementId, String soundId) {
            this.regPath = regPath;
            this.texMod = texMod;
            this.texBase = texBase;
            this.movementId = movementId;
            this.soundId = soundId;
        }
    }

    private static final List<DoorSpec> SPECS = Arrays.asList(
            new DoorSpec("door_bunker", "parasitusfix", "bunker_door",
                    "rotating_door", "vanilla_door"),
            new DoorSpec("door_fir", "parasitusfix", "door_fir",
                    "rotating_door", "vanilla_door"),
            new DoorSpec("door_pine", "parasitusfix", "door_pine",
                    "rotating_door", "vanilla_door"),
            new DoorSpec("door_palm", "parasitusfix", "door_palm",
                    "rotating_door", "vanilla_door"),
            new DoorSpec("door_willow", "parasitusfix", "door_willow",
                    "rotating_door", "vanilla_door"),
            new DoorSpec("door_mangrove", "parasitusfix", "door_mangrove",
                    "rotating_door", "vanilla_door"),
            new DoorSpec("door_redwood", "parasitusfix", "door_redwood",
                    "rotating_door", "vanilla_door"),
            new DoorSpec("door_baobab", "parasitusfix", "door_baobab",
                    "rotating_door", "vanilla_door"),
            new DoorSpec("door_cottonwood", "parasitusfix", "door_cottonwood",
                    "rotating_door", "vanilla_door"),
            new DoorSpec("door_aspen", "parasitusfix", "door_aspen",
                    "rotating_door", "vanilla_door"),
            new DoorSpec("door_maple", "parasitusfix", "door_maple",
                    "rotating_door", "vanilla_door"),
            new DoorSpec("door_juniper", "parasitusfix", "door_juniper",
                    "rotating_door", "vanilla_door"),
            new DoorSpec("door_cherry_blossom", "parasitusfix", "door_cherry_blossom",
                                 "rotating_door", "vanilla_door"),
            new DoorSpec("door_jacaranda", "parasitusfix", "door_jacaranda",
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

            d.setMovement(DoorRegistry.getMovement(s.movementId));
            d.setSound(DoorRegistry.getSound(s.soundId));
            d.setOpeningTime(8);
            d.setDoubleDoor(true);
            d.setProximityDetection(false);
            d.setRedstoneBehavior(DoorDescriptor.RedstoneBehavior.STANDARD);

            d.setTextureName(s.texMod, s.texBase);

            d.create();
            d.register();
        }
    }
}
