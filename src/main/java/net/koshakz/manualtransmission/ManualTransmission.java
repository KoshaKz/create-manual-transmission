package net.koshakz.manualtransmission;

import com.tterrag.registrate.Registrate;
import net.koshakz.manualtransmission.network.ModPackets;
import net.koshakz.manualtransmission.registry.ModBlockEntities;
import net.koshakz.manualtransmission.registry.ModBlocks;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Mod(ManualTransmission.MOD_ID)
public class ManualTransmission {
    public static final String MOD_ID = "manualtransmission";
    public static final String NAME = "Create: Manual Transmission";
    public static final Logger LOGGER = LoggerFactory.getLogger(NAME);
    public static final Registrate REGISTRATE = Registrate.create(MOD_ID);

    public ManualTransmission() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        
        ModBlocks.register();
        ModBlockEntities.register();
        
        // Регистрируем пакеты во время Common Setup
        modEventBus.addListener(this::setup);
        
        // REGISTRATE.registerEventListeners(modEventBus);
        
        LOGGER.info("Manual Transmission mod initialized!");
    }

    private void setup(final FMLCommonSetupEvent event) {
        ModPackets.register();
    }
}
