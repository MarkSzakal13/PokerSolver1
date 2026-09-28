import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * Console front end for the poker solver. Run {@link PokerSolverUI} for the
 * graphical version.
 */
public final class PokerSolverImplementation {

    /**
     * Private constructor.
     */
    private PokerSolverImplementation() {
    }

    /**
     * Prompts until the user enters a number (or "q" to quit).
     *
     * @param in
     *            input scanner
     * @param prompt
     *            prompt text
     * @param min
     *            smallest allowed value
     * @param max
     *            largest allowed value
     * @return the value, or NaN if the user quit
     */
    private static double readNumber(Scanner in, String prompt, double min,
            double max) {
        while (true) {
            System.out.print(prompt);
            if (!in.hasNextLine()) {
                return Double.NaN;
            }
            String line = in.nextLine().trim();
            if (line.equalsIgnoreCase("q")) {
                return Double.NaN;
            }
            try {
                double v = Double.parseDouble(line);
                if (v >= min && v <= max) {
                    return v;
                }
            } catch (NumberFormatException e) {
                // fall through to the retry message
            }
            System.out.println("  Please enter a number from " + min + " to "
                    + max + ".");
        }
    }

    /**
     * Main method to run the program.
     *
     * @param args
     *            command-line arguments
     */
    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        System.out.println("=== Poker Solver (enter q at any prompt to quit)"
                + " ===");

        while (true) {
            System.out.println();
            Position[] seats = Position.values();
            for (int i = 0; i < seats.length; i++) {
                System.out.println((i + 1) + ": " + seats[i]);
            }
            double pos = readNumber(in, "Position: ", 1, seats.length);
            if (Double.isNaN(pos)) {
                break;
            }
            Position position = Position.fromNumber((int) pos);

            PokerSolver solver = new PokerSolverOnList();
            while (solver.handSize() < PokerSolverKernel.MAX_CARDS) {
                System.out.print("Enter hand (e.g. Ah,Kd): ");
                if (!in.hasNextLine()) {
                    in.close();
                    return;
                }
                String line = in.nextLine().trim();
                if (line.equalsIgnoreCase("q")) {
                    in.close();
                    return;
                }
                solver.clear();
                try {
                    for (String c : line.split("[,\\s]+")) {
                        solver.addCard(c);
                    }
                } catch (IllegalArgumentException
                        | IllegalStateException e) {
                    System.out.println("  " + e.getMessage());
                    solver.clear();
                }
            }

            List<Card> board = new ArrayList<>();
            System.out.print("Board cards (blank for preflop): ");
            String boardLine = in.hasNextLine() ? in.nextLine().trim() : "";
            try {
                if (!boardLine.isEmpty()) {
                    for (String c : boardLine.split("[,\\s]+")) {
                        board.add(Card.parse(c));
                    }
                }
            } catch (IllegalArgumentException e) {
                System.out.println("  " + e.getMessage()
                        + " - ignoring board.");
                board.clear();
            }

            double opp = readNumber(in, "Opponents in hand (1-8): ", 1,
                    EquityCalculator.MAX_OPPONENTS);
            if (Double.isNaN(opp)) {
                break;
            }
            double pot = 0;
            if (!board.isEmpty()) {
                pot = readNumber(in, "Pot in BB (including bet to call): ", 0,
                        Double.MAX_VALUE);
                if (Double.isNaN(pot)) {
                    break;
                }
            }
            double bet = readNumber(in,
                    board.isEmpty() ? "Current bet in BB (1 = unopened): "
                            : "Bet to call in BB (0 = none): ",
                    0, Double.MAX_VALUE);
            if (Double.isNaN(bet)) {
                break;
            }

            try {
                Advice a = solver.advise(position, board, (int) opp, pot, bet);
                System.out.println();
                System.out.println("Hand:     " + solver + " (" + a.handCode()
                        + ")");
                System.out.println("Street:   " + a.street());
                if (a.madeHand() != null) {
                    System.out.println("Made:     " + a.madeHand());
                }
                System.out.printf("Equity:   %.1f%% vs %d random hand(s)%n",
                        a.equity().equity() * 100, (int) opp);
                System.out.println(">> " + a.action().toString().toUpperCase()
                        + " - " + a.reason());
            } catch (IllegalArgumentException e) {
                System.out.println("  " + e.getMessage());
            }
        }
        in.close();
    }
}
