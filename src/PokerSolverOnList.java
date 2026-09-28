import java.util.ArrayList;
import java.util.List;

/**
 * {@link PokerSolver} kernel implemented on a {@link List} of {@link Card}s.
 *
 * @convention |$this.playerCards| <= MAX_CARDS and the cards are distinct
 * @correspondence hand = $this.playerCards
 */
public class PokerSolverOnList extends PokerSolverSecondary {

    /**
     * The player's hole cards.
     */
    private List<Card> playerCards;

    /**
     * Creates the empty representation.
     */
    private void createNewRep() {
        this.playerCards = new ArrayList<>();
    }

    /**
     * Creates an empty hand.
     */
    public PokerSolverOnList() {
        this.createNewRep();
    }

    /**
     * Creates a hand from card strings, e.g. {@code new
     * PokerSolverOnList("Ah", "Kh")}.
     *
     * @param cards
     *            the cards to add
     */
    public PokerSolverOnList(String... cards) {
        this.createNewRep();
        for (String c : cards) {
            this.addCard(c);
        }
    }

    @Override
    public final void addCard(String card) {
        if (card == null) {
            throw new IllegalArgumentException("Card is null");
        }
        if (this.playerCards.size() >= MAX_CARDS) {
            throw new IllegalStateException("Hand already has two cards");
        }
        Card c = Card.parse(card);
        if (this.playerCards.contains(c)) {
            throw new IllegalArgumentException("Duplicate card: " + c);
        }
        this.playerCards.add(c);
    }

    @Override
    public final Card removeCard(int index) {
        assert 0 <= index && index < this.playerCards.size()
                : "Violation of: 0 <= index < handSize()";
        return this.playerCards.remove(index);
    }

    @Override
    public final List<Card> getPlayerCards() {
        return new ArrayList<>(this.playerCards);
    }

    @Override
    public final int handSize() {
        return this.playerCards.size();
    }

    @Override
    public final void clear() {
        this.createNewRep();
    }
}
