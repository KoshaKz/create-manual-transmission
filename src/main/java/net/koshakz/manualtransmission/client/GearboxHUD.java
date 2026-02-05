package net.koshakz.manualtransmission.client;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.client.gui.overlay.ForgeGui;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.lwjgl.glfw.GLFW;

public class GearboxHUD {
    
    private static final ResourceLocation TEXTURE = new ResourceLocation("manualtransmission", "textures/gui/gearbox_hud.png");
    private static final int WIDTH = 120;
    private static final int HEIGHT = 100;

    // Gear positions relative to HUD center
    private float knobX = 0;
    private float knobY = 0;
    private int currentGear = 0; // 0 = Neutral, -1 = Reverse, 1-5 = Forward

    public GearboxHUD() {
        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void onRenderOverlay(RenderGuiOverlayEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) return;
        
        // TODO: Check if player is actually using the Steering Wheel Block
        // if (!player.isUsingSteeringWheel()) return;

        int screenWidth = event.getWindow().getGuiScaledWidth();
        int screenHeight = event.getWindow().getGuiScaledHeight();
        
        int x = screenWidth - WIDTH - 10;
        int y = screenHeight - HEIGHT - 10;

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        
        GuiGraphics guiGraphics = event.getGuiGraphics();
        
        // Render Background (H-Shifter Plate)
        guiGraphics.blit(TEXTURE, x, y, 0, 0, WIDTH, HEIGHT, 256, 256);
        
        // Render Knob
        int knobDrawX = x + (WIDTH / 2) + (int)knobX - 8; // -8 for centering 16px knob
        int knobDrawY = y + (HEIGHT / 2) + (int)knobY - 8;
        guiGraphics.blit(TEXTURE, knobDrawX, knobDrawY, 0, 100, 16, 16, 256, 256); // Assuming knob texture at 0,100

        RenderSystem.disableBlend();
    }

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;

        // "сделай чтобы когда зажимаешь alt то ты мог динамически двигать ручкой кпп"
        boolean isAltDown = GLFW.glfwGetKey(mc.getWindow().getWindow(), GLFW.GLFW_KEY_LEFT_ALT) == GLFW.GLFW_PRESS;

        if (isAltDown) {
            // Unlock cursor logic would go here (or virtual cursor tracking)
            // For now, we simulate dragging with mouse delta if we were capturing it
            
            double mouseX = mc.mouseHandler.xpos();
            double mouseY = mc.mouseHandler.ypos();
            
            // Calculate target positions based on mouse movement
            // This is a simplified logic. In real implementation, use MouseHelper deltas.
            
            // Physics / Clamping Logic
            // "она не должна выходить за линии кпп"
            updateKnobPhysics();
        } else {
            // Snap to nearest gear if released
            snapToGear();
        }
    }

    private void updateKnobPhysics() {
        // H-Shifter Dimensions
        float neutralY = 0;
        float gearThrow = 30; // pixels up/down
        float gearWidth = 20; // width of gear channels
        
        // Gear X positions: R, 1, 2, 3, 4, 5
        float[] gearSlots = {-40, -20, 0, 20, 40}; 
        
        // Logic: If Y is not near Neutral (0), X is locked to a slot
        boolean inNeutralChannel = Math.abs(knobY) < 5;
        
        if (!inNeutralChannel) {
            // Find nearest slot
            float nearestSlot = getNearestSlot(knobX, gearSlots);
            // Clamp X to that slot
            knobX = Mth.clamp(knobX, nearestSlot - 5, nearestSlot + 5);
        }
        
        // Clamp overall bounds
        knobX = Mth.clamp(knobX, -50, 50);
        knobY = Mth.clamp(knobY, -35, 35);
    }
    
    private float getNearestSlot(float current, float[] slots) {
        float best = slots[0];
        float dist = Math.abs(current - best);
        for (float s : slots) {
            float d = Math.abs(current - s);
            if (d < dist) {
                dist = d;
                best = s;
            }
        }
        return best;
    }

    private void snapToGear() {
        // Snap logic to pull knob into gear when Alt is released
    }
}
