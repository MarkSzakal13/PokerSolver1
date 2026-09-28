import java.util.List;

/**
 * Enhanced poker solver interface: strategy advice layered on top of the
 * {@link PokerSolverKernel} hand.
 */
public interface PokerSolver extends PokerSolverKernel {

    /**
     * Returns the starting-hand code of the hand, e.g. "AKs", "T9o", "77".
     *
     * @return the hand code
     * @requires handSize() = 2
     */
    String handCode();

    /**
     * Preflop recommendation from the position charts.
     *
     * @param position
     *            the player's seat
     * @param currentBetBB
     *            the largest bet in front of the player, in big blinds (1 if
     *            nobody has raised)
     * @return raise, call, check or fold
     * @requires handSize() = 2 and currentBetBB >= 0
     */
    Action recommendation(Position position, double currentBetBB);

    /**
     * Full advice for any street: preflop uses the position charts, postflop
     * compares Monte Carlo equity against the pot odds.
     *
     * @param position
     *            the player's seat
     * @param board
     *            0, 3, 4 or 5 community cards
     * @param opponents
     *            number of opponents still in the hand (1..8)
     * @param potBB
     *            the pot in big blinds, including any bet to call
     * @param currentBetBB
     *            the bet to call in big blinds (preflop: the largest bet)
     * @return the advice
     * @requires handSize() = 2
     */
    Advice advise(Position position, List<Card> board, int opponents,
            double potBB, double currentBetBB);
}
