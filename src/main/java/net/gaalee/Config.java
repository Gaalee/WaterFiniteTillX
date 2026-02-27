package net.gaalee;

import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.common.ModConfigSpec;

/**
 * WaterFiniteTillX mod configuration.
 */
public class Config {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    public static final ModConfigSpec.IntValue REQUIRED_SOURCES;
    static final ModConfigSpec SPEC;

    static {
        REQUIRED_SOURCES = BUILDER.comment("Number of connected water sources to be considered a infinite body of water.")
                .translation("waterfinitetillx.config.requiredSources")
                .defineInRange("requiredSources", 100, 0, 2000);
        SPEC = BUILDER.build();
    }

    public static int requiredSources;

    static void onLoad(final ModConfigEvent event) {
        if (event.getConfig().getSpec() == SPEC) {
            requiredSources = REQUIRED_SOURCES.get();
        }
    }
}
