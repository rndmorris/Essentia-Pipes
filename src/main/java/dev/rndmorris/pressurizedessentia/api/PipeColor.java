package dev.rndmorris.pressurizedessentia.api;

import javax.annotation.Nonnull;

/**
 * Colors a pipe segment might be. Used for restricting inter-segment connections.
 */
public enum PipeColor {

    NONE((byte) 0),
    YELLOW((byte) 1),
    RED((byte) 2),
    BLUE((byte) 3),
    GREEN((byte) 4),
    WHITE((byte) 5),
    BLACK((byte) 6),
    PURPLE((byte) 7);

    /**
     * Unique id for each color.
     */
    public final byte id;

    PipeColor(byte id) {
        this.id = id;
    }

    /**
     * Valid pipe colors, for enumeration.
     */
    public static final PipeColor[] COLORS = { NONE, YELLOW, RED, BLUE, GREEN, WHITE, BLACK, PURPLE };

    /**
     * Get a color from its id.
     * 
     * @param id The id of the color.
     * @return A color if the id is valid, or null if not.
     */
    public static PipeColor fromId(int id) {
        if (0 <= id && id < COLORS.length) {
            return COLORS[id];
        }
        return null;
    }

    /**
     * Evaluate if this color should connect to another.
     * 
     * @param color The color to compare against.
     * @return True if the color will connect to the other, or false if not.
     */
    public boolean willConnectTo(@Nonnull PipeColor color) {
        return this == NONE || color == NONE || this == color;
    }

    /**
     * Get the next color in the cycle, looping back around to the first upon reaching the end.
     * 
     * @return The next color.
     */
    public PipeColor nextColor() {
        return id + 1 >= COLORS.length ? COLORS[0] : COLORS[id + 1];
    }

    /**
     * Get the previous color in the cycle, looping back around to the last upon reaching the beginning.
     * 
     * @return The previous color.
     */
    public PipeColor prevColor() {
        return id - 1 < 0 ? COLORS[COLORS.length - 1] : COLORS[id - 1];
    }
}
