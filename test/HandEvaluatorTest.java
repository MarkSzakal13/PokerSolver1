import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

/**
 * Tests for {@link HandEvaluator}.
 */
public class HandEvaluatorTest {

    /**
     * @param cards
     *            space-separated cards
     * @return the score
     */
    private static int eval(String cards) {
        List<Card> list = new ArrayList<>();
        for (String c : cards.split(" ")) {
            list.add(Card.parse(c));
        }
        return HandEvaluator.evaluate(list);
    }

    /**
     * @param cards
     *            space-separated cards
     * @return the category
     */
    private static HandEvaluator.Category cat(String cards) {
        return HandEvaluator.category(eval(cards));
    }

    /** Every category is detected. */
    @Test
    public void testCategories() {
        assertEquals(HandEvaluator.Category.HIGH_CARD,
                cat("Ah Kd 9c 7s 2h 3d 5c"));
        assertEquals(HandEvaluator.Category.PAIR, cat("Ah Ad 9c 7s 2h 3d Jc"));
        assertEquals(HandEvaluator.Category.TWO_PAIR,
                cat("Ah Ad 9c 9s 2h 3d Jc"));
        assertEquals(HandEvaluator.Category.THREE_OF_A_KIND,
                cat("Ah Ad Ac 9s 2h 3d Jc"));
        assertEquals(HandEvaluator.Category.STRAIGHT,
                cat("5h 6d 7c 8s 9h 2d 2c"));
        assertEquals(HandEvaluator.Category.FLUSH,
                cat("2h 6h 9h Jh Kh Ad Ac"));
        assertEquals(HandEvaluator.Category.FULL_HOUSE,
                cat("Ah Ad Ac 9s 9h 3d Jc"));
        assertEquals(HandEvaluator.Category.FOUR_OF_A_KIND,
                cat("Ah Ad Ac As 9h 3d Jc"));
        assertEquals(HandEvaluator.Category.STRAIGHT_FLUSH,
                cat("5h 6h 7h 8h 9h Ad Ac"));
    }

    /** The wheel is a five-high straight, below a six-high straight. */
    @Test
    public void testWheel() {
        assertEquals(HandEvaluator.Category.STRAIGHT,
                cat("Ah 2d 3c 4s 5h Kd Kc"));
        assertTrue(eval("Ah 2d 3c 4s 5h Kd Qc") < eval("2h 3d 4c 5s 6h Kd Qc"));
    }

    /** Steel wheel is a straight flush. */
    @Test
    public void testSteelWheel() {
        assertEquals(HandEvaluator.Category.STRAIGHT_FLUSH,
                cat("Ah 2h 3h 4h 5h Kd Kc"));
    }

    /** Kickers break ties; equal hands tie. */
    @Test
    public void testKickers() {
        assertTrue(eval("Ah Ad Kc 7s 2h") > eval("As Ac Qc 7d 2c"));
        assertTrue(eval("Kh Kd 9c 9s Ah") > eval("Ks Kc 9d 9h Qh"));
        assertEquals(eval("Ah Kd 9c 7s 2h"), eval("As Kc 9d 7h 2d"));
    }

    /** Two trips make the best full house. */
    @Test
    public void testDoubleTrips() {
        assertEquals(eval("Ah Ad Ac Ks Kh"), eval("Ah Ad Ac Ks Kh Kd 2c"));
    }

    /** Flush beats straight on the same board. */
    @Test
    public void testFlushBeatsStraight() {
        assertTrue(eval("2h 6h 9h Jh Kh") > eval("9s Tc Jd Qc Kd"));
    }
}
