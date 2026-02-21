package net.gaalee;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.common.ModConfigSpec;

/**
 * WaterFiniteTillX mod configuration.
 */
@EventBusSubscriber(modid = waterfinitetillx.MODID)
public class Config {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    public static final ModConfigSpec.IntValue REQUIRED_SOURCES;
    static final ModConfigSpec SPEC;

    static {
        REQUIRED_SOURCES = BUILDER.comment("Number of connected water sources required (via BFS) to allow a new source")
                .translation("waterfinitetillx.config.requiredSources")
                .defineInRange("requiredSources", 100, 2, 2000);
        SPEC = BUILDER.build();
    }

    public static int requiredSources;

    @SubscribeEvent
    static void onLoad(final ModConfigEvent event) {
        requiredSources = REQUIRED_SOURCES.get();
    }
}
