package net.koshakz.manualtransmission.client;

import com.mojang.blaze3d.systems.RenderSystem;
import net.koshakz.manualtransmission.content.steering.SteeringWheelBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.client.gui.overlay.ForgeGui;
import net.minecraftforge.client.gui.overlay.IGuiOverlay;

/**
 * Passive HUD overlay: shows the H-pattern when looking at the steering wheel.
 * Actual input is handled by {@link TransmissionScreen}.
 */
public class TransmissionOverlay {

    public static final IGuiOverlay OVERLAY = TransmissionOverlay::renderOverlay;

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

        // Simple preview lines
        graphics.fill(x + 20, y + 10, x + 25, y + 90, 0x66000000);
        graphics.fill(x + 48, y + 10, x + 53, y + 90, 0x66000000);
        graphics.fill(x + 75, y + 10, x + 80, y + 90, 0x66000000);
        graphics.fill(x + 20, y + 45, x + 80, y + 50, 0x66000000);

        graphics.drawString(mc.font, "Hold ALT", x, y - 20, 0xFFFFFF);

        RenderSystem.disableBlend();
    }
}
