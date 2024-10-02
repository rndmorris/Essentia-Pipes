package dev.rndmorris.pressurizedessentia.api;

public enum PipeColor {

    NONE((byte) 0),
    YELLOW((byte) 1),
    RED((byte) 2),
    BLUE((byte) 3),
    GREEN((byte) 4),
    WHITE((byte) 5),
    BLACK((byte) 6),
    PURPLE((byte) 7);

    public final byte id;

    PipeColor(byte id) {
        this.id = id;
    }

    public static final PipeColor[] COLORS = { NONE, YELLOW, RED, BLUE, GREEN, WHITE, BLACK, PURPLE };

    public static PipeColor fromId(int id) {
        return COLORS[clampId(id)];
    }

    public static int clampId(int id) {
        return Math.max(0, Math.min(id, COLORS.length));
    }

    public boolean willConnectTo(PipeColor color) {
        return this == NONE || color == NONE || this == color;
    }

    public PipeColor nextColor() {
        return id + 1 >= COLORS.length ? COLORS[0] : COLORS[id + 1];
    }

    public PipeColor prevColor() {
        return id - 1 < 0 ? COLORS[COLORS.length - 1] : COLORS[id - 1];
    }
}
