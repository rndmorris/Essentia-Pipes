package dev.rndmorris.pressurizedessentia;

import java.io.File;

import net.minecraftforge.common.config.Configuration;

public class Config {

    public static int transferRateBasic = 4;
    public static int transferRateThaumium = 16;
    public static int transferRateVoidmetal = 64;

    public static boolean pipeEnabledBasic = true;
    public static boolean pipeEnabledThaumium = true;
    public static boolean pipeEnabledVoidmetal = true;

    public static final String CATEGORY_PIPE_BASIC = "Basic Pipe";
    public static final String CATEGORY_PIPE_THAUMIUM = "Thaumium Pipe";
    public static final String CATEGORY_PIPE_VOIDMETAL = "Voidmetal Pipe";

    public static void synchronizeConfiguration(File configFile) {
        Configuration configuration = new Configuration(configFile);

        transferRateBasic = configuration.getInt(
            "Transfer Rate",
            CATEGORY_PIPE_BASIC,
            transferRateBasic,
            1,
            999999,
            "How much essentia the basic pipe will try to transfer every cycle.");
        transferRateThaumium = configuration.getInt(
            "Transfer Rate",
            CATEGORY_PIPE_THAUMIUM,
            transferRateThaumium,
            1,
            999999,
            "How much essentia the thaumium pipe will try to transfer every cycle.");
        transferRateVoidmetal = configuration.getInt(
            "Transfer Rate",
            CATEGORY_PIPE_VOIDMETAL,
            transferRateVoidmetal,
            1,
            999999,
            "How much essentia the voidmetal pipe will try to transfer every cycle.");

        pipeEnabledBasic = configuration
            .getBoolean("Enabled", CATEGORY_PIPE_BASIC, pipeEnabledBasic, "Whether the basic pipe is enabled.");
        pipeEnabledThaumium = configuration.getBoolean(
            "Enabled",
            CATEGORY_PIPE_THAUMIUM,
            pipeEnabledThaumium,
            "Whether the thaumium pipe is enabled.");
        pipeEnabledVoidmetal = configuration.getBoolean(
            "Enabled",
            CATEGORY_PIPE_VOIDMETAL,
            pipeEnabledVoidmetal,
            "Whether the voidmetal pipe is enabled.");

        if (configuration.hasChanged()) {
            configuration.save();
        }
    }
}
