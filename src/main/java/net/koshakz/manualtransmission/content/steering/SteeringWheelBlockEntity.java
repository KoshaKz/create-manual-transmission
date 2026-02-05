package net.koshakz.manualtransmission.content.steering;

import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class SteeringWheelBlockEntity extends KineticBlockEntity {
    
    private float currentGearRatio = 0;
    private float gearX = 0; 
    private float gearY = 0;
    
    public SteeringWheelBlockEntity(BlockEntityType<?> typeIn, BlockPos pos, BlockState state) {
        super(typeIn, pos, state);
    }

    @Override
    public float getGeneratedSpeed() {
        return super.getGeneratedSpeed(); 
    }
    
    public void updateGearShift(float x, float y) {
        this.gearX = x;
        this.gearY = y;
        calculateGearRatio();
        notifyUpdate();
    }

    private void calculateGearRatio() {
        // Placeholder for gear logic
    }
}
