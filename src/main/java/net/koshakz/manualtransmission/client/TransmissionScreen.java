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
        super.init();
        
        // Sync state from BlockEntity when opening
        if (Minecraft.getInstance().level.getBlockEntity(blockPos) instanceof SteeringWheelBlockEntity be) {
            this.stickX = be.gearX;
            this.stickY = be.gearY;
            
            // Recalculate gear just in case
            calculateGear();
        }
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        // No dark background
        
        int size = 100;
        int x = this.width - size - 20;
        int y = this.height - size - 20;

        RenderSystem.enableBlend();
        
        // Draw H-Pattern Background
        graphics.fill(x + 20, y + 10, x + 25, y + 90, 0xCC000000); // 1-2
        graphics.fill(x + 48, y + 10, x + 53, y + 90, 0xCC000000); // 3-4
        graphics.fill(x + 75, y + 10, x + 80, y + 90, 0xCC000000); // 5-6
        graphics.fill(x + 20, y + 45, x + 80, y + 50, 0xCC000000); // N

        // Determine Center
        float centerX = x + 50;
        float centerY = y + 50;
        
        // --- INPUT LOGIC START ---
        
        // Map mouse to -1..1 range
        float rangeX = 30.0f;
        float rangeY = 40.0f;
        
        // Target is where the mouse IS
        float targetX = (mouseX - centerX) / rangeX;
        float targetY = (mouseY - centerY) / rangeY;
        
        // H-Pattern Constraint Logic
        // We act like a physical object inside rails
        
        // 1. Clamp to box
        if (targetX < -1) targetX = -1;
        if (targetX > 1) targetX = 1;
        if (targetY < -1) targetY = -1;
        if (targetY > 1) targetY = 1;
        
        // 2. Logic:
        // You can always move along Y if you are aligned with a column.
        // You can always move along X if you are in Neutral (Y ~ 0).
        
        boolean inNeutralY = Math.abs(targetY) < 0.15f; 
        
        // Columns X centers: -1, 0, 1. (Actually we render them at: 22.5, 50.5, 77.5 relative to 100px box)
        // Normalized X cols are roughly: -0.9, 0.0, 0.9 based on render code 
        // Render code: 20-25 (Left), 48-53 (Center), 75-80 (Right) -> Centers: 22.5, 50.5, 77.5
        // Box width 100. Center 50.
        // Left col X: (22.5 - 50) / 28 = -0.98
        // Center col X: 0
        // Right col X: (77.5 - 50) / 28 = +0.98
        
        // We use stickX * 28 for render. So max range is roughly -1 to 1.
        
        float colThreshold = 0.3f; // Width of slot
        boolean inLeftCol = Math.abs(stickX - (-1.0f)) < colThreshold;
        boolean inCenterCol = Math.abs(stickX - 0.0f) < colThreshold;
        boolean inRightCol = Math.abs(stickX - 1.0f) < colThreshold;
        
        // If we try to move Y out of neutral, we must be in a column
        if (!inNeutralY) {
            // We are trying to be in gear (Top or Bottom)
            // Force X to snap to nearest column
            if (Math.abs(targetX - (-1.0f)) < colThreshold) targetX = -1.0f;
            else if (Math.abs(targetX - 0.0f) < colThreshold) targetX = 0.0f;
            else if (Math.abs(targetX - 1.0f) < colThreshold) targetX = 1.0f;
            else {
                // Not in a column, force Y back to 0 (cannot enter gear)
                targetY = 0.0f;
                // Allow free X movement since we are now forced to Y=0
            }
        }
        
        // Apply
        this.stickX = targetX;
        this.stickY = targetY;
        
        // --- INPUT LOGIC END ---

        // Draw Stick Knob
        int knobX = x + 50 + (int)(stickX * 28); 
        int knobY = y + 48 + (int)(stickY * 40);
        
        graphics.fill(knobX - 5, knobY - 5, knobX + 5, knobY + 5, 0xFFFF0000); 
        
        calculateGear();
        
        String gearName = currentGear == 0 ? "N" : String.valueOf(currentGear);
        graphics.drawString(this.font, "Gear: " + gearName, x, y - 20, 0xFFFFFF);
        graphics.drawCenteredString(this.font, "ACTIVE", x + 50, y - 10, 0x00FF00);

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
        long window = Minecraft.getInstance().getWindow().getWindow();
        if (GLFW.glfwGetKey(window, GLFW.GLFW_KEY_LEFT_ALT) != GLFW.GLFW_PRESS) {
            this.onClose();
        }
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
