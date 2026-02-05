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
        
        private static float lockedYaw = 0;
        private static float lockedPitch = 0;
        private static boolean wasAltHeld = false;
        private static double lastX = 0;
        private static double lastY = 0;

        // Force camera to stay still when Alt is held
        @SubscribeEvent
        public static void onCameraSetup(ViewportEvent.ComputeCameraAngles event) {
            Minecraft mc = Minecraft.getInstance();
            long window = mc.getWindow().getWindow();
            
            boolean altHeld = GLFW.glfwGetKey(window, GLFW.GLFW_KEY_LEFT_ALT) == GLFW.GLFW_PRESS;
            
            if (altHeld && wasAltHeld) { // Only lock if we are successfully tracking state
                 if (mc.hitResult instanceof BlockHitResult blockHit && mc.level.getBlockEntity(blockHit.getBlockPos()) instanceof SteeringWheelBlockEntity) {
                     event.setYaw(lockedYaw);
                     event.setPitch(lockedPitch);
                 }
            }
        }

        @SubscribeEvent
        public static void clientTick(TickEvent.ClientTickEvent event) {
            if (event.phase != TickEvent.Phase.END) return;
            Minecraft mc = Minecraft.getInstance();
            if (mc.player == null) return;

            long window = mc.getWindow().getWindow();
            boolean altHeld = GLFW.glfwGetKey(window, GLFW.GLFW_KEY_LEFT_ALT) == GLFW.GLFW_PRESS;
            
            // Check if looking at wheel
            boolean lookingAtWheel = false;
            if (mc.hitResult instanceof BlockHitResult blockHit && mc.level.getBlockEntity(blockHit.getBlockPos()) instanceof SteeringWheelBlockEntity) {
                lookingAtWheel = true;
            }

            if (altHeld && lookingAtWheel) {
                if (!wasAltHeld) {
                    // Just started holding Alt - lock current angles
                    lockedYaw = mc.player.getYRot(); // Use entity rotation as base
                    lockedPitch = mc.player.getXRot();
                    
                    // Init mouse pos
                    double[] xpos = new double[1];
                    double[] ypos = new double[1];
                    GLFW.glfwGetCursorPos(window, xpos, ypos);
                    lastX = xpos[0];
                    lastY = ypos[0];
                } else {
                    // Holding Alt - read delta
                    double[] xpos = new double[1];
                    double[] ypos = new double[1];
                    GLFW.glfwGetCursorPos(window, xpos, ypos);
                    
                    double dx = xpos[0] - lastX;
                    double dy = ypos[0] - lastY;
                    
                    // Pass to overlay
                    TransmissionOverlay.onMouseInput(dx, dy);
                    
                    lastX = xpos[0];
                    lastY = ypos[0];
                }
            }
            
            wasAltHeld = altHeld && lookingAtWheel;
        }
    }
}
