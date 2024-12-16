import java.util.List;

import components.standard.Standard;

/**
 * Kernel interface for a poker solver.
 */
public interface PokerSolverKernel extends Standard<PokerSolverOnList> {

    /**
     * Adds a card to players hand.
     *
     * @param card
     *            The card in the expected format.
     * @requires card != null
     * @ensures c\The card is added to the player's hand.
     */
    void addCard(String card);

    /**
     * Returns list of cards in the player's hand.
     *
     * @return A list of the players cards.
     * @ensures getPlayerCards returns the players hand.
     */
    List<PokerSolverOnList.Card> getPlayerCards();

    /**
     * Provides a recommendation for the player based on position and cards.
     *
     * @param position
     *            The player's position at the table.
     * @param playerCards
     *            The player's cards.
     * @return if the player should call, raise, or fold.
     * @requires position != 0 && playerCards != null
     * @ensures recommendation = raise, call, or fold
     */
    boolean recommendation(int position,
            List<PokerSolverOnList.Card> playerCards);

    /**
     * Reorders values so the larger one is first in the list.
     *
     * @param values
     *            values of each card.
     */
    void reOrder(List<Integer> values);

    /**
     * Provides a specific recommendation for a player UTG.
     *
     * @param values
     *            values of each card.
     * @param suits
     *            suits of each card.
     * @return if the player should call, raise or fold.
     * @requires values != null && suits != null
     * @ensures recommendationForUTG = raise, call, or fold
     */
    boolean recommendationForUTG(List<Integer> values, List<String> suits);

    /**
     * Converts face cards to their numeric values.
     *
     * @param value
     *            String representation of card values.
     * @return The numeric value of the card.
     * @ensures parseCardValue = corresponding numeric value of face card.
     */
    int parseCardValue(String value);
}
