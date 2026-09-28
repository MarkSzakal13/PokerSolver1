import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JSpinner;
import javax.swing.JTextArea;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.UIManager;
import javax.swing.border.TitledBorder;

/**
 * Swing user interface for the poker solver.
 *
 * <p>
 * Click cards in the deck to deal them into the hero's hand, then onto the
 * board. Click a dealt card to take it back. Advice is recalculated
 * automatically whenever anything changes.
 */
public final class PokerSolverUI extends JFrame {

    /** Serialization id. */
    private static final long serialVersionUID = 1L;

    // ---------------------------------------------------------------- colors

    /** Felt background. */
    private static final Color FELT = new Color(0x0F5132);
    /** Darker felt for panels. */
    private static final Color FELT_DARK = new Color(0x0A3622);
    /** Light text. */
    private static final Color TEXT = new Color(0xF1F5F2);
    /** Muted text. */
    private static final Color MUTED = new Color(0xA7C4B5);
    /** Accent / highlight. */
    private static final Color GOLD = new Color(0xF5C542);
    /** Red suits. */
    private static final Color RED_SUIT = new Color(0xC62828);
    /** Black suits. */
    private static final Color BLACK_SUIT = new Color(0x1B1B1B);
    /** Raise color. */
    private static final Color RAISE_C = new Color(0xD9534F);
    /** Call color. */
    private static final Color CALL_C = new Color(0x2E9E5B);
    /** Check color. */
    private static final Color CHECK_C = new Color(0x3C78B5);
    /** Fold color. */
    private static final Color FOLD_C = new Color(0x4A4F55);

    // ----------------------------------------------------------------- sizes

    /** Number of board cards. */
    private static final int BOARD_SIZE = 5;
    /** Slot card width. */
    private static final int SLOT_W = 64;
    /** Slot card height. */
    private static final int SLOT_H = 90;
    /** Deck card width. */
    private static final int DECK_W = 42;
    /** Deck card height. */
    private static final int DECK_H = 58;
    /** Preferred range grid cell size. */
    private static final int CELL = 28;
    /** Smallest range grid cell size. */
    private static final int MIN_CELL = 16;
    /** Minimum window width. */
    private static final int MIN_W = 1000;
    /** Minimum window height. */
    private static final int MIN_H = 600;
    /** Grid dimension. */
    private static final int GRID = 13;
    /** Corner radius. */
    private static final int ARC = 10;
    /** Standard padding. */
    private static final int PAD = 8;
    /** Big font size. */
    private static final float BIG_FONT = 34f;
    /** Percent multiplier. */
    private static final int PERCENT = 100;
    /** Max pot / bet spinner value. */
    private static final double MAX_BB = 10_000;

    // ------------------------------------------------------------------ state

    /** Hero hole cards (null = empty slot). */
    private final Card[] hero = new Card[PokerSolverKernel.MAX_CARDS];
    /** Board cards (null = empty slot). */
    private final Card[] board = new Card[BOARD_SIZE];
    /** Deck buttons indexed by card index. */
    private final DeckCard[] deckCards = new DeckCard[Card.DECK_SIZE];
    /** Slot components for the hand. */
    private final Slot[] heroSlots = new Slot[PokerSolverKernel.MAX_CARDS];
    /** Slot components for the board. */
    private final Slot[] boardSlots = new Slot[BOARD_SIZE];
    /** Current in-flight calculation. */
    private transient SwingWorker<Advice, Void> worker;
    /** Whether the previous update was preflop. */
    private boolean wasPreflop = true;

    // --------------------------------------------------------------- widgets

    /** Position selector. */
    private final JComboBox<Position> positionBox = new JComboBox<>(
            Position.values());
    /** Opponent count. */
    private final JSpinner opponents = new JSpinner(
            new SpinnerNumberModel(1, 1, EquityCalculator.MAX_OPPONENTS, 1));
    /** Pot size. */
    private final JSpinner pot = new JSpinner(
            new SpinnerNumberModel(6.0, 0.0, MAX_BB, 0.5));
    /** Bet to call. */
    private final JSpinner bet = new JSpinner(
            new SpinnerNumberModel(1.0, 0.0, MAX_BB, 0.5));
    /** Label for the bet spinner. */
    private final JLabel betLabel = label("Current bet (BB)");
    /** Label for the pot spinner. */
    private final JLabel potLabel = label("Pot (BB, incl. bet)");
    /** Situation hint under the bet spinner. */
    private final JLabel situationLabel = label(" ");
    /** Big action label. */
    private final JLabel actionLabel = new JLabel("Pick two cards",
            SwingConstants.CENTER);
    /** Detail lines. */
    private final JLabel handLabel = label(" ");
    /** Detail lines. */
    private final JLabel madeLabel = label(" ");
    /** Equity bar. */
    private final JProgressBar equityBar = new JProgressBar(0, PERCENT * 10);
    /** Win/tie line. */
    private final JLabel winLabel = label(" ");
    /** Pot odds line. */
    private final JLabel oddsLabel = label(" ");
    /** Reason text. */
    private final JTextArea reasonText = new JTextArea(3, 24);
    /** Range grid. */
    private final RangeGrid rangeGrid = new RangeGrid();
    /** Range grid title. */
    private final TitledBorder rangeBorder = titled("Preflop range");

    /**
     * Builds the window.
     */
    public PokerSolverUI() {
        super("Poker Solver");
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel root = new JPanel(new BorderLayout(PAD, PAD));
        root.setBackground(FELT);
        root.setBorder(BorderFactory.createEmptyBorder(PAD, PAD, PAD, PAD));
        root.add(this.buildControls(), BorderLayout.WEST);
        root.add(this.buildTable(), BorderLayout.CENTER);
        root.add(this.buildResults(), BorderLayout.EAST);
        this.setContentPane(root);

        this.positionBox.addActionListener(e -> this.update());
        this.opponents.addChangeListener(e -> this.update());
        this.pot.addChangeListener(e -> this.update());
        this.bet.addChangeListener(e -> this.update());
        for (JSpinner sp : new JSpinner[] { this.opponents, this.pot,
            this.bet }) {
            // Apply typed numbers immediately, not only on Enter.
            ((javax.swing.text.DefaultFormatter) ((JSpinner.DefaultEditor) sp
                    .getEditor()).getTextField().getFormatter())
                            .setCommitsOnValidEdit(true);
        }

        this.update();
        this.pack();
        java.awt.Rectangle screen = java.awt.GraphicsEnvironment
                .getLocalGraphicsEnvironment().getMaximumWindowBounds();
        this.setSize(Math.min(this.getWidth(), screen.width),
                Math.min(this.getHeight(), screen.height));
        this.setMinimumSize(new Dimension(MIN_W, MIN_H));
        this.setLocationRelativeTo(null);
    }

    // ================================================================ layout

    /**
     * Left column: position, opponents, pot, bet, buttons.
     *
     * @return the panel
     */
    private JComponent buildControls() {
        JPanel p = new JPanel(new GridBagLayout());
        p.setBackground(FELT_DARK);
        p.setBorder(titled("Situation"));
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(2, PAD, 2, PAD);
        c.anchor = GridBagConstraints.NORTHWEST;

        p.add(label("Position"), c);
        p.add(this.positionBox, c);
        p.add(Box.createVerticalStrut(PAD), c);
        p.add(label("Opponents in hand"), c);
        p.add(this.opponents, c);
        p.add(Box.createVerticalStrut(PAD), c);
        p.add(this.potLabel, c);
        p.add(this.pot, c);
        p.add(Box.createVerticalStrut(PAD), c);
        p.add(this.betLabel, c);
        p.add(this.bet, c);
        this.situationLabel.setForeground(GOLD);
        p.add(this.situationLabel, c);
        p.add(Box.createVerticalStrut(PAD * 2), c);

        JButton random = button("Random hand");
        random.addActionListener(e -> this.dealRandomHand());
        p.add(random, c);
        JButton clearBoard = button("Clear board");
        clearBoard.addActionListener(e -> {
            for (int i = 0; i < BOARD_SIZE; i++) {
                this.board[i] = null;
            }
            this.update();
        });
        p.add(clearBoard, c);
        JButton clearAll = button("Clear all");
        clearAll.addActionListener(e -> {
            for (int i = 0; i < this.hero.length; i++) {
                this.hero[i] = null;
            }
            for (int i = 0; i < BOARD_SIZE; i++) {
                this.board[i] = null;
            }
            this.update();
        });
        p.add(clearAll, c);

        c.weighty = 1;
        p.add(Box.createVerticalGlue(), c);
        JLabel help = label("<html><div style='width:150px'>"
                + "Click deck cards to deal your hand, then the board. "
                + "Click a dealt card to remove it.<br><br>"
                + "Bets and pot are in big blinds.</div></html>");
        help.setForeground(MUTED);
        p.add(help, c);
        return p;
    }

    /**
     * Center: dealt cards and the deck.
     *
     * @return the panel
     */
    private JComponent buildTable() {
        JPanel center = new JPanel(new BorderLayout(PAD, PAD));
        center.setOpaque(false);

        JPanel dealt = new JPanel(new FlowLayout(FlowLayout.CENTER, PAD * 2,
                PAD));
        dealt.setOpaque(false);

        JPanel handPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 4, 4));
        handPanel.setBackground(FELT_DARK);
        handPanel.setBorder(titled("Your hand"));
        for (int i = 0; i < this.heroSlots.length; i++) {
            final int idx = i;
            this.heroSlots[i] = new Slot(() -> this.hero[idx], () -> {
                this.hero[idx] = null;
                this.update();
            });
            handPanel.add(this.heroSlots[i]);
        }
        JPanel boardPanel = new JPanel(
                new FlowLayout(FlowLayout.CENTER, 4, 4));
        boardPanel.setBackground(FELT_DARK);
        boardPanel.setBorder(titled("Board"));
        for (int i = 0; i < BOARD_SIZE; i++) {
            final int idx = i;
            this.boardSlots[i] = new Slot(() -> this.board[idx], () -> {
                this.board[idx] = null;
                this.update();
            });
            boardPanel.add(this.boardSlots[i]);
        }
        dealt.add(handPanel);
        dealt.add(boardPanel);
        JPanel top = new JPanel();
        top.setLayout(new BoxLayout(top, BoxLayout.Y_AXIS));
        top.setOpaque(false);
        top.add(dealt);

        JPanel deck = new JPanel(new GridLayout(Card.Suit.values().length,
                GRID, 3, 3));
        deck.setBackground(FELT_DARK);
        deck.setBorder(titled("Deck"));
        Card.Suit[] suits = { Card.Suit.SPADES, Card.Suit.HEARTS,
            Card.Suit.DIAMONDS, Card.Suit.CLUBS };
        Card.Rank[] ranks = Card.Rank.values();
        for (Card.Suit s : suits) {
            for (int r = ranks.length - 1; r >= 0; r--) {
                Card card = new Card(ranks[r], s);
                DeckCard dc = new DeckCard(card);
                this.deckCards[card.index()] = dc;
                deck.add(dc);
            }
        }
        JPanel deckWrap = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        deckWrap.setOpaque(false);
        deckWrap.add(deck);
        top.add(deckWrap);
        center.add(top, BorderLayout.NORTH);

        JPanel gridWrap = new JPanel(new BorderLayout(0, 4));
        gridWrap.setBackground(FELT_DARK);
        gridWrap.setBorder(this.rangeBorder);
        gridWrap.add(this.rangeGrid, BorderLayout.CENTER);
        gridWrap.add(legend(), BorderLayout.SOUTH);
        center.add(gridWrap, BorderLayout.CENTER);
        return center;
    }

    /**
     * Right column: the recommendation and supporting numbers.
     *
     * @return the panel
     */
    private JComponent buildResults() {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBackground(FELT_DARK);
        p.setBorder(titled("Recommendation"));
        p.setPreferredSize(new Dimension(280, 0));

        this.actionLabel.setOpaque(true);
        this.actionLabel.setForeground(Color.WHITE);
        this.actionLabel.setBackground(FOLD_C);
        this.actionLabel.setFont(this.actionLabel.getFont()
                .deriveFont(Font.BOLD, BIG_FONT));
        this.actionLabel.setAlignmentX(LEFT_ALIGNMENT);
        this.actionLabel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 80));
        this.actionLabel.setPreferredSize(new Dimension(240, 80));
        p.add(this.actionLabel);
        p.add(Box.createVerticalStrut(PAD * 2));

        for (JLabel l : new JLabel[] { this.handLabel, this.madeLabel }) {
            l.setAlignmentX(LEFT_ALIGNMENT);
            l.setFont(l.getFont().deriveFont(Font.BOLD, 15f));
            p.add(l);
            p.add(Box.createVerticalStrut(4));
        }
        p.add(Box.createVerticalStrut(PAD));

        JLabel eqTitle = label("Equity vs random hands");
        eqTitle.setAlignmentX(LEFT_ALIGNMENT);
        p.add(eqTitle);
        this.equityBar.setStringPainted(true);
        this.equityBar.setString("-");
        this.equityBar.setForeground(GOLD);
        this.equityBar.setAlignmentX(LEFT_ALIGNMENT);
        this.equityBar.setMaximumSize(new Dimension(Integer.MAX_VALUE, 24));
        p.add(this.equityBar);
        p.add(Box.createVerticalStrut(4));
        this.winLabel.setAlignmentX(LEFT_ALIGNMENT);
        p.add(this.winLabel);
        this.oddsLabel.setAlignmentX(LEFT_ALIGNMENT);
        p.add(this.oddsLabel);
        p.add(Box.createVerticalStrut(PAD * 2));

        this.reasonText.setEditable(false);
        this.reasonText.setLineWrap(true);
        this.reasonText.setWrapStyleWord(true);
        this.reasonText.setOpaque(false);
        this.reasonText.setForeground(TEXT);
        this.reasonText.setFont(this.handLabel.getFont()
                .deriveFont(Font.PLAIN, 13f));
        this.reasonText.setAlignmentX(LEFT_ALIGNMENT);
        this.reasonText.setBorder(null);
        p.add(this.reasonText);
        p.add(Box.createVerticalGlue());

        JLabel note = label("<html><div style='width:200px'>Preflop advice"
                + " uses simplified 9-max, 100bb charts. Postflop advice"
                + " compares Monte Carlo equity against random hands with"
                + " the pot odds - real opponents' ranges are stronger,"
                + " so treat marginal calls with care.</div></html>");
        note.setForeground(MUTED);
        note.setAlignmentX(LEFT_ALIGNMENT);
        p.add(note);
        return p;
    }

    /**
     * Legend for the range grid.
     *
     * @return the legend panel
     */
    private static JComponent legend() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.CENTER, PAD, 0));
        p.setOpaque(false);
        Object[][] items = { { "Raise", RAISE_C }, { "Call", CALL_C },
            { "Check", CHECK_C }, { "Fold", FOLD_C }, { "Your hand", GOLD } };
        for (Object[] it : items) {
            JLabel l = label("■ " + it[0]);
            l.setForeground((Color) it[1]);
            p.add(l);
        }
        return p;
    }

    // ================================================================= logic

    /**
     * Deals a card into the first empty hand slot, else the first empty
     * board slot.
     *
     * @param card
     *            the card
     */
    private void deal(Card card) {
        for (int i = 0; i < this.hero.length; i++) {
            if (this.hero[i] == null) {
                this.hero[i] = card;
                this.update();
                return;
            }
        }
        for (int i = 0; i < BOARD_SIZE; i++) {
            if (this.board[i] == null) {
                this.board[i] = card;
                this.update();
                return;
            }
        }
        java.awt.Toolkit.getDefaultToolkit().beep();
    }

    /**
     * Replaces the hand with two random cards not on the board.
     */
    private void dealRandomHand() {
        List<Card> avail = new ArrayList<>();
        for (int i = 0; i < Card.DECK_SIZE; i++) {
            Card c = Card.fromIndex(i);
            if (!this.onBoard(c)) {
                avail.add(c);
            }
        }
        Collections.shuffle(avail);
        this.hero[0] = avail.get(0);
        this.hero[1] = avail.get(1);
        this.update();
    }

    /**
     * @param c
     *            a card
     * @return whether the card is on the board
     */
    private boolean onBoard(Card c) {
        for (Card b : this.board) {
            if (c.equals(b)) {
                return true;
            }
        }
        return false;
    }

    /**
     * @param c
     *            a card
     * @return whether the card has been dealt anywhere
     */
    private boolean isDealt(Card c) {
        return c.equals(this.hero[0]) || c.equals(this.hero[1])
                || this.onBoard(c);
    }

    /**
     * @return the board cards, compacted
     */
    private List<Card> boardList() {
        List<Card> out = new ArrayList<>();
        for (Card c : this.board) {
            if (c != null) {
                out.add(c);
            }
        }
        return out;
    }

    /**
     * Refreshes every widget and starts a new calculation if possible.
     */
    private void update() {
        for (DeckCard dc : this.deckCards) {
            dc.setDealt(this.isDealt(dc.card));
        }
        for (Slot s : this.heroSlots) {
            s.repaint();
        }
        for (Slot s : this.boardSlots) {
            s.repaint();
        }

        List<Card> b = this.boardList();
        boolean preflop = b.isEmpty();
        if (preflop != this.wasPreflop) {
            // Sensible default when switching streets.
            this.bet.setValue(preflop ? 1.0 : 0.0);
            this.wasPreflop = preflop;
        }
        this.pot.setEnabled(!preflop);
        this.potLabel.setEnabled(!preflop);
        double betBB = ((Number) this.bet.getValue()).doubleValue();
        Position position = (Position) this.positionBox.getSelectedItem();
        PreflopCharts.Situation situation = PreflopCharts.situation(betBB);
        if (preflop) {
            this.betLabel.setText("Current bet (BB)");
            this.situationLabel.setText(situation.toString());
        } else {
            this.betLabel.setText("Bet to call (BB)");
            this.situationLabel.setText(" ");
        }

        String code = null;
        if (this.hero[0] != null && this.hero[1] != null) {
            code = PreflopCharts.handCode(this.hero[0], this.hero[1]);
        }
        this.rangeGrid.show(position, situation, code);
        this.rangeBorder.setTitle(
                "Preflop range: " + position + " - " + situation);
        this.rangeGrid.getParent().getParent().repaint();

        if (this.worker != null) {
            this.worker.cancel(true);
            this.worker = null;
        }
        if (code == null) {
            this.showMessage("Pick two cards",
                    "Click two cards in the deck to set your hand.");
            return;
        }
        if (b.size() == 1 || b.size() == 2) {
            this.showMessage("Board?",
                    "Deal 3 (flop), 4 (turn) or 5 (river) board cards.");
            return;
        }

        PokerSolver solver = new PokerSolverOnList(this.hero[0].toString(),
                this.hero[1].toString());
        int opp = (Integer) this.opponents.getValue();
        double potBB = ((Number) this.pot.getValue()).doubleValue();
        this.actionLabel.setText("…");
        this.worker = new SwingWorker<>() {
            @Override
            protected Advice doInBackground() {
                return solver.advise(position, b, opp, potBB, betBB);
            }

            @Override
            protected void done() {
                if (this.isCancelled()) {
                    return;
                }
                try {
                    PokerSolverUI.this.showAdvice(this.get());
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } catch (ExecutionException e) {
                    PokerSolverUI.this.showMessage("Error",
                            e.getCause().getMessage());
                }
            }
        };
        this.worker.execute();
    }

    /**
     * Shows a placeholder message instead of advice.
     *
     * @param title
     *            big text
     * @param detail
     *            explanation
     */
    private void showMessage(String title, String detail) {
        this.actionLabel.setText(title);
        this.actionLabel.setBackground(FOLD_C);
        this.handLabel.setText(" ");
        this.madeLabel.setText(" ");
        this.equityBar.setValue(0);
        this.equityBar.setString("-");
        this.winLabel.setText(" ");
        this.oddsLabel.setText(" ");
        this.reasonText.setText(detail);
    }

    /**
     * Displays advice.
     *
     * @param a
     *            the advice
     */
    private void showAdvice(Advice a) {
        this.actionLabel.setText(a.action().toString().toUpperCase());
        this.actionLabel.setBackground(colorFor(a.action()));
        this.handLabel.setText(a.street() + "  ·  " + a.handCode());
        this.madeLabel.setText(a.madeHand() == null ? " "
                : "Made hand: " + a.madeHand());
        double eq = a.equity().equity();
        this.equityBar.setValue((int) Math.round(eq * PERCENT * 10));
        this.equityBar.setString(String.format("%.1f%%", eq * PERCENT));
        this.winLabel.setText(String.format("Win %.1f%%  ·  Tie %.1f%%"
                + "  ·  %,d sims", a.equity().win() * PERCENT,
                a.equity().tie() * PERCENT, a.equity().trials()));
        this.oddsLabel.setText(a.requiredEquity() > 0
                ? String.format("Pot odds need %.1f%% equity",
                        a.requiredEquity() * PERCENT)
                : " ");
        this.reasonText.setText(a.reason());
    }

    // =============================================================== helpers

    /**
     * @param a
     *            an action
     * @return its display color
     */
    private static Color colorFor(Action a) {
        switch (a) {
            case RAISE:
            case BET:
                return RAISE_C;
            case CALL:
                return CALL_C;
            case CHECK:
                return CHECK_C;
            default:
                return FOLD_C;
        }
    }

    /**
     * @param text
     *            label text
     * @return a light label
     */
    private static JLabel label(String text) {
        JLabel l = new JLabel(text);
        l.setForeground(TEXT);
        return l;
    }

    /**
     * @param text
     *            button text
     * @return a button
     */
    private static JButton button(String text) {
        JButton b = new JButton(text);
        b.setFocusPainted(false);
        return b;
    }

    /**
     * @param title
     *            border title
     * @return a titled border in theme colors
     */
    private static TitledBorder titled(String title) {
        TitledBorder t = BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(FELT.brighter(), 1, true),
                title);
        t.setTitleColor(GOLD);
        return t;
    }

    /**
     * Paints a face-up card.
     *
     * @param g
     *            graphics
     * @param c
     *            the card
     * @param w
     *            width
     * @param h
     *            height
     * @param dim
     *            whether to draw it faded
     */
    private static void paintCard(Graphics2D g, Card c, int w, int h,
            boolean dim) {
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g.setColor(dim ? new Color(0x55, 0x66, 0x5C) : Color.WHITE);
        g.fillRoundRect(0, 0, w - 1, h - 1, ARC, ARC);
        g.setColor(dim ? new Color(0x44, 0x55, 0x4B) : new Color(0xBBBBBB));
        g.drawRoundRect(0, 0, w - 1, h - 1, ARC, ARC);
        Color ink = c.suit().isRed() ? RED_SUIT : BLACK_SUIT;
        if (dim) {
            ink = new Color(0x7A8A80);
        }
        g.setColor(ink);
        String rank = c.rank() == Card.Rank.TEN ? "10"
                : String.valueOf(c.rank().getSymbol());
        String suit = String.valueOf(c.suit().getSymbol());
        Font f = new Font(Font.SANS_SERIF, Font.BOLD, h * 2 / 5);
        g.setFont(f);
        FontMetrics fm = g.getFontMetrics();
        g.drawString(rank, (w - fm.stringWidth(rank)) / 2,
                h / 2 - fm.getDescent() + 2);
        Font sf = new Font(Font.SANS_SERIF, Font.PLAIN, h * 2 / 5);
        g.setFont(sf);
        fm = g.getFontMetrics();
        g.drawString(suit, (w - fm.stringWidth(suit)) / 2,
                h - fm.getDescent() - 2);
    }

    // ============================================================ components

    /**
     * Supplies the card currently in a slot.
     */
    private interface CardSource {
        /**
         * @return the card or null
         */
        Card get();
    }

    /**
     * A dealt-card slot; click to remove the card.
     */
    private static final class Slot extends JComponent {
        /** Serialization id. */
        private static final long serialVersionUID = 1L;
        /** Card supplier. */
        private final transient CardSource source;

        /**
         * @param source
         *            where to read the card from
         * @param onRemove
         *            called when clicked while holding a card
         */
        Slot(CardSource source, Runnable onRemove) {
            this.source = source;
            this.setPreferredSize(new Dimension(SLOT_W, SLOT_H));
            this.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            this.setToolTipText("Click to remove");
            this.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    if (source.get() != null) {
                        onRemove.run();
                    }
                }
            });
        }

        @Override
        protected void paintComponent(Graphics g0) {
            Graphics2D g = (Graphics2D) g0.create();
            Card c = this.source.get();
            if (c == null) {
                g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);
                g.setColor(FELT);
                g.fillRoundRect(0, 0, SLOT_W - 1, SLOT_H - 1, ARC, ARC);
                g.setColor(MUTED);
                g.setStroke(new BasicStroke(1.5f, BasicStroke.CAP_ROUND,
                        BasicStroke.JOIN_ROUND, 1f, new float[] { 4f, 4f },
                        0f));
                g.drawRoundRect(1, 1, SLOT_W - 3, SLOT_H - 3, ARC, ARC);
            } else {
                paintCard(g, c, SLOT_W, SLOT_H, false);
            }
            g.dispose();
        }
    }

    /**
     * A card in the deck picker.
     */
    private final class DeckCard extends JComponent {
        /** Serialization id. */
        private static final long serialVersionUID = 1L;
        /** The card. */
        private final Card card;
        /** Whether it has been dealt. */
        private boolean dealt;

        /**
         * @param card
         *            the card
         */
        DeckCard(Card card) {
            this.card = card;
            this.setPreferredSize(new Dimension(DECK_W, DECK_H));
            this.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            this.setToolTipText(card.toString());
            this.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    if (!DeckCard.this.dealt) {
                        PokerSolverUI.this.deal(card);
                    }
                }
            });
        }

        /**
         * @param d
         *            whether the card has been dealt
         */
        void setDealt(boolean d) {
            this.dealt = d;
            this.setCursor(Cursor.getPredefinedCursor(
                    d ? Cursor.DEFAULT_CURSOR : Cursor.HAND_CURSOR));
            this.repaint();
        }

        @Override
        protected void paintComponent(Graphics g0) {
            Graphics2D g = (Graphics2D) g0.create();
            paintCard(g, this.card, DECK_W, DECK_H, this.dealt);
            g.dispose();
        }
    }

    /**
     * 13x13 starting-hand chart for the selected position and situation.
     */
    private static final class RangeGrid extends JComponent {
        /** Serialization id. */
        private static final long serialVersionUID = 1L;
        /** Position shown. */
        private Position position = Position.UTG;
        /** Situation shown. */
        private PreflopCharts.Situation situation =
                PreflopCharts.Situation.UNOPENED;
        /** Hero hand code, or null. */
        private String heroCode;

        /**
         * Constructor.
         */
        RangeGrid() {
            this.setPreferredSize(new Dimension(CELL * GRID + 1,
                    CELL * GRID + 1));
            this.setMinimumSize(new Dimension(MIN_CELL * GRID + 1,
                    MIN_CELL * GRID + 1));
            this.setToolTipText("");
        }

        /**
         * @param p
         *            position
         * @param s
         *            situation
         * @param code
         *            hero hand code or null
         */
        void show(Position p, PreflopCharts.Situation s, String code) {
            this.position = p;
            this.situation = s;
            this.heroCode = code;
            this.repaint();
        }

        /**
         * @return current cell size, fitted to the component
         */
        private int cell() {
            return Math.max(1, (Math.min(this.getWidth(), this.getHeight())
                    - 1) / GRID);
        }

        /**
         * @return left edge that centers the grid horizontally
         */
        private int left() {
            return (this.getWidth() - this.cell() * GRID) / 2;
        }

        @Override
        public String getToolTipText(MouseEvent e) {
            int col = Math.floorDiv(e.getX() - this.left(), this.cell());
            int row = e.getY() / this.cell();
            if (row < 0 || col < 0 || row >= GRID || col >= GRID) {
                return null;
            }
            String code = PreflopCharts.gridCode(row, col);
            return code + ": " + PreflopCharts.action(this.position,
                    this.situation, code);
        }

        @Override
        protected void paintComponent(Graphics g0) {
            Graphics2D g = (Graphics2D) g0.create();
            g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                    RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            final int cell = this.cell();
            final int x0 = this.left();
            // Largest font (up to 3/8 of a cell) where every label fits.
            int size = Math.max(6, cell * 3 / 8);
            g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, size));
            while (size > 6 && g.getFontMetrics().stringWidth("QQo") > cell
                    - 2) {
                size--;
                g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, size));
            }
            FontMetrics fm = g.getFontMetrics();
            for (int row = 0; row < GRID; row++) {
                for (int col = 0; col < GRID; col++) {
                    String code = PreflopCharts.gridCode(row, col);
                    Action a = PreflopCharts.action(this.position,
                            this.situation, code);
                    int x = x0 + col * cell;
                    int y = row * cell;
                    g.setColor(colorFor(a));
                    g.fillRect(x, y, cell, cell);
                    g.setColor(FELT_DARK);
                    g.drawRect(x, y, cell, cell);
                    g.setColor(a == Action.FOLD ? MUTED : Color.WHITE);
                    g.drawString(code, x + (cell - fm.stringWidth(code)) / 2,
                            y + (cell + fm.getAscent()) / 2 - 2);
                    if (code.equals(this.heroCode)) {
                        g.setColor(GOLD);
                        g.setStroke(new BasicStroke(3f));
                        g.drawRect(x + 1, y + 1, cell - 2, cell - 2);
                        g.setStroke(new BasicStroke(1f));
                    }
                }
            }
            g.dispose();
        }
    }

    /**
     * Launches the UI.
     *
     * @param args
     *            ignored
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(
                        UIManager.getCrossPlatformLookAndFeelClassName());
            } catch (ReflectiveOperationException
                    | javax.swing.UnsupportedLookAndFeelException e) {
                // keep the default look and feel
            }
            new PokerSolverUI().setVisible(true);
        });
    }
}
