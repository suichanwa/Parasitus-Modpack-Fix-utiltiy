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
    private static final int FADE_IN_TICKS = 40;
    private static final int LETTERBOX_IN_TICKS = 26;
    private static final int LINE_1_START_TICK = 12;
    private static final int LINE_2_GAP_TICKS = 8;

    private int openTicks;

    @Override
    public void initGui() {
        buttonList.clear();
        openTicks = 0;
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
    public void updateScreen() {
        super.updateScreen();
        openTicks++;
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

        String line1 = I18n.format("gui." + ParasitusFix.MODID + ".intro.line1");
        String line2 = I18n.format("gui." + ParasitusFix.MODID + ".intro.line2");
        int line2StartTick = LINE_1_START_TICK + line1.length() + LINE_2_GAP_TICKS;

        drawCenteredString(
                fontRenderer,
                line1.substring(0, visibleChars(line1, LINE_1_START_TICK)),
                width / 2,
                top + 126,
                0xFFFFFF
        );
        drawCenteredString(
                fontRenderer,
                line2.substring(0, visibleChars(line2, line2StartTick)),
                width / 2,
                top + 140,
                0xE6E6E6
        );

        super.drawScreen(mouseX, mouseY, partialTicks);
        drawLetterboxBars();
        drawFadeOverlay();
    }

    private int visibleChars(String text, int startTick) {
        if (text == null || text.isEmpty()) return 0;
        int visible = openTicks - startTick;
        if (visible <= 0) return 0;
        return Math.min(visible, text.length());
    }

    private void drawLetterboxBars() {
        float progress = Math.min(1.0F, openTicks / (float) LETTERBOX_IN_TICKS);
        float eased = 1.0F - (float) Math.pow(1.0F - progress, 3.0F);
        int targetHeight = Math.max(20, Math.min(48, height / 6));
        int barHeight = (int) (targetHeight * eased);
        if (barHeight <= 0) return;

        drawRect(0, 0, width, barHeight, 0xFF000000);
        drawRect(0, height - barHeight, width, height, 0xFF000000);
    }

    private void drawFadeOverlay() {
        if (openTicks >= FADE_IN_TICKS) return;
        int alpha = 255 - (openTicks * 255 / FADE_IN_TICKS);
        drawRect(0, 0, width, height, alpha << 24);
    }
}
