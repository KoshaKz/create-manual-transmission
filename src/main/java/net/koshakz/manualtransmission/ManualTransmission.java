package net.koshakz.manualtransmission;

import net.minecraftforge.fml.common.Mod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Mod(ManualTransmission.MOD_ID)
public class ManualTransmission {
    public static final String MOD_ID = "manualtransmission";
    public static final String NAME = "Create: Manual Transmission";
    public static final Logger LOGGER = LoggerFactory.getLogger(NAME);

    public ManualTransmission() {
        LOGGER.info("Manual Transmission mod initialized!");
        LOGGER.info("Adding steering wheel and transmission system to Create...");
    }
}
