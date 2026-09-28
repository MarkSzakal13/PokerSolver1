/**
 * Seats at a full-ring (9-handed) table, in preflop action order.
 */
public enum Position {
    /** Seats. */
    UTG("UTG"), UTG1("UTG+1"), UTG2("UTG+2"), LOJACK("Lojack"),
    /** Seats. */
    HIJACK("Hijack"), CUTOFF("Cutoff"), BUTTON("Button"),
    /** Seats. */
    SMALL_BLIND("Small Blind"), BIG_BLIND("Big Blind");

    /**
     * Display name.
     */
    private final String label;

    /**
     * Constructor.
     *
     * @param label
     *            display name
     */
    Position(String label) {
        this.label = label;
    }

    /**
     * Returns the position for the 1-based menu number used by the console
     * app (1 = UTG ... 9 = Big Blind).
     *
     * @param number
     *            menu number
     * @return the position
     * @throws IllegalArgumentException
     *             if out of range
     */
    public static Position fromNumber(int number) {
        if (number < 1 || number > values().length) {
            throw new IllegalArgumentException("Position must be 1-"
                    + values().length + ": " + number);
        }
        return values()[number - 1];
    }

    @Override
    public String toString() {
        return this.label;
    }
}
