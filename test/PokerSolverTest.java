import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Test;

/**
 * Tests for {@link PokerSolverOnList} kernel and secondary methods.
 */
public class PokerSolverTest {

    /** Adding and reading cards. */
    @Test
    public void testAddAndGet() {
        PokerSolver s = new PokerSolverOnList();
        assertEquals(0, s.handSize());
        s.addCard("Ah");
        s.addCard("10d");
        assertEquals(2, s.handSize());
        assertEquals(List.of(Card.parse("Ah"), Card.parse("Td")),
                s.getPlayerCards());
    }

    /** getPlayerCards returns a copy. */
    @Test
    public void testGetReturnsCopy() {
        PokerSolver s = new PokerSolverOnList("Ah", "Kd");
        s.getPlayerCards().clear();
        assertEquals(2, s.handSize());
    }

    /** Duplicates rejected. */
    @Test(expected = IllegalArgumentException.class)
    public void testDuplicate() {
        new PokerSolverOnList("Ah", "ah");
    }

    /** At most two cards. */
    @Test(expected = IllegalStateException.class)
    public void testThirdCard() {
        new PokerSolverOnList("Ah", "Kd", "Qc");
    }

    /** removeCard and clear. */
    @Test
    public void testRemoveAndClear() {
        PokerSolver s = new PokerSolverOnList("Ah", "Kd");
        assertEquals(Card.parse("Ah"), s.removeCard(0));
        assertEquals(1, s.handSize());
        s.clear();
        assertEquals(0, s.handSize());
    }

    /** equals / hashCode / toString. */
    @Test
    public void testEquals() {
        PokerSolver a = new PokerSolverOnList("Ah", "Kd");
        PokerSolver b = new PokerSolverOnList("Ah", "Kd");
        PokerSolver c = new PokerSolverOnList("Kd", "Ah");
        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
        assertNotEquals(a, c);
        assertEquals("[Ah, Kd]", a.toString());
    }

    /** Preflop recommendation. */
    @Test
    public void testRecommendation() {
        assertEquals(Action.RAISE, new PokerSolverOnList("Ah", "Ad")
                .recommendation(Position.UTG, 1));
        assertEquals(Action.FOLD, new PokerSolverOnList("7h", "2d")
                .recommendation(Position.UTG, 1));
        assertEquals("AKs", new PokerSolverOnList("Kh", "Ah").handCode());
    }

    /** Preflop advice matches the chart. */
    @Test
    public void testAdvisePreflop() {
        Advice a = new PokerSolverOnList("Ah", "Ad").advise(Position.BUTTON,
                List.of(), 1, 0, 1);
        assertEquals(Action.RAISE, a.action());
        assertEquals("Preflop", a.street());
        assertNull(a.madeHand());
    }

    /** Postflop: the nuts raises, air folds to a big bet, check if free. */
    @Test
    public void testAdvisePostflop() {
        List<Card> board = List.of(Card.parse("Qh"), Card.parse("Jh"),
                Card.parse("Th"));
        Advice nuts = new PokerSolverOnList("Ah", "Kh").advise(Position.CUTOFF,
                board, 1, 20, 10);
        assertEquals(Action.RAISE, nuts.action());
        assertEquals(HandEvaluator.Category.STRAIGHT_FLUSH, nuts.madeHand());
        assertEquals("Flop", nuts.street());

        Advice air = new PokerSolverOnList("2c", "3d").advise(Position.CUTOFF,
                board, 1, 20, 10);
        assertEquals(Action.FOLD, air.action());
        assertEquals(10.0 / 30.0, air.requiredEquity(), 1e-9);

        Advice free = new PokerSolverOnList("2c", "3d")
                .advise(Position.CUTOFF, board, 1, 20, 0);
        assertEquals(Action.CHECK, free.action());
        assertTrue(free.requiredEquity() == 0);
    }
}
