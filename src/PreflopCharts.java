import java.util.EnumMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Preflop strategy charts for a 9-handed, 100 big blind cash game.
 *
 * <p>
 * The ranges are simplified approximations of commonly published
 * game-theory-inspired opening, 3-betting and defending ranges. Ranges are
 * written in standard shorthand (e.g. {@code "77+, A2s+, KTs+, AJo+"}) and
 * parsed into sets of the 169 starting-hand codes such as {@code "AKs"},
 * {@code "T9o"} or {@code "77"}.
 */
public final class PreflopCharts {

    /**
     * What the hero is facing, derived from the size of the bet to call.
     */
    public enum Situation {
        /** Nobody has raised (only the blinds are in). */
        UNOPENED("Unopened pot"),
        /** One player has open-raised. */
        FACING_OPEN("Facing an open raise"),
        /** Facing a 3-bet or a large raise. */
        FACING_3BET("Facing a 3-bet / big raise"),
        /** Facing an all-in or near all-in. */
        FACING_ALL_IN("Facing an all-in");

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
        Situation(String label) {
            this.label = label;
        }

        @Override
        public String toString() {
            return this.label;
        }
    }

    /** Ranks from low to high, as single characters. */
    public static final String RANKS = "23456789TJQKA";

    /** Largest bet (in big blinds) that still counts as unopened. */
    private static final double UNOPENED_MAX = 1.0;

    /** Largest bet (in big blinds) that counts as a normal open raise. */
    private static final double OPEN_MAX = 4.5;

    /** Largest bet (in big blinds) that counts as a 3-bet. */
    private static final double THREE_BET_MAX = 15.0;

    /** Opening (raise first in) ranges. */
    private static final Map<Position, Set<String>> OPEN = new EnumMap<>(
            Position.class);

    /** 3-bet ranges versus a single open raise. */
    private static final Map<Position, Set<String>> THREE_BET = new EnumMap<>(
            Position.class);

    /** Flat-call ranges versus a single open raise. */
    private static final Map<Position, Set<String>> FLAT = new EnumMap<>(
            Position.class);

    /** Hands the big blind raises with when the pot is only limped. */
    private static final Set<String> BB_ISO = parseRange(
            "88+, ATs+, KJs+, AJo+, KQo");

    /** Hands that 4-bet versus a 3-bet. */
    private static final Set<String> FOUR_BET = parseRange("QQ+, AKs");

    /** Hands that call a 3-bet. */
    private static final Set<String> CALL_3BET = parseRange(
            "JJ-99, AKo, AQs, AJs, KQs");

    /** Hands that call an all-in. */
    private static final Set<String> CALL_ALL_IN = parseRange(
            "JJ+, AKs, AKo");

    static {
        OPEN.put(Position.UTG,
                parseRange("55+, A3s+, K9s+, Q9s+, JTs, T9s, AJo+, KQo"));
        OPEN.put(Position.UTG1, parseRange(
                "55+, A2s+, K9s+, Q9s+, J9s+, T9s, 98s, ATo+, KQo"));
        OPEN.put(Position.UTG2, parseRange(
                "44+, A2s+, K8s+, Q9s+, J9s+, T8s+, 98s, 87s, ATo+, KJo+"));
        OPEN.put(Position.LOJACK, parseRange("33+, A2s+, K7s+, Q9s+, J9s+,"
                + " T8s+, 97s+, 87s, 76s, A9o+, KJo+, QJo"));
        OPEN.put(Position.HIJACK, parseRange("22+, A2s+, K6s+, Q8s+, J8s+,"
                + " T8s+, 97s+, 86s+, 76s, 65s, A9o+, KTo+, QTo+, JTo"));
        OPEN.put(Position.CUTOFF, parseRange("22+, A2s+, K3s+, Q6s+, J7s+,"
                + " T7s+, 96s+, 85s+, 75s+, 64s+, 54s, A7o+, A5o, KTo+,"
                + " QTo+, JTo"));
        OPEN.put(Position.BUTTON, parseRange("22+, A2s+, K2s+, Q2s+, J4s+,"
                + " T6s+, 95s+, 85s+, 74s+, 63s+, 53s+, 43s, A2o+, K8o+,"
                + " Q9o+, J9o+, T8o+, 98o, 87o"));
        OPEN.put(Position.SMALL_BLIND, parseRange("22+, A2s+, K2s+, Q4s+,"
                + " J6s+, T6s+, 96s+, 85s+, 75s+, 64s+, 54s, A2o+, K8o+,"
                + " Q9o+, J9o+, T9o"));
        OPEN.put(Position.BIG_BLIND, BB_ISO);

        Set<String> ep3 = parseRange("QQ+, AKs, AKo");
        Set<String> epCall = parseRange(
                "JJ-22, AQs-ATs, KQs, KJs, QJs, JTs, AQo");
        for (Position p : new Position[] { Position.UTG, Position.UTG1,
            Position.UTG2, Position.LOJACK }) {
            THREE_BET.put(p, ep3);
            FLAT.put(p, epCall);
        }
        Set<String> mp3 = parseRange("JJ+, AQs+, AKo, A5s-A4s");
        Set<String> mpCall = parseRange("TT-22, AJs-ATs, KQs-KTs, QJs, QTs,"
                + " JTs, T9s, 98s, AQo, AJo, KQo");
        THREE_BET.put(Position.HIJACK, mp3);
        FLAT.put(Position.HIJACK, mpCall);
        THREE_BET.put(Position.CUTOFF, mp3);
        FLAT.put(Position.CUTOFF, mpCall);

        THREE_BET.put(Position.BUTTON,
                parseRange("TT+, AJs+, KQs, AQo+, A5s-A4s"));
        FLAT.put(Position.BUTTON, parseRange("99-22, ATs-A6s, KJs-K9s, QTs+,"
                + " J9s+, T8s+, 97s+, 87s, 76s, 65s, AJo, KQo, KJo"));

        THREE_BET.put(Position.SMALL_BLIND,
                parseRange("99+, ATs+, KJs+, QJs, AQo+, A5s-A4s"));
        FLAT.put(Position.SMALL_BLIND, parseRange("88-66, JTs, T9s"));

        THREE_BET.put(Position.BIG_BLIND,
                parseRange("QQ+, AKs, AKo, A5s-A4s"));
        FLAT.put(Position.BIG_BLIND, parseRange("JJ-22, AQs-A6s, A3s-A2s,"
                + " K2s+, Q5s+, J7s+, T7s+, 96s+, 85s+, 74s+, 64s+, 53s+,"
                + " 43s, AQo-A7o, K9o+, Q9o+, J9o+, T9o, 98o"));
    }

    /**
     * Private constructor: static utility class.
     */
    private PreflopCharts() {
    }

    /**
     * Classifies the bet to call (in big blinds) into a situation.
     *
     * @param betBB
     *            the largest bet in front of the hero, in big blinds
     * @return the situation
     */
    public static Situation situation(double betBB) {
        if (betBB <= UNOPENED_MAX) {
            return Situation.UNOPENED;
        } else if (betBB <= OPEN_MAX) {
            return Situation.FACING_OPEN;
        } else if (betBB <= THREE_BET_MAX) {
            return Situation.FACING_3BET;
        }
        return Situation.FACING_ALL_IN;
    }

    /**
     * Returns the chart action for a hand code in a spot.
     *
     * @param position
     *            the hero's seat
     * @param situation
     *            what the hero faces
     * @param code
     *            a hand code such as "AKs", "T9o" or "77"
     * @return the recommended action
     */
    public static Action action(Position position, Situation situation,
            String code) {
        switch (situation) {
            case UNOPENED:
                if (OPEN.get(position).contains(code)) {
                    return Action.RAISE;
                }
                return position == Position.BIG_BLIND ? Action.CHECK
                        : Action.FOLD;
            case FACING_OPEN:
                if (THREE_BET.get(position).contains(code)) {
                    return Action.RAISE;
                }
                if (FLAT.get(position).contains(code)) {
                    return Action.CALL;
                }
                return Action.FOLD;
            case FACING_3BET:
                if (FOUR_BET.contains(code)) {
                    return Action.RAISE;
                }
                if (CALL_3BET.contains(code)) {
                    return Action.CALL;
                }
                return Action.FOLD;
            default:
                return CALL_ALL_IN.contains(code) ? Action.CALL
                        : Action.FOLD;
        }
    }

    /**
     * Returns the starting-hand code of two cards, e.g. "AKs", "T9o", "77".
     *
     * @param a
     *            first card
     * @param b
     *            second card
     * @return the hand code
     */
    public static String handCode(Card a, Card b) {
        Card hi = a;
        Card lo = b;
        if (b.rank().compareTo(a.rank()) > 0) {
            hi = b;
            lo = a;
        }
        String code = "" + hi.rank().getSymbol() + lo.rank().getSymbol();
        if (hi.rank() == lo.rank()) {
            return code;
        }
        return code + (hi.suit() == lo.suit() ? "s" : "o");
    }

    /**
     * Returns the hand code for a cell of the standard 13x13 grid. Row and
     * column 0 are aces; suited hands are above the diagonal and offsuit
     * hands below it.
     *
     * @param row
     *            row in [0, 13)
     * @param col
     *            column in [0, 13)
     * @return the hand code
     */
    public static String gridCode(int row, int col) {
        int last = RANKS.length() - 1;
        char r1 = RANKS.charAt(last - Math.min(row, col));
        char r2 = RANKS.charAt(last - Math.max(row, col));
        if (row == col) {
            return "" + r1 + r2;
        }
        return "" + r1 + r2 + (row < col ? "s" : "o");
    }

    /**
     * Parses range shorthand into a set of hand codes. Supports tokens like
     * {@code 77}, {@code 77+}, {@code TT-66}, {@code AKs}, {@code AK} (both
     * suited and offsuit), {@code A9s+} (A9s through AKs) and
     * {@code A5s-A2s}.
     *
     * @param range
     *            comma-separated range text
     * @return the set of hand codes
     * @throws IllegalArgumentException
     *             on a malformed token
     */
    public static Set<String> parseRange(String range) {
        Set<String> out = new HashSet<>();
        for (String raw : range.split(",")) {
            String tok = raw.trim();
            if (!tok.isEmpty()) {
                addToken(out, tok);
            }
        }
        return out;
    }

    /**
     * Adds one range token to a set.
     *
     * @param out
     *            the set to add to
     * @param tok
     *            the token
     */
    private static void addToken(Set<String> out, String tok) {
        boolean plus = tok.endsWith("+");
        String body = plus ? tok.substring(0, tok.length() - 1) : tok;
        String from = body;
        String to = null;
        int dash = body.indexOf('-');
        if (dash > 0) {
            from = body.substring(0, dash);
            to = body.substring(dash + 1);
        }
        int hi = rankIndex(from.charAt(0), tok);
        int lo = rankIndex(from.charAt(1), tok);
        String kind = from.length() > 2 ? from.substring(2) : "";

        if (hi == lo) {
            int top = hi;
            int bottom = hi;
            if (plus) {
                top = RANKS.length() - 1;
            } else if (to != null) {
                bottom = rankIndex(to.charAt(0), tok);
                if (bottom > top) {
                    int t = top;
                    top = bottom;
                    bottom = t;
                }
            }
            for (int r = bottom; r <= top; r++) {
                out.add("" + RANKS.charAt(r) + RANKS.charAt(r));
            }
            return;
        }

        if (lo > hi) {
            int t = hi;
            hi = lo;
            lo = t;
        }
        int kTop = lo;
        int kBottom = lo;
        if (plus) {
            kTop = hi - 1;
        } else if (to != null) {
            int other = rankIndex(to.charAt(1), tok);
            kTop = Math.max(lo, other);
            kBottom = Math.min(lo, other);
        }
        for (int k = kBottom; k <= kTop; k++) {
            String base = "" + RANKS.charAt(hi) + RANKS.charAt(k);
            if (kind.isEmpty() || kind.equals("s")) {
                out.add(base + "s");
            }
            if (kind.isEmpty() || kind.equals("o")) {
                out.add(base + "o");
            }
        }
    }

    /**
     * Index of a rank character in {@link #RANKS}.
     *
     * @param c
     *            the rank character
     * @param tok
     *            the token (for error messages)
     * @return the index
     */
    private static int rankIndex(char c, String tok) {
        int i = RANKS.indexOf(Character.toUpperCase(c));
        if (i < 0) {
            throw new IllegalArgumentException("Bad range token: " + tok);
        }
        return i;
    }
}
