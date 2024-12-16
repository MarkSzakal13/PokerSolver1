import java.util.ArrayList;
import java.util.List;

/**
 * Class to represent PokerSolver kernel methods including addCard,
 * getPlayerCards, getValues, getSuits, and parseCardValues.
 */
public class PokerSolverOnList {

    /**
     * Defines constants for all face cards.
     */
    public static final class CardConstants {
        /**
         * Constant for the value of a 3.
         */
        public static final int TWO = 2;

        /**
         * Constant for the value of a 3.
         */
        public static final int THREE = 3;

        /**
         * Constant for the value of a 4.
         */
        public static final int FOUR = 4;

        /**
         * Constant for the value of a 5.
         */
        public static final int FIVE = 5;

        /**
         * Constant for the value of an 6.
         */
        public static final int SIX = 6;

        /**
         * Constant for the value of a 7.
         */
        public static final int SEVEN = 7;

        /**
         * Constant for the value of a 8.
         */
        public static final int EIGHT = 8;

        /**
         * Constant for the value of a 9.
         */
        public static final int NINE = 9;

        /**
         * Constant for the value of an 10.
         */
        public static final int TEN = 10;

        /**
         * Constant for the value of a Jack.
         */
        public static final int JACK = 11;

        /**
         * Constant for the value of a Queen.
         */
        public static final int QUEEN = 12;

        /**
         * Constant for the value of a King.
         */
        public static final int KING = 13;

        /**
         * Constant for the value of an Ace.
         */
        public static final int ACE = 14;

        /**
         *
         */
        private CardConstants() {

        }
    }

    /**
     * Class to represent each card.
     */
    public static class Card {
        /**
         * value of the card.
         */
        private final String value;
        /**
         * suit of the card.
         */
        private final String suit;

        /**
         * Constructs card using value and suit.
         *
         * @param value
         *            The value of each card.
         * @param suit
         *            The suit of each card.
         */
        public Card(String value, String suit) {
            this.value = value;
            this.suit = suit;
        }

        /**
         * Returns the value of the card.
         *
         * @return The value of the card.
         */
        public String getValue() {
            return this.value;
        }

        /**
         * Returns the suit of the card.
         *
         * @return The suit of the card.
         */
        public String getSuit() {
            return this.suit;
        }

        /**
         * Converts value and suit to string.
         */
        @Override
        public String toString() {
            return this.value + this.suit;
        }
    }

    /**
     * Initializes playerCards.
     */
    private List<Card> playerCards;

    /**
     * Constructs an empty hand by setting playerCards, values, and suits all to
     * null.
     */
    public PokerSolverOnList() {
        this.createNewRep();
    }

    /**
     * Creates a new rep of PokerSolverOnX.
     */
    private void createNewRep() {
        this.playerCards = new ArrayList<>();
    }

    /**
     * Adds cards to the player's hand by passing in value and suit.
     *
     * @param card
     *            The card being imported.
     * @requires card != null
     * @ensures The card is added to the players hand with value and suit being
     *          stored.
     */
    public void addCard(String card) {
        int index = -1;

        for (char suit : new char[] { 'c', 'h', 'd', 's' }) {
            int suitIndex = card.indexOf(suit);
            if (suitIndex != -1 && (index == -1 || suitIndex < index)) {
                index = suitIndex;
            }
        }

        String value = card.substring(0, index).trim();
        String suit = card.substring(index, index + 1);

        this.playerCards.add(new Card(value, suit));
    }

    /**
     * Gets the players cards.
     *
     * @return Returns the players cards.
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
        List<String> suits = new ArrayList<>();

        for (Card card : playerCards) {
            int numVal = parseCardValue(card.getValue());
            values.add(numVal);
            suits.add(card.getSuit());
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
            List<String> suits) {

        int value1 = values.get(0);
        int value2 = values.get(1);
        boolean isSuited = suits.get(0).equals(suits.get(1));

        if (value1 == value2 && value1 >= CardConstants.FIVE) {
            return "Raise";
        }

        if (isSuited) {
            if (value1 == CardConstants.ACE && value2 >= CardConstants.THREE) {
                return "Raise";
            }
            if (value1 >= CardConstants.TEN && value2 >= CardConstants.NINE) {
                if (!(value1 == CardConstants.JACK
                        && value2 == CardConstants.NINE)) {
                    return "Raise";
                }
            }
        } else {
            if (value1 == CardConstants.ACE && value2 >= CardConstants.JACK) {
                return "Raise";
            }
            if (value1 == CardConstants.KING && value2 == CardConstants.QUEEN) {
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
            List<String> suits) {

        int value1 = values.get(0);
        int value2 = values.get(1);
        boolean isSuited = suits.get(0).equals(suits.get(1));

        if (value1 == value2 && value1 >= CardConstants.SIX) {
            return "Raise";
        }

        if (isSuited) {
            if (value1 == CardConstants.ACE && value2 >= CardConstants.THREE) {
                return "Raise";
            }
            if (value1 >= CardConstants.NINE && value2 >= CardConstants.EIGHT) {
                if (!(value1 == CardConstants.QUEEN
                        || value1 == CardConstants.JACK
                                && value2 == CardConstants.EIGHT)) {
                    return "Raise";
                }
            }
        } else {
            if (value1 == CardConstants.ACE && value2 >= CardConstants.TEN) {
                return "Raise";
            }
            if (value1 == CardConstants.KING && value2 == CardConstants.QUEEN) {
                return "Raise";
            }
        }
        return "Fold";
    }

    /**
     * Converts face cards to numeric values.
     *
     * @param value
     *            String representation of the card values.
     * @return The numeric value of the card.
     * @ensures parseCardValue = corresponding numeric value of face card.
     */
    private static int parseCardValue(String value) {
        switch (value) {
            case "j":
                return CardConstants.JACK;
            case "q":
                return CardConstants.QUEEN;
            case "k":
                return CardConstants.KING;
            case "a":
                return CardConstants.ACE;
            default:
                return Integer.parseInt(value);
        }
    }
}
