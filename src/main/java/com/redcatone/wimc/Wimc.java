package com.redcatone.wimc;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


// The value here should match an entry in the META-INF/mods.toml file
@Mod(Wimc.MOD_ID)
public class Wimc {
    public static final String MOD_ID = "wimc";

    // Directly reference a log4j logger.
    private static final Logger LOGGER = LogManager.getLogger();

    public Wimc() {
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::preInitClient);

        // Register ourselves for server and other game events we are interested in
        MinecraftForge.EVENT_BUS.register(this);
    }

    private void preInitClient(final FMLClientSetupEvent event) {
        CrateTooltip.init();
    }
}
