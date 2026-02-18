package com.toomda.parasitusfix.network;

import com.toomda.parasitusfix.ParasitusFix;
import io.netty.buffer.ByteBuf;
import net.minecraft.util.IThreadListener;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;

public class PacketOpenFirstJoinIntro implements IMessage {
    @Override
    public void fromBytes(ByteBuf buf) {
        // No payload.
    }

    @Override
    public void toBytes(ByteBuf buf) {
        // No payload.
    }

    public static class Handler implements IMessageHandler<PacketOpenFirstJoinIntro, IMessage> {
        @Override
        public IMessage onMessage(PacketOpenFirstJoinIntro message, MessageContext ctx) {
            if (ctx.side != Side.CLIENT) return null;

            IThreadListener thread = FMLCommonHandler.instance().getWorldThread(ctx.netHandler);
            thread.addScheduledTask(() -> ParasitusFix.proxy.openFirstJoinIntro());
            return null;
        }
    }
}
