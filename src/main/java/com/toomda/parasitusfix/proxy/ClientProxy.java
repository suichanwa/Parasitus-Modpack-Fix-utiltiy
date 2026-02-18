package com.toomda.parasitusfix.proxy;

import com.toomda.parasitusfix.client.gui.GuiFirstJoinIntro;
import net.minecraft.client.Minecraft;

public class ClientProxy extends CommonProxy {
    @Override
    public void openFirstJoinIntro() {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc == null || mc.world == null || mc.player == null) return;
        mc.displayGuiScreen(new GuiFirstJoinIntro());
    }
}
