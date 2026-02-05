package net.koshakz.manualtransmission.client;

import net.koshakz.manualtransmission.ManualTransmission;
import net.koshakz.manualtransmission.content.steering.SteeringWheelBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.api.distmarker.Dist;
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

        private static boolean screenOpened = false;

        @SubscribeEvent
        public static void clientTick(TickEvent.ClientTickEvent event) {
            if (event.phase != TickEvent.Phase.END) return;

            Minecraft mc = Minecraft.getInstance();
            if (mc.player == null || mc.level == null) return;

            long window = mc.getWindow().getWindow();
            boolean altHeld = GLFW.glfwGetKey(window, GLFW.GLFW_KEY_LEFT_ALT) == GLFW.GLFW_PRESS;

            boolean lookingAtWheel = mc.hitResult instanceof BlockHitResult blockHit
                    && mc.level.getBlockEntity(blockHit.getBlockPos()) instanceof SteeringWheelBlockEntity;

            // Open screen when Alt pressed while looking at wheel
            if (altHeld && lookingAtWheel && mc.screen == null && !screenOpened) {
                BlockHitResult bhr = (BlockHitResult) mc.hitResult;
                mc.setScreen(new TransmissionScreen(bhr.getBlockPos()));
                screenOpened = true;
            }

            // Reset flag when Alt released
            if (!altHeld) {
                screenOpened = false;
            }
        }
    }
}
