package net.koshakz.manualtransmission.client;

import com.mojang.blaze3d.systems.RenderSystem;
import net.koshakz.manualtransmission.content.steering.SteeringWheelBlockEntity;
import net.koshakz.manualtransmission.network.GearShiftPacket;
import net.koshakz.manualtransmission.network.ModPackets;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

public class TransmissionScreen extends Screen {

    private final BlockPos blockPos;
    private float stickX = 0;
    private float stickY = 0;
    private int currentGear = 0;

    public TransmissionScreen(BlockPos pos) {
        super(Component.literal("Transmission"));
        this.blockPos = pos;
    }

    @Override
    protected void init() {
        // Optional: restore previous stick state from BE if possible
        super.init();
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        // No background tint
        
        int size = 100;
        int x = this.width - size - 20;
        int y = this.height - size - 20;

        RenderSystem.enableBlend();
        
        // Draw H-Pattern Background
        graphics.fill(x + 20, y + 10, x + 25, y + 90, 0xCC000000); // 1-2
        graphics.fill(x + 48, y + 10, x + 53, y + 90, 0xCC000000); // 3-4
        graphics.fill(x + 75, y + 10, x + 80, y + 90, 0xCC000000); // 5-6
        graphics.fill(x + 20, y + 45, x + 80, y + 50, 0xCC000000); // N

        // Update Stick Position based on Mouse
        // Map mouse position relative to H-pattern center to stick coordinates (-1 to 1)
        float centerX = x + 50;
        float centerY = y + 50;
        
        // Calculate raw input based on mouse position relative to center
        float rawX = (mouseX - centerX) / 30.0f; 
        float rawY = (mouseY - centerY) / 40.0f;
        
        // Clamp logic
        if (rawX < -1) rawX = -1;
        if (rawX > 1) rawX = 1;
        if (rawY < -1) rawY = -1;
        if (rawY > 1) rawY = 1;
        
        // Snap logic (Magnetic slots)
        boolean inNeutralZone = Math.abs(rawY) < 0.2;
        
        if (!inNeutralZone) {
            // Lock into columns
            if (rawX < -0.5) rawX = -1.0f;
            else if (rawX > 0.5) rawX = 1.0f;
            else rawX = 0.0f;
        } else {
             // In neutral, snap Y to 0
             rawY = 0.0f;
        }
        
        this.stickX = rawX;
        this.stickY = rawY;

        // Draw Stick Knob
        int knobX = x + 50 + (int)(stickX * 28); 
        int knobY = y + 48 + (int)(stickY * 40);
        
        graphics.fill(knobX - 5, knobY - 5, knobX + 5, knobY + 5, 0xFFFF0000); // Red knob when active
        
        // Calculate Gear
        calculateGear();
        
        String gearName = currentGear == 0 ? "N" : String.valueOf(currentGear);
        graphics.drawString(this.font, "Gear: " + gearName, x, y - 20, 0xFFFFFF);
        graphics.drawCenteredString(this.font, "Control Active", x + 50, y - 10, 0x00FF00);

        RenderSystem.disableBlend();
    }

    private void calculateGear() {
        int newGear = 0;
        if (stickY < -0.8) {
            if (stickX < -0.8) newGear = 1;
            else if (stickX > -0.2 && stickX < 0.2) newGear = 3;
            else if (stickX > 0.8) newGear = 5;
        } else if (stickY > 0.8) {
             if (stickX < -0.8) newGear = 2;
            else if (stickX > -0.2 && stickX < 0.2) newGear = 4;
            else if (stickX > 0.8) newGear = 6;
        }
        
        if (newGear != currentGear) {
            currentGear = newGear;
            ModPackets.sendToServer(new GearShiftPacket(blockPos, stickX, stickY));
        }
    }

    @Override
    public void tick() {
        // Close if Alt is released
        long window = Minecraft.getInstance().getWindow().getWindow();
        if (GLFW.glfwGetKey(window, GLFW.GLFW_KEY_LEFT_ALT) != GLFW.GLFW_PRESS) {
            this.onClose();
        }
        
        // Also check if we are still looking at the block (optional, but good for consistency)
        // Actually, if we are in a screen, we shouldn't care where we look, 
        // but let's close if player is too far? Nah, simpler is better.
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
