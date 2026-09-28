/**
 * An immutable playing card made of a {@link Rank} and a {@link Suit}.
 *
 * <p>
 * Cards can be parsed from short strings such as {@code "Ah"}, {@code "td"},
 * {@code "10s"} or {@code "9c"} and every card maps to a unique index in
 * {@code [0, 52)} which the hand evaluator uses internally.
 *
 * @param rank
 *            the rank of the card
 * @param suit
 *            the suit of the card
 */
public record Card(Rank rank, Suit suit) implements java.io.Serializable {

    /**
     * Number of cards in a standard deck.
     */
    public static final int DECK_SIZE = 52;

    /**
     * Card ranks, two through ace.
     */
    public enum Rank {
        /** Rank values. */
        TWO(2, '2'), THREE(3, '3'), FOUR(4, '4'), FIVE(5, '5'), SIX(6, '6'),
        /** Rank values. */
        SEVEN(7, '7'), EIGHT(8, '8'), NINE(9, '9'), TEN(10, 'T'),
        /** Rank values. */
        JACK(11, 'J'), QUEEN(12, 'Q'), KING(13, 'K'), ACE(14, 'A');

        /**
         * Numeric value (2..14).
         */
        private final int value;

        /**
         * Single-character symbol.
         */
        private final char symbol;

        /**
         * Constructor.
         *
         * @param value
         *            numeric value
         * @param symbol
         *            display symbol
         */
        Rank(int value, char symbol) {
            this.value = value;
            this.symbol = symbol;
        }

        /**
         * @return the numeric value of this rank (2..14)
         */
        public int getValue() {
            return this.value;
        }

        /**
         * @return the one-character symbol of this rank (e.g. 'T', 'A')
         */
        public char getSymbol() {
            return this.symbol;
        }

        /**
         * Parses a rank. Accepts 2-9, T/10, J, Q, K, A (case-insensitive).
         *
         * @param text
         *            the rank text
         * @return the matching rank
         * @throws IllegalArgumentException
         *             if the text is not a rank
         */
        public static Rank parse(String text) {
            String t = text.trim().toUpperCase();
            if (t.equals("10")) {
                return TEN;
            }
            if (t.length() == 1) {
                for (Rank r : values()) {
                    if (r.symbol == t.charAt(0)) {
                        return r;
                    }
                }
            }
            throw new IllegalArgumentException("Invalid rank: " + text);
        }
    }

    /**
     * Card suits.
     */
    public enum Suit {
        /** Suits. */
        CLUBS('c', '♣'), DIAMONDS('d', '♦'), HEARTS('h', '♥'),
        /** Suits. */
        SPADES('s', '♠');

        /**
         * Letter used when parsing / printing.
         */
        private final char letter;

        /**
         * Unicode symbol for display.
         */
        private final char symbol;

        /**
         * Constructor.
         *
         * @param letter
         *            the suit letter
         * @param symbol
         *            the unicode symbol
         */
        Suit(char letter, char symbol) {
            this.letter = letter;
            this.symbol = symbol;
        }

        /**
         * @return the suit letter (c, d, h, s)
         */
        public char getSuit() {
            return this.letter;
        }

        /**
         * @return the unicode suit symbol
         */
        public char getSymbol() {
            return this.symbol;
        }

        /**
         * @return true for hearts and diamonds
         */
        public boolean isRed() {
            return this == HEARTS || this == DIAMONDS;
        }

        /**
         * Parses a suit letter or unicode symbol (case-insensitive).
         *
         * @param c
         *            the suit character
         * @return the matching suit
         * @throws IllegalArgumentException
         *             if the character is not a suit
         */
        public static Suit parse(char c) {
            char lc = Character.toLowerCase(c);
            for (Suit s : values()) {
                if (s.letter == lc || s.symbol == c) {
                    return s;
                }
            }
            throw new IllegalArgumentException("Invalid suit: " + c);
        }
    }

    /**
     * Compact constructor: rejects nulls.
     *
     * @param rank
     *            the rank
     * @param suit
     *            the suit
     */
    public Card {
        if (rank == null || suit == null) {
            throw new IllegalArgumentException("Rank and suit are required");
        }
    }

    /**
     * Parses a card such as "Ah", "10d", "Tc" or "9s".
     *
     * @param text
     *            the card text
     * @return the parsed card
     * @throws IllegalArgumentException
     *             if the text is not a valid card
     */
    public static Card parse(String text) {
        if (text == null) {
            throw new IllegalArgumentException("Card text is null");
        }
        String t = text.trim();
        if (t.length() < 2 || t.length() > 3) {
            throw new IllegalArgumentException("Invalid card: " + text);
        }
        Suit suit = Suit.parse(t.charAt(t.length() - 1));
        Rank rank = Rank.parse(t.substring(0, t.length() - 1));
        return new Card(rank, suit);
    }

    /**
     * @return a unique index in [0, 52): rank-major, suit-minor
     */
    public int index() {
        return this.rank.ordinal() * Suit.values().length
                + this.suit.ordinal();
    }

    /**
     * Returns the card with the given index.
     *
     * @param index
     *            an index in [0, 52)
     * @return the card
     */
    public static Card fromIndex(int index) {
        int n = Suit.values().length;
        return new Card(Rank.values()[index / n], Suit.values()[index % n]);
    }

    /**
     * @return a pretty string such as "A♥"
     */
    public String pretty() {
        return "" + this.rank.getSymbol() + this.suit.getSymbol();
    }

    @Override
    public String toString() {
        return "" + this.rank.getSymbol() + this.suit.getSuit();
    }
}
