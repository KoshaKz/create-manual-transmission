package net.koshakz.manualtransmission.client;

import net.koshakz.manualtransmission.ManualTransmission;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
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
        
        // Use MouseInputEvent or raw Mouse Move event if available to capture delta
        @SubscribeEvent
        public static void onMouseInput(InputEvent.MouseScrollingEvent event) {
            // Scrolling is not what we want, we want movement.
            // Unfortunately Forge doesn't have a direct "MouseMoved" event that gives delta easily without mixins or access transformers.
            // HOWEVER, we can calculate delta manually in ClientTick if we know previous position.
        }
        
        // Let's use ClientTick to read mouse changes if we can't get event
        @SubscribeEvent
        public static void clientTick(TickEvent.ClientTickEvent event) {
            if (event.phase == TickEvent.Phase.END && Minecraft.getInstance().player != null) {
                Minecraft mc = Minecraft.getInstance();
                long window = mc.getWindow().getWindow();
                
                // Only if Alt is held
                if (GLFW.glfwGetKey(window, GLFW.GLFW_KEY_LEFT_ALT) == GLFW.GLFW_PRESS) {
                    double[] xpos = new double[1];
                    double[] ypos = new double[1];
                    GLFW.glfwGetCursorPos(window, xpos, ypos);
                    
                    double dx = xpos[0] - lastX;
                    double dy = ypos[0] - lastY;
                    
                    // If cursor is disabled, GLFW resets position to center usually? No, it just hides it.
                    // But if we locked it, we might need to rely on the fact that we can read accumulation.
                    // Actually, if we use GLFW_CURSOR_DISABLED, we get raw deltas via GetCursorPos relative to... infinite?
                    // No, GLFW standard behavior: disabled cursor provides unlimited virtual cursor motion.
                    // BUT Minecraft resets the cursor position every frame when it handles mouse look.
                    // So reading GetCursorPos might just give us what Minecraft reset it to + movement.
                    
                    // Since implementing proper mouse delta capture without interfering with MC is hard:
                    // We will assume the user uses the mouse purely for shifting when ALT is held.
                    // TransmissionOverlay.onMouseInput(dx, dy); 
                    
                    // Temporary: Revert to WASD logic in overlay handleInput called here, 
                    // OR try to use the raw dx/dy from Minecraft's MouseHandler via AT if we could.
                    // Since we can't do ATs easily right now:
                    
                    // Let's rely on the fact that when cursor is locked, MC accumulates into MouseHandler.
                    // We can try to read MouseHandler.xpos/ypos via reflection if needed, but let's try the Overlay's internal logic first.
                    
                    TransmissionOverlay.handleInput(mc); 
                    
                    lastX = xpos[0];
                    lastY = ypos[0];
                }
            }
        }
        
        private static double lastX = 0;
        private static double lastY = 0;
    }
}
