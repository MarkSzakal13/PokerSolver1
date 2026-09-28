import java.util.List;

/**
 * Kernel interface for a poker solver. The kernel models the hero's hand: a
 * collection of at most two distinct hole cards.
 */
public interface PokerSolverKernel {

    /**
     * Maximum number of hole cards (Texas Hold'em).
     */
    int MAX_CARDS = 2;

    /**
     * Adds a card to the player's hand.
     *
     * @param card
     *            the card, e.g. "Ah", "10d", "Tc"
     * @throws IllegalArgumentException
     *             if the card is malformed or already in the hand
     * @requires card != null and handSize() < MAX_CARDS
     * @ensures the parsed card is appended to the hand
     */
    void addCard(String card);

    /**
     * Removes and returns the card at a position in the hand.
     *
     * @param index
     *            position of the card
     * @return the removed card
     * @requires 0 <= index < handSize()
     * @ensures the card at index is removed from the hand
     */
    Card removeCard(int index);

    /**
     * Returns a copy of the cards in the player's hand.
     *
     * @return the player's cards, in the order added
     * @ensures the hand is unchanged
     */
    List<Card> getPlayerCards();

    /**
     * Returns the number of cards in the hand.
     *
     * @return the hand size
     * @ensures handSize = |hand|
     */
    int handSize();

    /**
     * Empties the hand.
     *
     * @ensures handSize() = 0
     */
    void clear();
}
