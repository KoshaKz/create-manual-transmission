package net.koshakz.manualtransmission.registry;

import com.tterrag.registrate.util.entry.BlockEntityEntry;
import net.koshakz.manualtransmission.ManualTransmission;
import net.koshakz.manualtransmission.content.steering.SteeringWheelBlockEntity;
import net.koshakz.manualtransmission.content.steering.SteeringWheelRenderer;

public class ModBlockEntities {
    
    public static final BlockEntityEntry<SteeringWheelBlockEntity> STEERING_WHEEL = ManualTransmission.REGISTRATE
            .blockEntity("steering_wheel", SteeringWheelBlockEntity::new)
            .validBlocks(ModBlocks.STEERING_WHEEL)
            .renderer(() -> SteeringWheelRenderer::new)
            .register();

    public static void register() {}
}
