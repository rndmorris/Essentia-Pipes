package dev.rndmorris.pressurizedessentia;

import java.io.File;

import net.minecraftforge.common.config.Configuration;

public class Config {

    public static int cycleLengthBasic = 20;
    public static int cycleLengthThaumium = 40;
    public static int cycleLengthVoidmetal = 80;

    public static boolean pipeEnabledBasic = true;
    public static boolean pipeEnabledThaumium = true;
    public static boolean pipeEnabledVoidmetal = true;

    public static int transferRateBasic = 4;
    public static int transferRateThaumium = 8;
    public static int transferRateVoidmetal = 16;

    public static final String CATEGORY_PIPE_BASIC = "Basic Pipe";
    public static final String CATEGORY_PIPE_THAUMIUM = "Thaumium Pipe";
    public static final String CATEGORY_PIPE_VOIDMETAL = "Voidmetal Pipe";

    public static void synchronizeConfiguration(File configFile) {
        Configuration configuration = new Configuration(configFile);

        cycleLengthBasic = configuration.getInt(
            "Cycle Length",
            CATEGORY_PIPE_BASIC,
            cycleLengthBasic,
            4,
            999999,
            "How long the basic pipe's processing cycle is, in ticks.");
        cycleLengthThaumium = configuration.getInt(
            "Cycle Length",
            CATEGORY_PIPE_THAUMIUM,
            cycleLengthThaumium,
            4,
            999999,
            "How long the thaumium pipe's processing cycle is, in ticks.");
        cycleLengthVoidmetal = configuration.getInt(
            "Cycle Length",
            CATEGORY_PIPE_VOIDMETAL,
            cycleLengthVoidmetal,
            4,
            999999,
            "How long the voidmetal pipe's processing cycle is, in ticks.");

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

        if (configuration.hasChanged()) {
            configuration.save();
        }
    }
}
