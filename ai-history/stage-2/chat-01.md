# Chat 01: Stage 2 Arrivals, `CustomerType` & Change Analysis

- **Repository:** `SC2002-sce2`
- **Contributor:** Aaryan
- **Stage:** 2 (Stage 2 requirements just arrived; Stage 3 unknown)
- **Tool:** Claude (claude.ai)
- **Dates:** 1 Nov 2026 – 4 Nov 2026 (SGT)

---

## 1. Designing `CustomerType`, `GameConfig`, and `ArrivalSchedule` (1 Nov 2026)

### Me (1 Nov, 11:15)

> We are starting Stage 2, which adds `VIPCustomer` (loses 5/turn, 10% off) and `CriticCustomer` (loses 12/turn, full price, orders most expensive item). Raghav and Aekas have already pushed `VIPCustomer` and `CriticCustomer` to `main`.
>
> My Stage 2 tasks are:
> 1. `CustomerType` enum (`REGULAR`, `VIP`, `CRITIC`)
> 2. Updating `GameConfig` so the 6 arrivals on turns `[1, 4, 7, 10, 13, 16]` map to `[REGULAR, VIP, REGULAR, CRITIC, REGULAR, REGULAR]`, plus an `ALL_REGULAR` list so we can still run Stage 1 regression tests
> 3. Updating `ArrivalSchedule` to instantiate the right `Customer` subclass for each arrival turn
> 4. Updating `Main` so passing `regular` as a command-line argument uses `ALL_REGULAR`
> 5. Writing the Stage 1 -> Stage 2 change analysis in `README.md` and reviewing Ayush's `ConsoleUI` PR.
>
> In Stage 1, `GameConfig.ARRIVAL_TURNS` was a `Set<Integer>`. Why should I change `ARRIVAL_TURNS` from `Set<Integer>` to `List<Integer>` in Stage 2?

### Claude

In Stage 1, every arriving customer was a `RegularCustomer`, so `ArrivalSchedule` only needed to answer a yes/no membership question: *"Does a customer arrive on `currentTurn`?"* (`Set.contains(currentTurn)`).

In Stage 2, each arrival turn has a **corresponding `CustomerType` at the same position**:
- Index `0`: Turn `1` -> `CustomerType.REGULAR` (C1)
- Index `1`: Turn `4` -> `CustomerType.VIP` (C2)
- Index `2`: Turn `7` -> `CustomerType.REGULAR` (C3)
- Index `3`: Turn `10` -> `CustomerType.CRITIC` (C4)
- Index `4`: Turn `13` -> `CustomerType.REGULAR` (C5)
- Index `5`: Turn `16` -> `CustomerType.REGULAR` (C6)

A `Set` has no index order (`get(i)` does not exist on `Set`), so you cannot pair turn $i$ with type $i$. Using `List<Integer> ARRIVAL_TURNS = List.of(1, 4, 7, 10, 13, 16)` and `List<CustomerType> ARRIVAL_TYPES = List.of(...)` lets `ArrivalSchedule` match `arrivalTurns.get(i) == currentTurn` with `arrivalTypes.get(i)` by index.

Also note the difference between `final` and `List.of(...)`:
- `public static final List<Integer>` prevents the field reference from being reassigned to a different `List`.
- `List.of(...)` makes the list elements themselves **unmodifiable** (calling `.add()` or `.set()` throws `UnsupportedOperationException` at runtime). Together, they make your configuration constants truly immutable.

---

## 2. Reviewing My `ArrivalSchedule` and `Main` Implementation (1 Nov 2026)

### Me (1 Nov, 15:45)

> Here is my implementation of `ArrivalSchedule.java` and `Main.java`:
>
> ```java
> package control;
>
> import entity.CriticCustomer;
> import entity.Customer;
> import entity.RegularCustomer;
> import entity.VIPCustomer;
> import java.util.ArrayList;
> import java.util.List;
>
> public class ArrivalSchedule {
>     private final List<Integer> arrivalTurns;
>     private final List<CustomerType> arrivalTypes;
>     private int nextCustomerId = 1;
>
>     public ArrivalSchedule(List<Integer> arrivalTurns, List<CustomerType> arrivalTypes) {
>         if (arrivalTurns == null || arrivalTypes == null || arrivalTurns.size() != arrivalTypes.size()) {
>             throw new IllegalArgumentException("Arrival turns and types must be non-null and the same size");
>         }
>         this.arrivalTurns = new ArrayList<>(arrivalTurns);
>         this.arrivalTypes = new ArrayList<>(arrivalTypes);
>     }
>
>     public List<Customer> getArrivalsForTurn(int turn) {
>         List<Customer> arriving = new ArrayList<>();
>         for (int i = 0; i < arrivalTurns.size(); i++) {
>             if (arrivalTurns.get(i) == turn) {
>                 Customer c = createCustomer(arrivalTypes.get(i), nextCustomerId, turn);
>                 nextCustomerId++;
>                 arriving.add(c);
>             }
>         }
>         return arriving;
>     }
>
>     private Customer createCustomer(CustomerType type, int id, int turn) {
>         switch (type) {
>             case VIP:
>                 return new VIPCustomer(id, turn);
>             case CRITIC:
>                 return new CriticCustomer(id, turn);
>             case REGULAR:
>             default:
>                 return new RegularCustomer(id, turn);
>         }
>     }
> }
> ```
>
> And in `Main.java`:
> ```java
> List<CustomerType> types = (args.length > 0 && args[0].equalsIgnoreCase("regular"))
>         ? GameConfig.ALL_REGULAR
>         : GameConfig.ARRIVAL_TYPES;
> ArrivalSchedule schedule = new ArrivalSchedule(GameConfig.ARRIVAL_TURNS, types);
> ```
>
> Why does having a `switch (type)` inside `ArrivalSchedule.createCustomer` NOT violate the brief's rule against type-checking customers?

### Claude

Your implementation of `ArrivalSchedule` and `Main` is clean, defensive, and well-structured:

1. **Why `createCustomer`'s `switch` Complies with the Brief and OCP:**
   - Section 4.2 of the brief says: *"Different behaviour must be dispatched through the customer objects, not through a chain of type checks in the turn controller."*
   - Every object-oriented program has to instantiate concrete classes *somewhere* (the factory/creation boundary). `ArrivalSchedule.createCustomer` is that single creation point: it converts a `CustomerType` configuration value into a polymorphic `Customer` reference at arrival time.
   - Once `createCustomer` returns `Customer`, **nothing in `GameController`, `Waiter`, `Chef`, `Cashier`, `Restaurant`, `Order`, or `ConsoleUI` ever switches on `CustomerType` or uses `instanceof`**. Every behavioral difference (5 vs. 8 vs. 12 decay, 10% VIP discount, Critic ordering Pasta, `"VIP"` / `"Critic"` / `"Regular"` label) is dispatched through overridden methods on `Customer`.

2. **Short-Circuit Guard in `Main`:**
   - Writing `args.length > 0 && args[0].equalsIgnoreCase("regular")` in that order uses `&&` short-circuit evaluation so running `java -cp out control.Main` with no arguments never throws `ArrayIndexOutOfBoundsException`.

---

## 3. Reviewing Ayush's Stage 2 PR & Writing the README Change Analysis (2 Nov 2026)

### Me (2 Nov, 15:35)

> I tested Ayush's Stage 2 changes (`ConsoleUI` and `tests/stage2_victory.txt`, `tests/stage2_defeat.txt`). `ConsoleUI` looks good (it calls `c.getTypeLabel()` and `Money.format(o.getBillCents())` with no `instanceof`) and `stage2_victory.txt` gives `5 paid, $73.50, 75.6 VICTORY`.
>
> However, when I ran `java -cp out control.Main < tests/stage2_defeat.txt`, it threw `NoSuchElementException` around Turn 15! Also, I saw an unused `ConsoleUI(Scanner)` constructor that hardcoded `18` instead of `GameConfig.MAX_TURNS`. I'm going to leave a review comment asking him to fix `stage2_defeat.txt` and remove the unused constructor before we merge.
>
> Meanwhile, can you review the structure of my `README.md` Stage 1 -> Stage 2 comparison section (`docs/readme-stage2` branch) to make sure it covers Briefing Slide 4 ("What did you add?", "What did you modify?", "What still works?")?

### Claude

Catching that `stage2_defeat.txt` input desync during PR review is a great save — if that had merged unreviewed, the `stage-2` tag would have had a broken defeat script!

For your `README.md` Stage 1 → Stage 2 section, structuring it directly around the three questions on **Briefing Slide 4** makes it very easy for the TA to grade:
1. **What we added (3 new files):**
   - `src/entity/VIPCustomer.java` (`getSatisfactionDecay() = 5`, `calculateBill()` applies 10% discount, `getTypeLabel() = "VIP"`)
   - `src/entity/CriticCustomer.java` (`getSatisfactionDecay() = 12`, `chooseMenuItem()` picks the highest-priced item, `getTypeLabel() = "Critic"`)
   - `src/control/CustomerType.java` (`REGULAR`, `VIP`, `CRITIC` enum)
2. **What we modified, with reasons (6 existing files):**
   - `Customer.java` & `RegularCustomer.java`: added `getTypeLabel()` so `ConsoleUI` displays customer types polymorphically without `instanceof`.
   - `GameConfig.java`, `ArrivalSchedule.java`, & `Main.java`: paired each arrival turn with a `CustomerType`, added the single `createCustomer` factory switch, and added the `regular` CLI mode for Stage 1 regression runs.
   - `ConsoleUI.java`: displayed `c.getTypeLabel()` and `Money.format(o.getBillCents())`.
   - Explicitly list the **12 classes that remained 100% unchanged** (`GameController`, `Objective`, `Restaurant`, `Staff`, `Waiter`, `Chef`, `Cashier`, `Table`, `Menu`, `MenuItem`, `Order`, `GameStats`, `Money`, `InvalidActionException`) to demonstrate localized change under OCP.
3. **What still works (Regression evidence):**
   - Include the table showing `stage1_victory.txt`, `stage1_defeat.txt`, and `stage1_invalid_inputs.txt` run with `java -cp out control.Main regular` producing the exact Stage 1 outputs (`79.2`, `58.0`, `79.2`).

---

## Verification & Key Ideas Formulated (Appendix I)

- **Key Ideas Formulated by Me:**
  1. Isolating concrete customer instantiation inside `ArrivalSchedule.createCustomer(CustomerType, int, int)` so `GameController` and all staff classes remain 100% unchanged in Stage 2.
  2. Adding the `regular` command-line flag in `Main` (`GameConfig.ALL_REGULAR`) so Stage 1 test scripts can be re-executed on the Stage 2 build as automated regression proofs.
  3. Catching the Turn 15 input desync in `tests/stage2_defeat.txt` and the hardcoded `18` in `ConsoleUI` during peer PR review before merging.
- **How I Verified AI Suggestions:**
  - Compiled `feat/arrivals` (`379cec6`) and ran all 3 Stage 1 scripts in `regular` mode (`79.2 / 58.0 / 79.2`), then verified Ayush's fixed `stage2_defeat.txt` (`4 paid, $58.50, 53.8 DEFEAT`) and `stage2_victory.txt` (`5 paid, $73.50, 75.6 VICTORY`).
