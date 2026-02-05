package net.koshakz.manualtransmission.content;

import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import net.koshakz.manualtransmission.client.GearboxHUD;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.DistExecutor;

import java.util.List;
import java.util.UUID;

// Based on TFMG EngineControllerBlockEntity
public class SteeringWheelBlockEntity extends SmartBlockEntity implements MenuProvider {

    private UUID user;
    
    // Transmission State
    public int currentGear = 0; // 0=Neutral, -1=Reverse, 1-5=Forward
    public float clutchState = 0f; // 0-1
    public float gasState = 0f;
    public float brakeState = 0f;

    public SteeringWheelBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Override
    public void tick() {
        super.tick();
        if (level.isClientSide) {
            // Client-side input handling via GearboxHUD
            // The HUD updates the static state or sends packets
        } else {
            // Server-side logic: Apply speed changes to connected engines based on gear
            if (user != null) {
                // Logic to control engine speed goes here
                // e.g. connectedEngine.setSpeed(baseSpeed * getGearRatio(currentGear));
            }
        }
    }

    public float getGearRatio(int gear) {
        return switch (gear) {
            case -1 -> -0.5f;
            case 1 -> 0.2f;
            case 2 -> 0.4f;
            case 3 -> 0.7f;
            case 4 -> 1.0f;
            case 5 -> 1.5f;
            default -> 0f; // Neutral
        };
    }

    public void startUsing(Player player) {
        this.user = player.getUUID();
        setChanged();
    }

    public void stopUsing() {
        this.user = null;
        setChanged();
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour> behaviours) {
        // Add Create behaviours if needed
    }

    @Override
    public Component getDisplayName() {
        return Component.literal("Steering Wheel");
    }

    @Override
    public AbstractContainerMenu createMenu(int id, Inventory inv, Player player) {
        return null; // No container needed if we use HUD
    }
}
