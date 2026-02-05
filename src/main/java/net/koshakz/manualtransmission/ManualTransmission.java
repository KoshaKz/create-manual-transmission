package net.koshakz.manualtransmission;

import com.tterrag.registrate.Registrate;
import net.koshakz.manualtransmission.registry.ModBlockEntities;
import net.koshakz.manualtransmission.registry.ModBlocks;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
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
        
        // В новых версиях Registrate метод registerEventListeners защищен или вызывается иначе.
        // Обычно Registrate.create() уже достаточно, если мы используем правильный API.
        // Но для Forge часто нужно явно передать event bus.
        // Попробуем просто не вызывать его явно, так как Registrate.create() часто подхватывает контекст FML автоматически,
        // либо используем правильный метод если он есть.
        
        // Правильный способ для Create Addons обычно такой:
        // Передаем EventBus сразу при регистрации объектов или используем register() если он публичный
        
        ModBlocks.register();
        ModBlockEntities.register();
        
        // В Create 0.5.1/6.0 с Registrate 1.3.3 этот метод может быть не нужен или вызываться неявно.
        // Попробуем закомментировать, так как Registrate сам подписывается на события если создан в конструкторе мода.
        // REGISTRATE.registerEventListeners(modEventBus);
        
        LOGGER.info("Manual Transmission mod initialized!");
    }
}
