package net.gaalee;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;

@Mod(waterfinitetillx.MODID)
public class waterfinitetillx {
    public static final String MODID = "waterfinitetillx";

    public waterfinitetillx(IEventBus modEventBus, ModContainer modContainer) {
        modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC);
        modEventBus.addListener(Config::onLoad);
        
        if (net.neoforged.fml.loading.FMLEnvironment.dist.isClient()) {
            modContainer.registerExtensionPoint(net.neoforged.neoforge.client.gui.IConfigScreenFactory.class, net.neoforged.neoforge.client.gui.ConfigurationScreen::new);
        }
    }
}
