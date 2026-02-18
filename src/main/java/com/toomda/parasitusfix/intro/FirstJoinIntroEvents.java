package com.toomda.parasitusfix.intro;

import com.toomda.parasitusfix.network.ParasitusFixNetwork;
import com.toomda.parasitusfix.network.PacketOpenFirstJoinIntro;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;

public class FirstJoinIntroEvents {
    @SubscribeEvent
    public void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        EntityPlayer player = event.player;
        if (!(player instanceof EntityPlayerMP) || player.world.isRemote) return;

        EntityPlayerMP playerMp = (EntityPlayerMP) player;
        WorldServer overworld = playerMp.getServerWorld().getMinecraftServer().getWorld(0);
        World dataWorld = overworld != null ? overworld : playerMp.world;

        FirstJoinIntroData data = FirstJoinIntroData.get(dataWorld);
        if (!data.markShown(playerMp.getUniqueID())) return;

        ParasitusFixNetwork.CHANNEL.sendTo(new PacketOpenFirstJoinIntro(), playerMp);
    }
}
