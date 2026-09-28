# PokerSolver

A fast poker hand evaluator, equity calculator and decision helper for
No-Limit Texas Hold'em, with a desktop UI and a console mode.

## Features

- **Graphical UI** (`PokerSolverUI`): click cards to deal your hand and the
  board, pick your seat, opponents, pot and bet, and get an instant
  Raise / Call / Check / Bet / Fold recommendation. A 13x13 range chart shows
  the full preflop strategy for your seat with your hand highlighted.
- **Hand evaluation** (`HandEvaluator`): ranks any 5-7 card hand, from high
  card to straight flush (including the A-2-3-4-5 wheel).
- **Equity calculator** (`EquityCalculator`): Monte Carlo simulation (50,000
  deals by default) of your hand versus 1-8 random opponents, preflop or on
  any flop, turn or river.
- **Preflop charts** (`PreflopCharts`): simplified 9-max, 100bb opening,
  3-bet, flat-call and defend ranges for all nine seats, plus responses to
  3-bets and all-ins. Ranges are written in standard shorthand
  (`"55+, A3s+, KTs+, AJo+"`), so they are easy to tweak.
- **Postflop advice**: compares your equity with the pot odds - call when
  equity beats the price, raise / bet when you are well ahead, otherwise
  check or fold.

> Postflop equity is measured against *random* hands. Real opponents who bet
> usually hold stronger ranges, so treat close calls with care.

## Running

Requires Java 17+ (records are used). From the repository root:

```bash
javac -d bin src/*.java
java -cp bin PokerSolverUI              # desktop UI
java -cp bin PokerSolverImplementation  # console version
```

In VS Code you can also open `src/PokerSolverUI.java` and press **Run**.

### Using the UI

1. Click two cards in the deck for your hand; further clicks deal the flop,
   turn and river. Click any dealt card to take it back.
2. Choose your **Position** and the number of **Opponents in hand**.
3. Preflop, set **Current bet (BB)** to the largest bet in front of you:
   `1` = unopened, `2-4.5` = facing an open raise, `4.5-15` = facing a
   3-bet, more = facing an all-in.
4. Postflop, set the **Pot** (including the bet you face) and the **Bet to
   call** (`0` if checked to you).

The recommendation updates automatically after every change.

## Project layout

| File | Role |
| --- | --- |
| `PokerSolverKernel` | Kernel interface: the hero's hand (add, remove, get, size, clear) |
| `PokerSolver` | Enhanced interface: `handCode`, `recommendation`, `advise` |
| `PokerSolverSecondary` | Secondary methods written using only kernel methods |
| `PokerSolverOnList` | Kernel implementation on a `List<Card>` |
| `Card`, `Position`, `Action`, `Advice` | Value types |
| `HandEvaluator`, `EquityCalculator`, `PreflopCharts` | Poker engine |
| `PokerSolverUI` | Swing desktop UI |
| `PokerSolverImplementation` | Console front end |

## Tests

JUnit 4 tests live in `test/`. In VS Code they run from the Testing panel.
From a terminal (with `junit-4.13.2.jar` and `hamcrest-core-1.3.jar` in
`lib/`):

```bash
javac -d bin -cp "lib/*" src/*.java test/*.java
java -ea -cp "bin:lib/*" org.junit.runner.JUnitCore \
    CardTest HandEvaluatorTest PreflopChartsTest EquityCalculatorTest PokerSolverTest
```
