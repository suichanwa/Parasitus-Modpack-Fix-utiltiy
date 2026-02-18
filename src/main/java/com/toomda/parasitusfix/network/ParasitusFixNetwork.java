package com.toomda.parasitusfix.network;

import com.toomda.parasitusfix.ParasitusFix;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;

public final class ParasitusFixNetwork {
    public static final SimpleNetworkWrapper CHANNEL = NetworkRegistry.INSTANCE.newSimpleChannel(ParasitusFix.MODID);

    private ParasitusFixNetwork() {}

    public static void init() {
        CHANNEL.registerMessage(PacketOpenFirstJoinIntro.Handler.class, PacketOpenFirstJoinIntro.class, 0, Side.CLIENT);
    }
}
