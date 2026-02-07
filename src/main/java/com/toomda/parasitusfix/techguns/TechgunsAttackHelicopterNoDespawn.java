package com.toomda.parasitusfix.techguns;

import com.toomda.parasitusfix.ParasitusFix;
import net.minecraft.entity.EntityLiving;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import techguns.entities.npcs.AttackHelicopter;

@Mod.EventBusSubscriber(modid = ParasitusFix.MODID)
public class TechgunsAttackHelicopterNoDespawn {
    @SubscribeEvent
    public static void onJoinWorld(EntityJoinWorldEvent event) {
        if (event.getWorld().isRemote) return;
        if (event.getEntity() instanceof AttackHelicopter) {
            ((EntityLiving) event.getEntity()).enablePersistence();
        }
    }
}
