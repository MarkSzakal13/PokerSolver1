import java.util.ArrayList;
import java.util.List;

/**
 * Layered implementations of the {@link PokerSolver} secondary methods, plus
 * {@code equals}, {@code hashCode} and {@code toString}, written only in
 * terms of the kernel methods.
 */
public abstract class PokerSolverSecondary implements PokerSolver {

    /** Flop size. */
    private static final int FLOP = 3;

    /** Turn board size. */
    private static final int TURN = 4;

    /** River board size. */
    private static final int RIVER = 5;

    /** How much of the non-fair-share equity counts as "strong". */
    private static final double VALUE_MARGIN = 0.35;

    /** Percent multiplier. */
    private static final double PERCENT = 100.0;

    @Override
    public final String handCode() {
        List<Card> cards = this.getPlayerCards();
        if (cards.size() != MAX_CARDS) {
            throw new IllegalStateException("Hand needs exactly 2 cards");
        }
        return PreflopCharts.handCode(cards.get(0), cards.get(1));
    }

    @Override
    public final Action recommendation(Position position,
            double currentBetBB) {
        PreflopCharts.Situation s = PreflopCharts.situation(currentBetBB);
        return PreflopCharts.action(position, s, this.handCode());
    }

    @Override
    public final Advice advise(Position position, List<Card> board,
            int opponents, double potBB, double currentBetBB) {
        List<Card> hand = this.getPlayerCards();
        String code = this.handCode();
        EquityCalculator.Result eq = EquityCalculator.calculate(hand, board,
                opponents);

        if (board.isEmpty()) {
            PreflopCharts.Situation s = PreflopCharts
                    .situation(currentBetBB);
            Action a = PreflopCharts.action(position, s, code);
            String reason;
            if (a == Action.FOLD) {
                reason = "In the " + position + ", " + lower(s) + ": "
                        + code + " is not in the playable range.";
            } else {
                reason = "In the " + position + ", " + lower(s) + ": "
                        + code + " is in the " + lower(a) + " range.";
            }
            return new Advice(a, "Preflop", code, null, eq, 0, reason);
        }

        List<Card> all = new ArrayList<>(hand);
        all.addAll(board);
        HandEvaluator.Category made = HandEvaluator
                .category(HandEvaluator.evaluate(all));
        String street = streetName(board.size());

        double fairShare = 1.0 / (opponents + 1);
        double strong = fairShare + (1 - fairShare) * VALUE_MARGIN;
        double required = 0;
        Action a;
        String reason;
        if (currentBetBB <= 0) {
            if (eq.equity() >= strong) {
                a = Action.BET;
                reason = "Strong equity (" + pct(eq.equity())
                        + ") - bet for value.";
            } else {
                a = Action.CHECK;
                reason = "Equity " + pct(eq.equity())
                        + " is not strong enough to bet for value.";
            }
        } else {
            // Pot odds: call / (pot after we call)
            required = currentBetBB / (potBB + currentBetBB);
            if (eq.equity() >= strong && eq.equity() > required) {
                a = Action.RAISE;
                reason = "Equity " + pct(eq.equity())
                        + " is well ahead - raise for value.";
            } else if (eq.equity() >= required) {
                a = Action.CALL;
                reason = "Equity " + pct(eq.equity()) + " beats the "
                        + pct(required) + " the pot odds require.";
            } else {
                a = Action.FOLD;
                reason = "Equity " + pct(eq.equity()) + " is below the "
                        + pct(required) + " the pot odds require.";
            }
        }
        return new Advice(a, street, code, made, eq, required, reason);
    }

    /**
     * Street name for a board size.
     *
     * @param n
     *            number of board cards
     * @return the street name
     */
    private static String streetName(int n) {
        switch (n) {
            case FLOP:
                return "Flop";
            case TURN:
                return "Turn";
            case RIVER:
                return "River";
            default:
                return "Preflop";
        }
    }

    /**
     * Lower-cases an object's display string.
     *
     * @param o
     *            the object
     * @return its lower-cased toString
     */
    private static String lower(Object o) {
        return o.toString().toLowerCase();
    }

    /**
     * Formats a fraction as a percentage.
     *
     * @param f
     *            the fraction
     * @return e.g. "42.3%"
     */
    private static String pct(double f) {
        return String.format("%.1f%%", f * PERCENT);
    }

    @Override
    public final String toString() {
        return this.getPlayerCards().toString();
    }

    @Override
    public final boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof PokerSolver)) {
            return false;
        }
        PokerSolver other = (PokerSolver) obj;
        return this.getPlayerCards().equals(other.getPlayerCards());
    }

    @Override
    public final int hashCode() {
        return this.getPlayerCards().hashCode();
    }
}
