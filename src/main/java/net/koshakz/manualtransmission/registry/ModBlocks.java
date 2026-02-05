package net.koshakz.manualtransmission.registry;

import com.simibubi.create.content.kinetics.BlockStressDefaults;
import com.simibubi.create.foundation.data.BlockStateGen;
import com.simibubi.create.foundation.data.SharedProperties;
import com.tterrag.registrate.util.entry.BlockEntry;
import net.koshakz.manualtransmission.ManualTransmission;
import net.koshakz.manualtransmission.content.steering.SteeringWheelBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;

public class ModBlocks {
    
    public static final BlockEntry<SteeringWheelBlock> STEERING_WHEEL = ManualTransmission.REGISTRATE
            .block("steering_wheel", SteeringWheelBlock::new)
            .initialProperties(SharedProperties::wooden)
            .properties(BlockBehaviour.Properties::noOcclusion)
            .transform(BlockStressDefaults.setNoImpact())
            .blockstate(BlockStateGen.simpleCubeAll("steering_wheel"))
            .simpleItem()
            .register();

    public static void register() {}
}
