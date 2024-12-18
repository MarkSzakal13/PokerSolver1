import java.util.ArrayList;
import java.util.List;

/**
 * Class to represent PokerSolver kernel methods including addCard,
 * getPlayerCards, and recommendations for poker hands.
 */
public class PokerSolverOnList {

    /**
     * Enum to represent values of cards.
     */
    public enum Rank {
        /**
         * Enum values 2 through ace.
         */
        TWO(2), THREE(3), FOUR(4), FIVE(5), SIX(6), SEVEN(7), EIGHT(8), NINE(
                9), TEN(10), JACK(11), QUEEN(12), KING(13), ACE(14);

        /**
         * Value representing each card.
         */
        private final int value;

        /**
         * Constructor for rank.
         *
         * @param value
         *            The numeric value of rank.
         */
        Rank(int value) {
            this.value = value;
        }

        /**
         * Gets the numeric value.
         *
         * @return The numeric value of the rank.
         */
        public int getValue() {
            return this.value;
        }

        /**
         * Parses a string representation into an enum.
         *
         * @param value
         *            The string version of the rank.
         * @return The rank enum.
         */
        public static Rank parseCardValue(String value) {
            switch (value.toLowerCase()) {
                case "j":
                    return JACK;
                case "q":
                    return QUEEN;
                case "k":
                    return KING;
                case "a":
                    return ACE;
                default:
                    return values()[Integer.parseInt(value) - 2];
            }
        }
    }

    /**
     * Enum for suits.
     */
    public enum Suit {
        /**
         * Enums representing each of the 4 suits.
         */
        CLUBS('c'), HEARTS('h'), DIAMONDS('d'), SPADES('s');

        /**
         * Suit for each card.
         */
        private final char suit;

        /**
         * Constructor for suit.
         *
         * @param suit
         *            The suit of the card.
         */
        Suit(char suit) {
            this.suit = suit;
        }

        /**
         * Gets the suit.
         *
         * @return The cards suit.
         */
        public char getSuit() {
            return this.suit;
        }

        /**
         * Parses the char version of the suit into a Suit enum.
         *
         * @param suitChar
         *            The char representation of the suit.
         * @return The suit enum.
         */
        public static Suit parseCardSuit(char suitChar) {
            for (Suit suit : values()) {
                if (suit.suit == suitChar) {
                    return suit;
                }
            }
            throw new IllegalArgumentException("Invalid suit: " + suitChar);
        }
    }

    /**
     * Record for a card which includes value and suit.
     *
     * @param value
     *            The value of the card.
     * @param suit
     *            The value of the suit.
     */
    public record Card(Rank value, Suit suit) {
        @Override
        public String toString() {
            return this.value.name() + this.suit.getSuit();
        }
    }

    /**
     * Private list to store the players cards.
     */
    private List<Card> playerCards;

    /**
     * Constructor for PokerSolverOnList.
     */
    public PokerSolverOnList() {
        this.createNewRep();
    }

    /**
     * Creates an empty, new hand.
     */
    private void createNewRep() {
        this.playerCards = new ArrayList<>();
    }

    /**
     * Adds card to the players hand.
     *
     * @param card
     *            The version of the card.
     */
    public void addCard(String card) {
        int index = -1;

        for (Suit suit : Suit.values()) {
            int suitIndex = card.indexOf(suit.getSuit());
            if (suitIndex != -1 && (index == -1 || suitIndex < index)) {
                index = suitIndex;
            }
        }

        if (index == -1) {
            throw new IllegalArgumentException("Invalid card format: " + card);
        }

        String valueChar = card.substring(0, index).trim();
        char suitChar = card.charAt(index);

        Rank rank = Rank.parseCardValue(valueChar);
        Suit suit = Suit.parseCardSuit(suitChar);

        this.playerCards.add(new Card(rank, suit));
    }

    /**
     * Gets a list of the players cards.
     *
     * @return A list of the players cards.
     */
    public List<Card> getPlayerCards() {
        return new ArrayList<>(this.playerCards);
    }

    /**
     * Provides a recommendation for the user based on their position and cards.
     *
     * @param position
     *            The players current position at the table.
     * @param playerCards
     *            The players current cards.
     * @return The recommendation for the player to raise, call, or fold.
     */
    public static String recommendation(int position, List<Card> playerCards) {
        List<Integer> values = new ArrayList<>();
        List<Suit> suits = new ArrayList<>();

        for (Card card : playerCards) {
            values.add(card.value().getValue());
            suits.add(card.suit());
        }

        reOrder(values);

        switch (position) {
            case 1:
                return recommendationForUTG(values, suits);
            case 2:
                return recommendationForUTG1(values, suits);
            default:
                return "Fold";
        }
    }

    /**
     * Reorders the values so the larger one comes first.
     *
     * @param values
     *            values of each card.
     */
    public static void reOrder(List<Integer> values) {
        if (values.get(1) > values.get(0)) {
            int temp = values.get(0);
            values.set(0, values.get(1));
            values.set(1, temp);
        }
    }

    /**
     * Provides a specific recommendation for a player UTG.
     *
     * @param values
     *            values of each card.
     * @param suits
     *            suits of each card.
     * @return The recommendation for the player to raise, call, or fold.
     */
    public static String recommendationForUTG(List<Integer> values,
            List<Suit> suits) {
        int value1 = values.get(0);
        int value2 = values.get(1);
        boolean isSuited = suits.get(0).equals(suits.get(1));

        if (value1 == value2 && value1 >= Rank.FIVE.getValue()) {
            return "Raise";
        }

        if (isSuited) {
            if (value1 == Rank.ACE.getValue()
                    && value2 >= Rank.THREE.getValue()) {
                return "Raise";
            }
            if (value1 >= Rank.TEN.getValue()
                    && value2 >= Rank.NINE.getValue()) {
                if (!(value1 == Rank.JACK.getValue()
                        && value2 == Rank.NINE.getValue())) {
                    return "Raise";
                }
            }
        } else {
            if (value1 == Rank.ACE.getValue()
                    && value2 >= Rank.JACK.getValue()) {
                return "Raise";
            }
            if (value1 == Rank.KING.getValue()
                    && value2 == Rank.QUEEN.getValue()) {
                return "Raise";
            }
        }
        return "Fold";
    }

    /**
     * Provides a specific recommendation for a player UTG+1.
     *
     * @param values
     *            values of each card.
     * @param suits
     *            suits of each card.
     * @return The recommendation for the player to raise, call, or fold.
     */
    public static String recommendationForUTG1(List<Integer> values,
            List<Suit> suits) {
        int value1 = values.get(0);
        int value2 = values.get(1);
        boolean isSuited = suits.get(0).equals(suits.get(1));

        if (value1 == value2 && value1 >= Rank.SIX.getValue()) {
            return "Raise";
        }

        if (isSuited) {
            if (value1 == Rank.ACE.getValue()
                    && value2 >= Rank.THREE.getValue()) {
                return "Raise";
            }
            if (value1 >= Rank.NINE.getValue()
                    && value2 >= Rank.EIGHT.getValue()) {
                if (!(value1 == Rank.QUEEN.getValue()
                        || value1 == Rank.JACK.getValue()
                                && value2 == Rank.EIGHT.getValue())) {
                    return "Raise";
                }
            }
        } else {
            if (value1 == Rank.ACE.getValue()
                    && value2 >= Rank.TEN.getValue()) {
                return "Raise";
            }
            if (value1 == Rank.KING.getValue()
                    && value2 == Rank.QUEEN.getValue()) {
                return "Raise";
            }
        }
        return "Fold";
    }
}
