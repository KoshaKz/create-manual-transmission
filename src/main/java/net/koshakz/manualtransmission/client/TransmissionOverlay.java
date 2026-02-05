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

    // Stick position: -1.0 to 1.0
    private static float stickX = 0; 
    private static float stickY = 0; 
    private static int currentGear = 0; // 0 = Neutral

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
        
        // Draw H-Pattern Background
        // Vertical lines
        graphics.fill(x + 20, y + 10, x + 25, y + 90, 0x80000000); // Gear 1-2
        graphics.fill(x + 48, y + 10, x + 53, y + 90, 0x80000000); // Gear 3-4
        graphics.fill(x + 75, y + 10, x + 80, y + 90, 0x80000000); // Gear 5-6
        // Horizontal line (Neutral)
        graphics.fill(x + 20, y + 45, x + 80, y + 50, 0x80000000);

        long window = mc.getWindow().getWindow();
        boolean altHeld = GLFW.glfwGetKey(window, GLFW.GLFW_KEY_LEFT_ALT) == GLFW.GLFW_PRESS;

        if (altHeld) {
            graphics.drawCenteredString(mc.font, "Mouse Control", x + 50, y - 10, 0xFFFFFF);
            
            // Lock cursor while holding Alt to control stick
            GLFW.glfwSetInputMode(window, GLFW.GLFW_CURSOR, GLFW.GLFW_CURSOR_DISABLED);
        } else {
             // Unlock cursor if it was locked
             if (GLFW.glfwGetInputMode(window, GLFW.GLFW_CURSOR) == GLFW.GLFW_CURSOR_DISABLED) {
                 GLFW.glfwSetInputMode(window, GLFW.GLFW_CURSOR, GLFW.GLFW_CURSOR_NORMAL);
             }
        }

        // Draw Stick Knob
        int knobX = x + 50 + (int)(stickX * 28); 
        int knobY = y + 48 + (int)(stickY * 40);
        
        graphics.fill(knobX - 5, knobY - 5, knobX + 5, knobY + 5, altHeld ? 0xFFFF0000 : 0xFFFFFFFF);
        
        String gearName = currentGear == 0 ? "N" : String.valueOf(currentGear);
        graphics.drawString(mc.font, "Gear: " + gearName, x, y - 20, 0xFFFFFF);

        RenderSystem.disableBlend();
    }
    
    // Called from ClientSetup on ClientTick (END phase)
    public static void handleInput(Minecraft mc) {
        long window = mc.getWindow().getWindow();
        
        // Only process if Alt is held AND we are looking at the block
        if (GLFW.glfwGetKey(window, GLFW.GLFW_KEY_LEFT_ALT) == GLFW.GLFW_PRESS) {
            if (mc.hitResult instanceof BlockHitResult blockHit && mc.level.getBlockEntity(blockHit.getBlockPos()) instanceof SteeringWheelBlockEntity) {
                
                // Get Mouse Delta directly from GLFW
                double[] xpos = new double[1];
                double[] ypos = new double[1];
                // Note: GetCursorPos returns absolute position, delta calculation is tricky without a dedicated event listener.
                // However, since we lock the cursor (CURSOR_DISABLED), the standard Minecraft mouse helper
                // accumulates DX/DY which is applied to the camera. We want to intercept this or just read raw movement.
                
                // Since we disabled the cursor in renderOverlay, Minecraft's MouseHandler might still apply rotation to player view.
                // We actually want to STOP player view rotation.
                
                // Better approach: Read accumulated mouse delta from MouseHandler and reset it?
                // Or simply read raw GLFW state if possible, but GLFW doesn't store delta easily without callback.
                
                // Let's rely on Minecraft's MouseHandler deltas if accessible, or just use a small hack:
                // We can't easily prevent camera movement here without a Mixin.
                // BUT, if the cursor is disabled, MC uses it for camera.
                
                // Let's use a simpler approach for now:
                // We use MouseHandler.xpos() / ypos() if available, but they are protected.
                
                // Let's stick to reading standard mouse input via the MouseHelper.
                double sensitivity = 0.05;
                double dx = mc.mouseHandler.xpos() - mc.mouseHandler.xpos(); // access issues likely
                
                // Okay, without Mixins blocking camera rotation is hard.
                // Let's just try to read the "accumulated" mouse delta from the screen center since we locked it?
                // No, CURSOR_DISABLED re-centers it.
                
                // Let's try this: Just assume the user moves the mouse.
                // Since we can't easily inject a Mouse Listener here without more boilerplate (ClientSetup event),
                // we will check if the user is moving mouse by checking MouseHandler fields via reflection or AT, 
                // OR we just add a MouseInputEvent listener in ClientSetup.
                
                // Waiting for ClientSetup update to pass the event data.
            }
        }
    }
    
    // New method to handle mouse input event directly
    public static void onMouseInput(double deltaX, double deltaY) {
         // Sensitivity
        float speed = 0.05f;
        
        stickX += (float) (deltaX * speed);
        stickY += (float) (deltaY * speed);
        
        // Clamp
        if (stickX < -1) stickX = -1;
        if (stickX > 1) stickX = 1;
        if (stickY < -1) stickY = -1;
        if (stickY > 1) stickY = 1;
        
        // Snap logic (Simulation of H-gate)
        // If moving sideways (X change), push Y towards 0 (Neutral) unless fully in neutral zone
        boolean inNeutralY = Math.abs(stickY) < 0.2;
        
        if (!inNeutralY) {
            // If in gear, resistance to X movement
            // stickX should tend to lock into -1, 0, 1 columns
            if (stickX < -0.5) stickX = -1.0f; // Lock left
            else if (stickX > 0.5) stickX = 1.0f; // Lock right
            else stickX = 0.0f; // Lock center
        }
        
        // Determine Gear
        int newGear = 0;
        // Simple H-pattern logic
        if (stickY < -0.8) { // Top row
            if (stickX < -0.8) newGear = 1;
            else if (stickX > -0.2 && stickX < 0.2) newGear = 3;
            else if (stickX > 0.8) newGear = 5;
        } else if (stickY > 0.8) { // Bottom row
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
