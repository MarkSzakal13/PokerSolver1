import java.util.List;

/**
 * Evaluates 5 to 7 card poker hands.
 *
 * <p>
 * {@link #evaluate(int[], int)} returns an integer score where a larger score
 * is a stronger hand, so two hands are compared with a plain integer
 * comparison. The hand category lives in the high bits (see
 * {@link #category(int)}), followed by up to five tie-breaking ranks.
 */
public final class HandEvaluator {

    /**
     * Hand categories, weakest to strongest.
     */
    public enum Category {
        /** Categories. */
        HIGH_CARD("High Card"), PAIR("One Pair"), TWO_PAIR("Two Pair"),
        /** Categories. */
        THREE_OF_A_KIND("Three of a Kind"), STRAIGHT("Straight"),
        /** Categories. */
        FLUSH("Flush"), FULL_HOUSE("Full House"),
        /** Categories. */
        FOUR_OF_A_KIND("Four of a Kind"), STRAIGHT_FLUSH("Straight Flush");

        /**
         * Human-readable name.
         */
        private final String label;

        /**
         * Constructor.
         *
         * @param label
         *            display name
         */
        Category(String label) {
            this.label = label;
        }

        @Override
        public String toString() {
            return this.label;
        }
    }

    /** Number of ranks. */
    private static final int RANKS = 13;

    /** Number of suits. */
    private static final int SUITS = 4;

    /** Bits per tie-breaker nibble. */
    private static final int NIBBLE = 4;

    /** Shift of the category bits. */
    private static final int CATEGORY_SHIFT = 20;

    /** Index of the ace rank. */
    private static final int ACE = 12;

    /** Number of cards in a made hand. */
    private static final int HAND = 5;

    /** Bit mask for the wheel straight A-2-3-4-5. */
    private static final int WHEEL = (1 << ACE) | 0b1111;

    /**
     * Private constructor: static utility class.
     */
    private HandEvaluator() {
    }

    /**
     * Evaluates a list of 5 to 7 cards.
     *
     * @param cards
     *            the cards
     * @return the hand score (higher is better)
     */
    public static int evaluate(List<Card> cards) {
        int[] idx = new int[cards.size()];
        for (int i = 0; i < idx.length; i++) {
            idx[i] = cards.get(i).index();
        }
        return evaluate(idx, idx.length);
    }

    /**
     * Evaluates the first {@code n} card indices (see {@link Card#index()}).
     *
     * @param cards
     *            card indices
     * @param n
     *            how many of them to use (5..7)
     * @return the hand score (higher is better)
     */
    public static int evaluate(int[] cards, int n) {
        int[] rankCount = new int[RANKS];
        int[] suitMask = new int[SUITS];
        int[] suitCount = new int[SUITS];
        int rankMask = 0;
        for (int i = 0; i < n; i++) {
            int r = cards[i] / SUITS;
            int s = cards[i] % SUITS;
            rankCount[r]++;
            suitCount[s]++;
            suitMask[s] |= 1 << r;
            rankMask |= 1 << r;
        }

        // Flush / straight flush
        for (int s = 0; s < SUITS; s++) {
            if (suitCount[s] >= HAND) {
                int sfTop = straightTop(suitMask[s]);
                if (sfTop >= 0) {
                    return score(Category.STRAIGHT_FLUSH, sfTop);
                }
                return score(Category.FLUSH, topBits(suitMask[s], HAND));
            }
        }

        int quad = -1;
        int trip1 = -1;
        int trip2 = -1;
        int pair1 = -1;
        int pair2 = -1;
        for (int r = ACE; r >= 0; r--) {
            switch (rankCount[r]) {
                case 4:
                    quad = r;
                    break;
                case 3:
                    if (trip1 < 0) {
                        trip1 = r;
                    } else if (trip2 < 0) {
                        trip2 = r;
                    }
                    break;
                case 2:
                    if (pair1 < 0) {
                        pair1 = r;
                    } else if (pair2 < 0) {
                        pair2 = r;
                    }
                    break;
                default:
                    break;
            }
        }

        if (quad >= 0) {
            int[] kicker = topBits(rankMask & ~(1 << quad), 1);
            return score(Category.FOUR_OF_A_KIND, quad, kicker[0]);
        }
        if (trip1 >= 0 && (trip2 >= 0 || pair1 >= 0)) {
            int pairPart = Math.max(trip2, pair1);
            return score(Category.FULL_HOUSE, trip1, pairPart);
        }
        int straight = straightTop(rankMask);
        if (straight >= 0) {
            return score(Category.STRAIGHT, straight);
        }
        if (trip1 >= 0) {
            int[] k = topBits(rankMask & ~(1 << trip1), 2);
            return score(Category.THREE_OF_A_KIND, trip1, k[0], k[1]);
        }
        if (pair1 >= 0 && pair2 >= 0) {
            int[] k = topBits(rankMask & ~(1 << pair1) & ~(1 << pair2), 1);
            return score(Category.TWO_PAIR, pair1, pair2, k[0]);
        }
        if (pair1 >= 0) {
            int[] k = topBits(rankMask & ~(1 << pair1), 3);
            return score(Category.PAIR, pair1, k[0], k[1], k[2]);
        }
        return score(Category.HIGH_CARD, topBits(rankMask, HAND));
    }

    /**
     * Extracts the category from a score.
     *
     * @param score
     *            a score from {@link #evaluate}
     * @return the hand category
     */
    public static Category category(int score) {
        return Category.values()[score >>> CATEGORY_SHIFT];
    }

    /**
     * Returns the rank of the highest straight in a rank mask, or -1.
     *
     * @param mask
     *            bit mask of ranks
     * @return the top rank index of the best straight, or -1 if none
     */
    private static int straightTop(int mask) {
        final int five = 0b11111;
        for (int top = ACE; top >= HAND - 1; top--) {
            int need = five << (top - (HAND - 1));
            if ((mask & need) == need) {
                return top;
            }
        }
        if ((mask & WHEEL) == WHEEL) {
            return HAND - 2; // five-high: rank index of the 5
        }
        return -1;
    }

    /**
     * Returns the {@code count} highest set bits of a rank mask, highest
     * first.
     *
     * @param mask
     *            bit mask of ranks
     * @param count
     *            how many ranks to return
     * @return the rank indices
     */
    private static int[] topBits(int mask, int count) {
        int[] out = new int[count];
        int k = 0;
        for (int r = ACE; r >= 0 && k < count; r--) {
            if ((mask & (1 << r)) != 0) {
                out[k] = r;
                k++;
            }
        }
        return out;
    }

    /**
     * Packs a category and tie-breakers into a score.
     *
     * @param c
     *            the category
     * @param ranks
     *            tie-breaking rank indices, most significant first
     * @return the packed score
     */
    private static int score(Category c, int... ranks) {
        int s = c.ordinal() << CATEGORY_SHIFT;
        int shift = CATEGORY_SHIFT - NIBBLE;
        for (int r : ranks) {
            s |= r << shift;
            shift -= NIBBLE;
        }
        return s;
    }
}
