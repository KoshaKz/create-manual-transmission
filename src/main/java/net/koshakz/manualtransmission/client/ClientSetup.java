package net.koshakz.manualtransmission.client;

import net.koshakz.manualtransmission.ManualTransmission;
import net.koshakz.manualtransmission.content.steering.SteeringWheelBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
import net.minecraftforge.client.event.ViewportEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.glfw.GLFW;

@Mod.EventBusSubscriber(modid = ManualTransmission.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ClientSetup {

    @SubscribeEvent
    public static void registerOverlays(RegisterGuiOverlaysEvent event) {
        event.registerAboveAll("transmission", TransmissionOverlay.OVERLAY);
    }
    
    @Mod.EventBusSubscriber(modid = ManualTransmission.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
    public static class ForgeEvents {
        
        // Block camera rotation when controlling gear shift
        @SubscribeEvent
        public static void onCameraSetup(ViewportEvent.ComputeCameraAngles event) {
            Minecraft mc = Minecraft.getInstance();
            long window = mc.getWindow().getWindow();
            
            if (GLFW.glfwGetKey(window, GLFW.GLFW_KEY_LEFT_ALT) == GLFW.GLFW_PRESS) {
                if (mc.hitResult instanceof BlockHitResult blockHit && mc.level.getBlockEntity(blockHit.getBlockPos()) instanceof SteeringWheelBlockEntity) {
                    // Lock camera to previous values
                    event.setYaw(event.getYaw());
                    event.setPitch(event.getPitch());
                    // Unfortunately setYaw/Pitch here affects rendering but not the player's actual rotation for next frame
                    // To truly stop rotation we need to cancel mouse input processing in MouseHandler, which requires Mixin.
                    // But we can try to counteract it or just rely on the fact that we consume the input?
                }
            }
        }

        // Capture raw mouse movement before it rotates the player
        @SubscribeEvent
        public static void onMouseInput(InputEvent.MouseScrollingEvent event) {
             // Not useful for XY movement
        }

        private static double lastX = 0;
        private static double lastY = 0;
        private static boolean wasAltHeld = false;

        @SubscribeEvent
        public static void clientTick(TickEvent.ClientTickEvent event) {
            if (event.phase == TickEvent.Phase.END && Minecraft.getInstance().player != null) {
                Minecraft mc = Minecraft.getInstance();
                long window = mc.getWindow().getWindow();
                
                boolean altHeld = GLFW.glfwGetKey(window, GLFW.GLFW_KEY_LEFT_ALT) == GLFW.GLFW_PRESS;
                
                if (altHeld) {
                    if (mc.hitResult instanceof BlockHitResult blockHit && mc.level.getBlockEntity(blockHit.getBlockPos()) instanceof SteeringWheelBlockEntity) {
                        
                        // Capture mouse delta
                        double[] xpos = new double[1];
                        double[] ypos = new double[1];
                        GLFW.glfwGetCursorPos(window, xpos, ypos);
                        
                        if (wasAltHeld) { // Skip first frame to avoid jump
                            double dx = xpos[0] - lastX;
                            double dy = ypos[0] - lastY;
                            
                            // Send delta to overlay
                            TransmissionOverlay.onMouseInput(dx, dy);
                            
                            // RESET cursor to center to prevent hitting screen edges
                            // This also helps "consume" the movement so camera doesn't spin as much?
                            // Actually, forcing cursor pos might cause jitter in camera if not careful.
                            // But usually re-centering is the standard way to implement custom mouse look/control.
                            
                            // Let's try NOT resetting and see if just reading delta works. 
                            // If camera spins, we might need to rely on the user stopping camera manually or live with it.
                            // BUT wait, if we use standard cursor mode (not disabled), camera stops!
                            
                            // STRATEGY: 
                            // 1. Unlock cursor (make it visible) -> Camera stops moving.
                            // 2. Read delta of the visible cursor.
                            // 3. Re-center cursor so it doesn't leave window.
                            
                            if (mc.mouseHandler.isMouseGrabbed()) {
                                mc.mouseHandler.releaseMouse(); // Shows cursor, STOPS camera rotation
                            }
                            
                            // Re-center logic
                            int centerX = mc.getWindow().getScreenWidth() / 2;
                            int centerY = mc.getWindow().getScreenHeight() / 2;
                            
                            // Calculate delta from center (since we reset to center last frame)
                            double deltaX = xpos[0] - centerX;
                            double deltaY = ypos[0] - centerY;
                            
                            if (Math.abs(deltaX) > 0 || Math.abs(deltaY) > 0) {
                                TransmissionOverlay.onMouseInput(deltaX, deltaY);
                                // Reset to center
                                GLFW.glfwSetCursorPos(window, centerX, centerY);
                                lastX = centerX;
                                lastY = centerY;
                                return; // Skip updating lastX/Y with raw values
                            }
                        }
                        
                        lastX = xpos[0];
                        lastY = ypos[0];
                    }
                } else {
                    // Re-grab mouse if we released it
                    if (wasAltHeld && !mc.mouseHandler.isMouseGrabbed() && mc.screen == null) {
                        mc.mouseHandler.grabMouse();
                    }
                }
                
                wasAltHeld = altHeld;
            }
        }
    }
}
