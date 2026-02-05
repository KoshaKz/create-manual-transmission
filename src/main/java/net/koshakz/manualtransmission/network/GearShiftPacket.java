package net.koshakz.manualtransmission.network;

import net.koshakz.manualtransmission.content.steering.SteeringWheelBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class GearShiftPacket {
    private final BlockPos pos;
    private final float x;
    private final float y;

    public GearShiftPacket(BlockPos pos, float x, float y) {
        this.pos = pos;
        this.x = x;
        this.y = y;
    }

    public GearShiftPacket(FriendlyByteBuf buffer) {
        this.pos = buffer.readBlockPos();
        this.x = buffer.readFloat();
        this.y = buffer.readFloat();
    }

    public void toBytes(FriendlyByteBuf buffer) {
        buffer.writeBlockPos(pos);
        buffer.writeFloat(x);
        buffer.writeFloat(y);
    }

    public void handle(Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> {
            ServerPlayer player = context.get().getSender();
            if (player == null) return;
            Level level = player.level();
            
            if (level.isLoaded(pos)) {
                BlockEntity be = level.getBlockEntity(pos);
                if (be instanceof SteeringWheelBlockEntity steeringBE) {
                    steeringBE.updateGearShift(x, y);
                }
            }
        });
        context.get().setPacketHandled(true);
    }
}
