package net.koshakz.manualtransmission.registry;

import com.simibubi.create.foundation.data.BlockStateGen;
import com.simibubi.create.foundation.data.SharedProperties;
import com.tterrag.registrate.util.entry.BlockEntry;
import net.koshakz.manualtransmission.ManualTransmission;
import net.koshakz.manualtransmission.content.steering.SteeringWheelBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;

// BlockStressDefaults was moved/renamed in Create 6.0 or is handled differently via tags now.
// We will simply remove it for now as it's optional for stress impact (defaults to 0 anyway for new blocks usually)

public class ModBlocks {
    
    public static final BlockEntry<SteeringWheelBlock> STEERING_WHEEL = ManualTransmission.REGISTRATE
            .block("steering_wheel", SteeringWheelBlock::new)
            .initialProperties(SharedProperties::wooden)
            .properties(BlockBehaviour.Properties::noOcclusion)
            // .transform(BlockStressDefaults.setNoImpact()) // REMOVED for Create 6.0 compatibility
            .blockstate(BlockStateGen.simpleCubeAll("steering_wheel"))
            .simpleItem()
            .register();

    public static void register() {}
}
