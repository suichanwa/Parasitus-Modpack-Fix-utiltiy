package com.toomda.parasitusfix.sevendaystomine;

import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import nuparu.sevendaystomine.entity.EntityBandit;

public class SevenDaysBanditNoDespawn {
    @SubscribeEvent
    public void onJoinWorld(EntityJoinWorldEvent event) {
        if (event.getWorld().isRemote) return;
        if (event.getEntity() instanceof EntityBandit) {
            ((EntityBandit) event.getEntity()).enablePersistence();
        }
    }
}
