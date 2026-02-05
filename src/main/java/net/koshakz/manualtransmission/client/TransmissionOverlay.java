package net.koshakz.manualtransmission.client;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
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
    
    // Texture location (we will use a simple color rendering for now if texture is missing, or standard widgets)
    // You should create a texture at assets/manualtransmission/textures/gui/h_pattern.png
    private static final ResourceLocation TEXTURE = new ResourceLocation(ManualTransmission.MOD_ID, "textures/gui/h_pattern.png");

    private static float stickX = 0; // -1 to 1
    private static float stickY = 0; // -1 to 1
    private static boolean isDragging = false;
    private static int currentGear = 0; // 0 = Neutral

    public static void renderOverlay(ForgeGui gui, GuiGraphics graphics, float partialTick, int screenWidth, int screenHeight) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) return;

        // Check if looking at Steering Wheel
        HitResult hit = mc.hitResult;
        if (!(hit instanceof BlockHitResult blockHit)) return;
        if (!(mc.level.getBlockEntity(blockHit.getBlockPos()) instanceof SteeringWheelBlockEntity be)) return;

        // Render setup
        int size = 100;
        int x = screenWidth - size - 20;
        int y = screenHeight - size - 20;

        RenderSystem.enableBlend();
        
        // Draw H-Pattern Background (Simple lines for now if no texture)
        // Vertical lines
        graphics.fill(x + 20, y + 10, x + 25, y + 90, 0x80000000); // Gear 1-2
        graphics.fill(x + 48, y + 10, x + 53, y + 90, 0x80000000); // Gear 3-4
        graphics.fill(x + 75, y + 10, x + 80, y + 90, 0x80000000); // Gear 5-6
        // Horizontal line (Neutral)
        graphics.fill(x + 20, y + 45, x + 80, y + 50, 0x80000000);

        // Input Logic (Only if Alt is held)
        long window = mc.getWindow().getWindow();
        boolean altHeld = GLFW.glfwGetKey(window, GLFW.GLFW_KEY_LEFT_ALT) == GLFW.GLFW_PRESS;

        if (altHeld) {
            // Mouse movement simulation (very basic)
            // In a real scenario we'd capture mouse delta. 
            // For now let's just say we visualize the state stored in BE or client static
            // To properly move it we need ClientTickEvent, handled separately.
            
            graphics.drawCenteredString(mc.font, "Control Mode", x + 50, y - 10, 0xFFFFFF);
        }

        // Draw Stick Knob
        int knobX = x + 50 + (int)(stickX * 28); 
        int knobY = y + 48 + (int)(stickY * 40);
        
        graphics.fill(knobX - 5, knobY - 5, knobX + 5, knobY + 5, altHeld ? 0xFFFF0000 : 0xFFFFFFFF);
        
        // Show Gear Number
        String gearName = currentGear == 0 ? "N" : String.valueOf(currentGear);
        graphics.drawString(mc.font, "Gear: " + gearName, x, y - 20, 0xFFFFFF);

        RenderSystem.disableBlend();
    }
    
    // Called from ClientSetup client tick
    public static void handleInput(Minecraft mc) {
        long window = mc.getWindow().getWindow();
        if (GLFW.glfwGetKey(window, GLFW.GLFW_KEY_LEFT_ALT) == GLFW.GLFW_PRESS) {
            if (mc.hitResult instanceof BlockHitResult blockHit && mc.level.getBlockEntity(blockHit.getBlockPos()) instanceof SteeringWheelBlockEntity) {
                // Lock mouse cursor or capture input delta here
                // Simplified: We assume mouse movement affects stickX/stickY
                // This requires a MouseHelper mixin or aggressive event handling normally
                // For simplicity, let's just snap to gears with WASD while ALT is held?
                
                // Let's use WASD for shifting as mouse delta is hard without locking cursor
                boolean w = GLFW.glfwGetKey(window, GLFW.GLFW_KEY_W) == GLFW.GLFW_PRESS;
                boolean s = GLFW.glfwGetKey(window, GLFW.GLFW_KEY_S) == GLFW.GLFW_PRESS;
                boolean a = GLFW.glfwGetKey(window, GLFW.GLFW_KEY_A) == GLFW.GLFW_PRESS;
                boolean d = GLFW.glfwGetKey(window, GLFW.GLFW_KEY_D) == GLFW.GLFW_PRESS;
                
                float speed = 0.1f;
                
                if (a && stickX > -1) stickX -= speed;
                if (d && stickX < 1) stickX += speed;
                
                // Y movement allowed only if aligned with columns (approx)
                boolean inCol1 = stickX < -0.8;
                boolean inCol2 = stickX > -0.2 && stickX < 0.2;
                boolean inCol3 = stickX > 0.8;
                boolean inNeutral = Math.abs(stickY) < 0.2;
                
                if (inNeutral || inCol1 || inCol2 || inCol3) {
                     if (w && stickY > -1) stickY -= speed;
                     if (s && stickY < 1) stickY += speed;
                } else {
                    // Force return to Y=0 if moving sideways
                    stickY *= 0.8;
                }
                
                // Determine Gear
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
                    ModPackets.sendToServer(new GearShiftPacket(blockHit.getBlockPos(), stickX, stickY));
                }
            }
        }
    }
}
