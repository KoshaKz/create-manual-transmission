package net.koshakz.manualtransmission.content.steering;

import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class SteeringWheelBlockEntity extends KineticBlockEntity {
    
    private float gearX = 0; 
    private float gearY = 0;
    private int currentGear = 0; // 0 = Neutral
    
    // Gear ratios: 1=0.5, 2=1.0, 3=1.5, 4=2.0, 5=3.0, 6=4.0
    private static final float[] GEAR_RATIOS = {0.0f, 0.5f, 1.0f, 1.5f, 2.0f, 3.0f, 4.0f};
    
    public SteeringWheelBlockEntity(BlockEntityType<?> typeIn, BlockPos pos, BlockState state) {
        super(typeIn, pos, state);
    }

    @Override
    public float getGeneratedSpeed() {
        // This method is called by Create to determine output speed.
        // We will take the input speed (if we were connected as a passthrough) or just act as a controller.
        // For a controller, usually we modifying speed of connected components or are a source.
        
        // If this is a source (creative motor style logic), we return speed.
        // If this modifies speed, we need to be part of the kinetic network as a converter (like Gearbox).
        // Since Steering Wheel is KineticBlock, it propagates speed.
        
        // Let's implement variable speed propagation:
        // Output Speed = Input Speed * Gear Ratio
        
        float inputSpeed = super.getGeneratedSpeed(); // Or theoretical input
        // Since we extend KineticBlockEntity, we participate in the network. 
        // We need to override propagateRotation to change speed, but that's complex API.
        
        // SIMPLIFIED LOGIC:
        // We will just store the ratio. The actual speed changing logic requires mixins or 
        // extending RotationSpeedController (like Speed Controller block).
        
        // For now, let's just allow this value to be accessed.
        return super.getGeneratedSpeed();
    }
    
    public float getGearRatio() {
        if (currentGear >= 0 && currentGear < GEAR_RATIOS.length) {
            return GEAR_RATIOS[currentGear];
        }
        return 0f;
    }
    
    public void updateGearShift(float x, float y) {
        this.gearX = x;
        this.gearY = y;
        calculateGearRatio();
        notifyUpdate();
    }

    private void calculateGearRatio() {
        // Same logic as client to ensure sync
        int newGear = 0;
        if (gearY < -0.8) {
            if (gearX < -0.8) newGear = 1;
            else if (gearX > -0.2 && gearX < 0.2) newGear = 3;
            else if (gearX > 0.8) newGear = 5;
        } else if (gearY > 0.8) {
             if (gearX < -0.8) newGear = 2;
            else if (gearX > -0.2 && gearX < 0.2) newGear = 4;
            else if (gearX > 0.8) newGear = 6;
        }
        this.currentGear = newGear;
        
        // Here we would trigger network update if this was a speed controller
    }
}
