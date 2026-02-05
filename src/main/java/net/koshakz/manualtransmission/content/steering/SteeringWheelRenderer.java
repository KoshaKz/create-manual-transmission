package net.koshakz.manualtransmission.content.steering;

// import com.jozufozu.flywheel.backend.Backend; 
// Flywheel backend import changed in newer versions or is internal.
// For standard kinetic renderer we don't strictly need it if we call super.

import com.simibubi.create.content.kinetics.base.KineticBlockEntityRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.level.block.state.BlockState;

public class SteeringWheelRenderer extends KineticBlockEntityRenderer<SteeringWheelBlockEntity> {

    public SteeringWheelRenderer(BlockEntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    protected void renderSafe(SteeringWheelBlockEntity be, float partialTicks, com.mojang.blaze3d.vertex.PoseStack ms, MultiBufferSource buffer, int light, int overlay) {
        // if (Backend.canUseInstancing(be.getLevel())) return; // Removed for compatibility
        
        super.renderSafe(be, partialTicks, ms, buffer, light, overlay);
    }
}
