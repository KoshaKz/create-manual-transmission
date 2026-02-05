package net.koshakz.manualtransmission.client;

import com.mojang.blaze3d.systems.RenderSystem;
import net.koshakz.manualtransmission.ManualTransmission;
import net.koshakz.manualtransmission.content.steering.SteeringWheelBlockEntity;
import net.koshakz.manualtransmission.network.GearShiftPacket;
import net.koshakz.manualtransmission.network.ModPackets;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.client.gui.overlay.ForgeGui;
import net.minecraftforge.client.gui.overlay.IGuiOverlay;
import org.lwjgl.glfw.GLFW;

public class TransmissionOverlay {

    public static final IGuiOverlay OVERLAY = TransmissionOverlay::renderOverlay;
    
    private static final ResourceLocation TEXTURE = new ResourceLocation(ManualTransmission.MOD_ID, "textures/gui/h_pattern.png");

    private static float stickX = 0; 
    private static float stickY = 0; 
    private static int currentGear = 0;

    public static void renderOverlay(ForgeGui gui, GuiGraphics graphics, float partialTick, int screenWidth, int screenHeight) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) return;

        HitResult hit = mc.hitResult;
        if (!(hit instanceof BlockHitResult blockHit)) return;
        if (!(mc.level.getBlockEntity(blockHit.getBlockPos()) instanceof SteeringWheelBlockEntity be)) return;

        int size = 100;
        int x = screenWidth - size - 20;
        int y = screenHeight - size - 20;

        RenderSystem.enableBlend();
        
        // H-Pattern lines
        graphics.fill(x + 20, y + 10, x + 25, y + 90, 0x80000000); // 1-2
        graphics.fill(x + 48, y + 10, x + 53, y + 90, 0x80000000); // 3-4
        graphics.fill(x + 75, y + 10, x + 80, y + 90, 0x80000000); // 5-6
        graphics.fill(x + 20, y + 45, x + 80, y + 50, 0x80000000); // N

        long window = mc.getWindow().getWindow();
        boolean altHeld = GLFW.glfwGetKey(window, GLFW.GLFW_KEY_LEFT_ALT) == GLFW.GLFW_PRESS;

        if (altHeld) {
            graphics.drawCenteredString(mc.font, "Mouse Control", x + 50, y - 10, 0xFFFFFF);
        }

        // Draw Stick
        int knobX = x + 50 + (int)(stickX * 28); 
        int knobY = y + 48 + (int)(stickY * 40);
        
        graphics.fill(knobX - 5, knobY - 5, knobX + 5, knobY + 5, altHeld ? 0xFFFF0000 : 0xFFFFFFFF);
        
        String gearName = currentGear == 0 ? "N" : String.valueOf(currentGear);
        graphics.drawString(mc.font, "Gear: " + gearName, x, y - 20, 0xFFFFFF);

        RenderSystem.disableBlend();
    }
    
    public static void handleInput(Minecraft mc) {}
    
    public static void onMouseInput(double deltaX, double deltaY) {
        // Reduced sensitivity significantly for smoother control (was 0.05f)
        float speed = 0.005f;
        
        stickX += (float) (deltaX * speed);
        stickY += (float) (deltaY * speed);
        
        if (stickX < -1) stickX = -1;
        if (stickX > 1) stickX = 1;
        if (stickY < -1) stickY = -1;
        if (stickY > 1) stickY = 1;
        
        // Snap logic
        boolean inNeutralY = Math.abs(stickY) < 0.2;
        
        if (!inNeutralY) {
            if (stickX < -0.5) stickX = -1.0f;
            else if (stickX > 0.5) stickX = 1.0f;
            else stickX = 0.0f;
        }
        
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
            Minecraft mc = Minecraft.getInstance();
            if (mc.hitResult instanceof BlockHitResult blockHit) {
                 ModPackets.sendToServer(new GearShiftPacket(blockHit.getBlockPos(), stickX, stickY));
            }
        }
    }
}
