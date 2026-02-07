package com.toomda.parasitusfix;

import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

// Disabled for now (bunkerdoor sounds).
// @Mod.EventBusSubscriber(modid = ParasitusFix.MODID)
@Deprecated
public class ParasitusFixSounds {
    @Deprecated
    public static SoundEvent BUNKERDOOR_OPEN;
    @Deprecated
    public static SoundEvent BUNKERDOOR_CLOSE;

    @SubscribeEvent
    @Deprecated
    public static void registerSounds(RegistryEvent.Register<SoundEvent> event) {
        BUNKERDOOR_OPEN = register(event, "bunkerdoor_open");
        BUNKERDOOR_CLOSE = register(event, "bunkerdoor_close");
    }

    @Deprecated
    private static SoundEvent register(RegistryEvent.Register<SoundEvent> event, String name) {
        ResourceLocation id = new ResourceLocation(ParasitusFix.MODID, name);
        SoundEvent sound = new SoundEvent(id).setRegistryName(id);
        event.getRegistry().register(sound);
        return sound;
    }
}
