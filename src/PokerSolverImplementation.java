import java.util.List;
import java.util.Scanner;

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
        Scanner scanner = new Scanner(System.in);

        boolean running = true;

        while (running) {
            // Display menu
            System.out.println("Position: ");
            System.out.println("1: UTG");
            System.out.println("2: UTG+1");
            System.out.println("3: UTG+2");
            System.out.println("4: Lojack");
            System.out.println("5: Hijack");
            System.out.println("6: Cutoff");
            System.out.println("7: Button");
            System.out.println("8: Small Blind");

            int position = Integer.parseInt(scanner.nextLine().trim());

            System.out.println("Enter Hand: ");
            String cards = scanner.nextLine().trim();

            System.out.println("Current Bet: ");
            Double currentBet = Double.parseDouble(scanner.nextLine().trim());

            PokerSolverOnList solver = new PokerSolverOnList();
            String[] cardArray = cards.split(",");

            for (String card : cardArray) {
                solver.addCard(card);
            }

            List<PokerSolverOnList.Card> playerCards = solver.getPlayerCards();
            String shouldCall = PokerSolverOnList.recommendation(position,
                    playerCards);

            System.out.println("\nPlayer Position: " + position);
            System.out.println("Current Bet: $" + currentBet);
            System.out.println("Player Cards: ");
            for (PokerSolverOnList.Card card : playerCards) {
                System.out.println(" - Value: " + card.value().name()
                        + ", Suit: " + card.suit().getSuit());
            }

            System.out.println("Recommendation: " + shouldCall);

        }

        scanner.close();
    }
}
