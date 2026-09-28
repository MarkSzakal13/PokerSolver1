import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.junit.Test;

/**
 * Tests for {@link EquityCalculator}.
 */
public class EquityCalculatorTest {

    /** Tolerance for Monte Carlo checks. */
    private static final double TOL = 0.015;

    /**
     * @param cards
     *            space-separated cards, may be empty
     * @return the cards
     */
    private static List<Card> cards(String cards) {
        List<Card> out = new ArrayList<>();
        if (!cards.isEmpty()) {
            for (String c : cards.split(" ")) {
                out.add(Card.parse(c));
            }
        }
        return out;
    }

    /**
     * @param hero
     *            hero cards
     * @param board
     *            board cards
     * @param opp
     *            opponents
     * @return equity
     */
    private static double eq(String hero, String board, int opp) {
        return EquityCalculator.calculate(cards(hero), cards(board), opp,
                100_000, new Random(42)).equity();
    }

    /** Aces vs one random hand is about 85%. */
    @Test
    public void testAcesHeadsUp() {
        assertEquals(0.852, eq("Ah As", "", 1), TOL);
    }

    /** 72o vs one random hand is about 35%. */
    @Test
    public void testSevenTwo() {
        assertEquals(0.346, eq("7h 2c", "", 1), TOL);
    }

    /** Aces lose equity multiway. */
    @Test
    public void testMultiway() {
        assertTrue(eq("Ah As", "", 4) < eq("Ah As", "", 1));
    }

    /** The nuts on the river always win. */
    @Test
    public void testNuts() {
        assertEquals(1.0, eq("Ah Kh", "Qh Jh Th 2c 3d", 3), 1e-9);
    }

    /** Board plays: always a split. */
    @Test
    public void testBoardPlays() {
        EquityCalculator.Result r = EquityCalculator.calculate(
                cards("2c 3d"), cards("Ah Kh Qh Jh Th"), 1, 1000,
                new Random(1));
        assertEquals(0.5, r.equity(), 1e-9);
        assertEquals(1.0, r.tie(), 1e-9);
    }

    /** Duplicates are rejected. */
    @Test(expected = IllegalArgumentException.class)
    public void testDuplicate() {
        eq("Ah Kh", "Ah 2c 3d", 1);
    }
}
