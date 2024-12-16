import java.util.List;

import components.simplereader.SimpleReader;
import components.simplereader.SimpleReader1L;
import components.simplewriter.SimpleWriter;
import components.simplewriter.SimpleWriter1L;

/**
 * A class implementing a poker solver which provides suggestions for the player
 * based on hand, position, and bet size.
 */
public final class PokerSolverImplementation {

    /**
     * Private contructor.
     */
    private PokerSolverImplementation() {
    }

    /**
     * Main method to run the program.
     *
     * @param args
     *            command-line arguments
     */
    public static void main(String[] args) {
        SimpleReader in = new SimpleReader1L();
        SimpleWriter out = new SimpleWriter1L();

        boolean running = true;

        while (running) {
            // Display menu
            out.println("Position: ");
            out.println("1: UTG");
            out.println("2: UTG+1");
            out.println("3: UTG+2");
            out.println("4: Lojack");
            out.println("5: Hijack");
            out.println("6: Cutoff");
            out.println("7: Button");
            out.println("8: Small Blind");

            int position = Integer.parseInt(in.nextLine().trim());

            out.println("Enter Hand: ");
            String cards = in.nextLine().trim();

            out.println("Current Bet: ");
            Double currentBet = Double.parseDouble(in.nextLine().trim());

            PokerSolverOnList solver = new PokerSolverOnList();
            String[] cardArray = cards.split(",");

            for (String card : cardArray) {
                solver.addCard(card);
            }

            List<PokerSolverOnList.Card> playerCards = solver.getPlayerCards();
            String shouldCall = PokerSolverOnList.recommendation(position,
                    playerCards);

            out.println("\nPlayer Position: " + position);
            out.println("Current Bet: $" + currentBet);
            out.println("Player Cards: ");
            for (PokerSolverOnList.Card card : playerCards) {
                out.println(" - Value: " + card.getValue() + ", Suit: "
                        + card.getSuit());
            }

            out.println("Recommendation: " + shouldCall);

        }

        in.close();
        out.close();

    }
}
