import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/**
 * Tests for {@link Card}.
 */
public class CardTest {

    /** Parsing common formats. */
    @Test
    public void testParse() {
        assertEquals(new Card(Card.Rank.ACE, Card.Suit.HEARTS),
                Card.parse("Ah"));
        assertEquals(new Card(Card.Rank.TEN, Card.Suit.DIAMONDS),
                Card.parse("10d"));
        assertEquals(new Card(Card.Rank.TEN, Card.Suit.CLUBS),
                Card.parse("tc"));
        assertEquals(new Card(Card.Rank.TWO, Card.Suit.SPADES),
                Card.parse(" 2S "));
    }

    /** Bad input is rejected. */
    @Test(expected = IllegalArgumentException.class)
    public void testParseBadSuit() {
        Card.parse("Ax");
    }

    /** Bad input is rejected. */
    @Test(expected = IllegalArgumentException.class)
    public void testParseBadRank() {
        Card.parse("1h");
    }

    /** Index round-trips for every card. */
    @Test
    public void testIndexRoundTrip() {
        for (int i = 0; i < Card.DECK_SIZE; i++) {
            assertEquals(i, Card.fromIndex(i).index());
        }
    }

    /** toString / pretty. */
    @Test
    public void testToString() {
        assertEquals("Ts", Card.parse("10s").toString());
        assertTrue(Card.parse("Kh").pretty().startsWith("K"));
    }
}
