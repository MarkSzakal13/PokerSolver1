import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

/**
 * Monte Carlo equity calculator: the hero's hole cards (and any known board
 * cards) against a number of opponents holding random hands.
 */
public final class EquityCalculator {

    /**
     * Result of a simulation.
     *
     * @param equity
     *            share of the pot won on average, in [0, 1]
     * @param win
     *            fraction of trials won outright
     * @param tie
     *            fraction of trials that were split
     * @param trials
     *            number of trials run
     */
    public record Result(double equity, double win, double tie, int trials) {
    }

    /** Default number of simulated deals. */
    public static final int DEFAULT_TRIALS = 50_000;

    /** Maximum number of opponents supported. */
    public static final int MAX_OPPONENTS = 8;

    /** Board size. */
    private static final int BOARD = 5;

    /** Hole card count. */
    private static final int HOLE = 2;

    /** Cards evaluated per player. */
    private static final int SEVEN = 7;

    /**
     * Private constructor: static utility class.
     */
    private EquityCalculator() {
    }

    /**
     * Runs a simulation with {@link #DEFAULT_TRIALS} trials.
     *
     * @param hero
     *            the hero's two hole cards
     * @param board
     *            0-5 known board cards
     * @param opponents
     *            number of opponents (1..8)
     * @return the result
     */
    public static Result calculate(List<Card> hero, List<Card> board,
            int opponents) {
        return calculate(hero, board, opponents, DEFAULT_TRIALS, new Random());
    }

    /**
     * Runs a simulation.
     *
     * @param hero
     *            the hero's two hole cards
     * @param board
     *            0-5 known board cards
     * @param opponents
     *            number of opponents (1..8)
     * @param trials
     *            number of simulated deals
     * @param rng
     *            random source
     * @return the result
     * @throws IllegalArgumentException
     *             on bad input or duplicate cards
     */
    public static Result calculate(List<Card> hero, List<Card> board,
            int opponents, int trials, Random rng) {
        if (hero.size() != HOLE) {
            throw new IllegalArgumentException("Hero needs exactly 2 cards");
        }
        if (board.size() > BOARD || board.size() == 1
                || board.size() == 2) {
            throw new IllegalArgumentException(
                    "Board must have 0, 3, 4 or 5 cards");
        }
        if (opponents < 1 || opponents > MAX_OPPONENTS) {
            throw new IllegalArgumentException("Opponents must be 1-8");
        }
        if (trials < 1) {
            throw new IllegalArgumentException("Trials must be positive");
        }

        Set<Integer> used = new HashSet<>();
        for (Card c : hero) {
            if (!used.add(c.index())) {
                throw new IllegalArgumentException("Duplicate card: " + c);
            }
        }
        for (Card c : board) {
            if (!used.add(c.index())) {
                throw new IllegalArgumentException("Duplicate card: " + c);
            }
        }

        int[] deck = new int[Card.DECK_SIZE - used.size()];
        int d = 0;
        for (int i = 0; i < Card.DECK_SIZE; i++) {
            if (!used.contains(i)) {
                deck[d] = i;
                d++;
            }
        }

        int knownBoard = board.size();
        int boardNeeded = BOARD - knownBoard;
        int draw = boardNeeded + HOLE * opponents;

        int[] heroCards = new int[SEVEN];
        heroCards[0] = hero.get(0).index();
        heroCards[1] = hero.get(1).index();
        int[] boardCards = new int[BOARD];
        for (int i = 0; i < knownBoard; i++) {
            boardCards[i] = board.get(i).index();
        }
        int[] villain = new int[SEVEN];

        double equitySum = 0;
        int wins = 0;
        int ties = 0;

        for (int t = 0; t < trials; t++) {
            // Partial Fisher-Yates: the first `draw` slots become our deal.
            for (int i = 0; i < draw; i++) {
                int j = i + rng.nextInt(deck.length - i);
                int tmp = deck[i];
                deck[i] = deck[j];
                deck[j] = tmp;
            }
            for (int i = 0; i < boardNeeded; i++) {
                boardCards[knownBoard + i] = deck[i];
            }
            System.arraycopy(boardCards, 0, heroCards, HOLE, BOARD);
            System.arraycopy(boardCards, 0, villain, HOLE, BOARD);
            int heroScore = HandEvaluator.evaluate(heroCards, SEVEN);

            int best = heroScore;
            int tiedWithHero = 1;
            boolean heroBeaten = false;
            for (int o = 0; o < opponents; o++) {
                villain[0] = deck[boardNeeded + HOLE * o];
                villain[1] = deck[boardNeeded + HOLE * o + 1];
                int s = HandEvaluator.evaluate(villain, SEVEN);
                if (s > best) {
                    best = s;
                    heroBeaten = true;
                    break;
                } else if (s == heroScore) {
                    tiedWithHero++;
                }
            }
            if (!heroBeaten) {
                if (tiedWithHero == 1) {
                    wins++;
                    equitySum += 1;
                } else {
                    ties++;
                    equitySum += 1.0 / tiedWithHero;
                }
            }
        }

        return new Result(equitySum / trials, (double) wins / trials,
                (double) ties / trials, trials);
    }
}
