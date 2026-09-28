/**
 * A recommendation together with the numbers behind it.
 *
 * @param action
 *            the recommended action
 * @param street
 *            "Preflop", "Flop", "Turn" or "River"
 * @param handCode
 *            the starting-hand code, e.g. "AKs"
 * @param madeHand
 *            the current made hand (postflop), or null preflop
 * @param equity
 *            simulated equity versus random hands
 * @param requiredEquity
 *            equity needed to call profitably (0 if nothing to call)
 * @param reason
 *            a one-line explanation
 */
public record Advice(Action action, String street, String handCode,
        HandEvaluator.Category madeHand, EquityCalculator.Result equity,
        double requiredEquity, String reason) {
}
