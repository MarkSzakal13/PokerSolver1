/**
 * A recommended action.
 */
public enum Action {
    /** Actions. */
    RAISE("Raise"), CALL("Call"), CHECK("Check"), BET("Bet"), FOLD("Fold");

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
    Action(String label) {
        this.label = label;
    }

    @Override
    public String toString() {
        return this.label;
    }
}
