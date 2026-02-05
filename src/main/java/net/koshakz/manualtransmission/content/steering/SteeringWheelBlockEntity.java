package net.koshakz.manualtransmission.content.steering;

import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class SteeringWheelBlockEntity extends KineticBlockEntity {
    
    // Make these public or add getters for the screen to access
    public float gearX = 0; 
    public float gearY = 0;
    private int currentGear = 0; 
    
    private static final float[] GEAR_RATIOS = {0.0f, 0.5f, 1.0f, 1.5f, 2.0f, 3.0f, 4.0f};
    
    public SteeringWheelBlockEntity(BlockEntityType<?> typeIn, BlockPos pos, BlockState state) {
        super(typeIn, pos, state);
    }

    @Override
    public float getGeneratedSpeed() {
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
        notifyUpdate(); // Marks dirty and syncs
    }

    private void calculateGearRatio() {
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
    }

    @Override
    protected void write(CompoundTag compound, boolean clientPacket) {
        super.write(compound, clientPacket);
        compound.putFloat("GearX", gearX);
        compound.putFloat("GearY", gearY);
        compound.putInt("CurrentGear", currentGear);
    }

    @Override
    protected void read(CompoundTag compound, boolean clientPacket) {
        super.read(compound, clientPacket);
        gearX = compound.getFloat("GearX");
        gearY = compound.getFloat("GearY");
        currentGear = compound.getInt("CurrentGear");
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag() {
        CompoundTag tag = super.getUpdateTag();
        write(tag, true);
        return tag;
    }
}
