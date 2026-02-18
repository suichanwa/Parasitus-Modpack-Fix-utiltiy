package com.toomda.parasitusfix.client.gui;

import com.toomda.parasitusfix.ParasitusFix;
import java.io.IOException;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.ResourceLocation;

public class GuiFirstJoinIntro extends GuiScreen {
    private static final ResourceLocation INTRO_IMAGE =
            new ResourceLocation("minecraft", "textures/gui/title/background/panorama_0.png");

    @Override
    public void initGui() {
        buttonList.clear();
        int panelTop = (height - 210) / 2;
        buttonList.add(new GuiButton(
                0,
                width / 2 - 60,
                panelTop + 176,
                120,
                20,
                I18n.format("gui." + ParasitusFix.MODID + ".intro.survive")
        ));
    }

    @Override
    protected void actionPerformed(GuiButton button) {
        if (button.id == 0) mc.displayGuiScreen(null);
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        if (keyCode == 1) return;
        super.keyTyped(typedChar, keyCode);
    }

    @Override
    public boolean doesGuiPauseGame() {
        return true;
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawDefaultBackground();

        int panelWidth = Math.min(360, width - 32);
        int panelHeight = 210;
        int left = (width - panelWidth) / 2;
        int top = (height - panelHeight) / 2;
        int imageX = left + 12;
        int imageY = top + 12;
        int imageWidth = panelWidth - 24;
        int imageHeight = 104;

        drawRect(left - 2, top - 2, left + panelWidth + 2, top + panelHeight + 2, 0xB0000000);
        drawRect(left, top, left + panelWidth, top + panelHeight, 0xD0181818);

        mc.getTextureManager().bindTexture(INTRO_IMAGE);
        drawModalRectWithCustomSizedTexture(imageX, imageY, 0.0F, 0.0F, imageWidth, imageHeight, 256.0F, 256.0F);

        drawCenteredString(
                fontRenderer,
                I18n.format("gui." + ParasitusFix.MODID + ".intro.line1"),
                width / 2,
                top + 126,
                0xFFFFFF
        );
        drawCenteredString(
                fontRenderer,
                I18n.format("gui." + ParasitusFix.MODID + ".intro.line2"),
                width / 2,
                top + 140,
                0xE6E6E6
        );

        super.drawScreen(mouseX, mouseY, partialTicks);
    }
}
