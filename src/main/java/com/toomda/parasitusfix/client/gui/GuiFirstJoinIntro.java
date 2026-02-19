package com.toomda.parasitusfix.client.gui;

import com.toomda.parasitusfix.ParasitusFix;
import java.io.IOException;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;

public class GuiFirstJoinIntro extends GuiScreen {
    private static final int FADE_IN_TICKS = 40;
    private static final int FADE_OUT_TICKS = 16;
    private static final int LETTERBOX_IN_TICKS = 26;
    private static final int LINE_1_START_TICK = 12;
    private static final int LINE_2_GAP_TICKS = 8;

    private int openTicks;
    private int closeTicks;
    private int line2StartTick;
    private int letterboxTargetHeight;
    private boolean closing;

    private String line1;
    private String line2;
    private String visibleLine1 = "";
    private String visibleLine2 = "";
    private int lastVisibleChars1 = -1;
    private int lastVisibleChars2 = -1;

    @Override
    public void initGui() {
        buttonList.clear();
        openTicks = 0;
        closeTicks = 0;
        closing = false;
        line1 = I18n.format("gui." + ParasitusFix.MODID + ".intro.line1");
        line2 = I18n.format("gui." + ParasitusFix.MODID + ".intro.line2");
        line2StartTick = LINE_1_START_TICK + line1.length() + LINE_2_GAP_TICKS;
        letterboxTargetHeight = Math.max(20, Math.min(48, height / 6));
        updateVisibleText();
        int buttonY = (height / 2) + 22;
        buttonList.add(new GuiButton(
                0,
                width / 2 - 60,
                buttonY,
                120,
                20,
                I18n.format("gui." + ParasitusFix.MODID + ".intro.survive")
        ));
    }

    @Override
    public void updateScreen() {
        super.updateScreen();
        if (closing) {
            closeTicks++;
            if (closeTicks >= FADE_OUT_TICKS) {
                mc.displayGuiScreen(null);
                return;
            }
        }
        openTicks++;
        updateVisibleText();
    }

    @Override
    protected void actionPerformed(GuiButton button) {
        if (button.id != 0 || closing) return;
        closing = true;
        closeTicks = 0;
        button.enabled = false;
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        if (keyCode == 1) return;
        super.keyTyped(typedChar, keyCode);
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawRect(0, 0, width, height, 0xFF000000);
        int textY = (height / 2) - 18;

        drawCenteredString(
                fontRenderer,
                visibleLine1,
                width / 2,
                textY,
                0xFFFFFF
        );
        drawCenteredString(
                fontRenderer,
                visibleLine2,
                width / 2,
                textY + 14,
                0xE6E6E6
        );

        super.drawScreen(mouseX, mouseY, partialTicks);
        drawLetterboxBars();
        drawFadeOverlay();
    }

    private void updateVisibleText() {
        int chars1 = visibleChars(line1, LINE_1_START_TICK);
        int chars2 = visibleChars(line2, line2StartTick);

        if (chars1 != lastVisibleChars1) {
            visibleLine1 = line1.substring(0, chars1);
            lastVisibleChars1 = chars1;
        }
        if (chars2 != lastVisibleChars2) {
            visibleLine2 = line2.substring(0, chars2);
            lastVisibleChars2 = chars2;
        }
    }

    private int visibleChars(String text, int startTick) {
        if (text == null || text.isEmpty()) return 0;
        int visible = openTicks - startTick;
        if (visible <= 0) return 0;
        return Math.min(visible, text.length());
    }

    private void drawLetterboxBars() {
        float progress = Math.min(1.0F, openTicks / (float) LETTERBOX_IN_TICKS);
        float inv = 1.0F - progress;
        float eased = 1.0F - (inv * inv * inv);
        int barHeight = (int) (letterboxTargetHeight * eased);
        if (barHeight <= 0) return;

        drawRect(0, 0, width, barHeight, 0xFF000000);
        drawRect(0, height - barHeight, width, height, 0xFF000000);
    }

    private void drawFadeOverlay() {
        int alpha = 0;
        if (openTicks < FADE_IN_TICKS) {
            alpha = Math.max(alpha, 255 - (openTicks * 255 / FADE_IN_TICKS));
        }
        if (closing) {
            alpha = Math.max(alpha, Math.min(255, closeTicks * 255 / FADE_OUT_TICKS));
        }
        if (alpha > 0) drawRect(0, 0, width, height, alpha << 24);    }
}
