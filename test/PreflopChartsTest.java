import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.HashSet;
import java.util.Set;

import org.junit.Test;

/**
 * Tests for {@link PreflopCharts}.
 */
public class PreflopChartsTest {

    /** Hand codes. */
    @Test
    public void testHandCode() {
        assertEquals("AKs",
                PreflopCharts.handCode(Card.parse("Kh"), Card.parse("Ah")));
        assertEquals("T9o",
                PreflopCharts.handCode(Card.parse("9c"), Card.parse("Td")));
        assertEquals("77",
                PreflopCharts.handCode(Card.parse("7c"), Card.parse("7d")));
    }

    /** Range shorthand. */
    @Test
    public void testParseRange() {
        assertEquals(Set.of("QQ", "KK", "AA"), PreflopCharts.parseRange("QQ+"));
        assertEquals(Set.of("TT", "99", "88"),
                PreflopCharts.parseRange("TT-88"));
        assertEquals(Set.of("AJs", "AQs", "AKs"),
                PreflopCharts.parseRange("AJs+"));
        assertEquals(Set.of("A5s", "A4s", "A3s"),
                PreflopCharts.parseRange("A5s-A3s"));
        assertEquals(Set.of("AKs", "AKo"), PreflopCharts.parseRange("AK"));
    }

    /** The 13x13 grid covers all 169 hands exactly once. */
    @Test
    public void testGrid() {
        Set<String> all = new HashSet<>();
        for (int r = 0; r < 13; r++) {
            for (int c = 0; c < 13; c++) {
                all.add(PreflopCharts.gridCode(r, c));
            }
        }
        assertEquals(169, all.size());
        assertEquals("AA", PreflopCharts.gridCode(0, 0));
        assertEquals("AKs", PreflopCharts.gridCode(0, 1));
        assertEquals("AKo", PreflopCharts.gridCode(1, 0));
        assertEquals("22", PreflopCharts.gridCode(12, 12));
    }

    /** Situations by bet size. */
    @Test
    public void testSituation() {
        assertEquals(PreflopCharts.Situation.UNOPENED,
                PreflopCharts.situation(1));
        assertEquals(PreflopCharts.Situation.FACING_OPEN,
                PreflopCharts.situation(3));
        assertEquals(PreflopCharts.Situation.FACING_3BET,
                PreflopCharts.situation(9));
        assertEquals(PreflopCharts.Situation.FACING_ALL_IN,
                PreflopCharts.situation(100));
    }

    /** Opening ranges widen from UTG to the button. */
    @Test
    public void testRangesWiden() {
        Position[] order = { Position.UTG, Position.UTG1, Position.UTG2,
            Position.LOJACK, Position.HIJACK, Position.CUTOFF,
            Position.BUTTON };
        int prev = 0;
        for (Position p : order) {
            int n = 0;
            for (int r = 0; r < 13; r++) {
                for (int c = 0; c < 13; c++) {
                    if (PreflopCharts.action(p,
                            PreflopCharts.Situation.UNOPENED,
                            PreflopCharts.gridCode(r, c)) == Action.RAISE) {
                        n++;
                    }
                }
            }
            assertTrue(p + " should open at least as wide", n >= prev);
            prev = n;
        }
    }

    /** Sample chart decisions. */
    @Test
    public void testActions() {
        PreflopCharts.Situation open = PreflopCharts.Situation.UNOPENED;
        assertEquals(Action.RAISE,
                PreflopCharts.action(Position.UTG, open, "AA"));
        assertEquals(Action.FOLD,
                PreflopCharts.action(Position.UTG, open, "72o"));
        assertEquals(Action.RAISE,
                PreflopCharts.action(Position.BUTTON, open, "K2s"));
        assertEquals(Action.CHECK,
                PreflopCharts.action(Position.BIG_BLIND, open, "72o"));
        assertEquals(Action.CALL, PreflopCharts.action(Position.BIG_BLIND,
                PreflopCharts.Situation.FACING_OPEN, "T9s"));
        assertEquals(Action.RAISE, PreflopCharts.action(Position.CUTOFF,
                PreflopCharts.Situation.FACING_3BET, "KK"));
        assertEquals(Action.FOLD, PreflopCharts.action(Position.CUTOFF,
                PreflopCharts.Situation.FACING_ALL_IN, "TT"));
    }
}
