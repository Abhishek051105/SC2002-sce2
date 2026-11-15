# Chat 01: Stage 2 `CriticCustomer` Implementation & SOLID Analysis

- **Repository:** `SC2002-sce2`
- **Contributor:** Aekas
- **Stage:** 2 (Stage 2 requirements just arrived; Stage 3 unknown)
- **Tool:** Claude (claude.ai)
- **Dates:** 1 Nov 2026 – 4 Nov 2026 (SGT)

---

## 1. Implementing `CriticCustomer` on Top of `Customer` (1 Nov 2026)

### Me

> We are starting Stage 2, which introduces `VIPCustomer` (Raghav's task) and `CriticCustomer` (my task). Raghav has already pushed `Customer.getTypeLabel()` and `VIPCustomer.java` to `main`.
>
> For `CriticCustomer`, the brief states that a Critic loses 12 satisfaction per waiting turn, pays full price, and has a distinct order preference or reaction to service that we define and document. Our group agreed that a Critic always orders the most expensive dish on the menu.
>
> Here is my implementation of `CriticCustomer.java`:
>
> ```java
> package entity;
>
> public class CriticCustomer extends Customer {
>     private static final int DECAY_PER_TURN = 12;
>
>     public CriticCustomer(int customerId, int arrivalTurn) {
>         super(customerId, arrivalTurn);
>     }
>
>     @Override
>     public int getSatisfactionDecay() {
>         return DECAY_PER_TURN;
>     }
>
>     @Override
>     public MenuItem chooseMenuItem(Menu menu) {
>         MenuItem mostExpensive = menu.getItem(0);
>         for (int i = 1; i < menu.size(); i++) {
>             MenuItem candidate = menu.getItem(i);
>             if (candidate.getPriceCents() > mostExpensive.getPriceCents()) {
>                 mostExpensive = candidate;
>             }
>         }
>         return mostExpensive;
>     }
>
>     @Override
>     public String getTypeLabel() {
>         return "Critic";
>     }
> }
> ```
>
> Can you review this and also help me check how we should explain the design trade-off in `docs/key-abstractions-and-solid.md` between our "orders the most expensive dish" rule vs. an alternative "bad review penalty on departure" rule?

### Claude

Your `CriticCustomer` implementation is clean and concise:
- It overrides `getSatisfactionDecay()` (`12`), `chooseMenuItem(Menu)` (scanning `menu` for the highest `getPriceCents()`, which selects `Pasta` at `$18.00` / 2 prep units in Stage 2), and `getTypeLabel()` (`"Critic"`), while inheriting `calculateBill(int)` (full price) and all `final` lifecycle methods from `Customer`.

**How to Frame the Design Trade-Off in `docs/key-abstractions-and-solid.md` (Section 5 Rubric):**
- **Chosen Design ("Orders the Most Expensive Dish"):**
  - Encapsulates the Critic's special behaviour entirely inside `CriticCustomer.chooseMenuItem(Menu)`, a hook that `Waiter.takeOrder(Customer, Menu)` already calls in `stage-1`.
  - Requires **zero modifications** to `Waiter`, `GameController`, `Restaurant`, or `GameStats`.
  - Still creates genuine gameplay tension: because `Pasta` requires 2 Chef preparation units instead of 1 AND the Critic loses 12 satisfaction per waiting turn (3 turns of waiting drops satisfaction by 36 points), the player must prioritize the Critic in both seating and kitchen prep.
- **Alternative Considered ("Bad Review Penalty When Leaving Unhappy"):**
  - We considered having an unhappy Critic subtract a penalty from `GameStats` upon abandonment.
  - However, that would require `Restaurant.customerLeft(...)` or `GameStats` to query the customer's penalty impact, widening the change across `Restaurant` and `GameStats`. Choosing the `chooseMenuItem(Menu)` override kept the Stage 2 extension 100% localized to the `Customer` hierarchy (maximizing OCP compliance).

---

## 2. Updating the LSP Section After Team Code Review (4 Nov 2026)

### Me

> During our Stage 2 review today, Abhishek pointed out that `Customer.chooseMenuItem(Menu)` checks `if (menu == null || menu.size() == 0) throw new IllegalArgumentException(...)`, whereas my initial `CriticCustomer.chooseMenuItem(Menu)` called `menu.getItem(0)` directly before checking for null/empty. That guard is now in `CriticCustomer.chooseMenuItem(Menu)`.
>
> Here is the note I wrote to add to the LSP section of `docs/key-abstractions-and-solid.md`:
>
> > `CriticCustomer.chooseMenuItem(Menu)` preserves the parent class's precondition check (`menu == null || menu.size() == 0` throws `IllegalArgumentException`) before scanning for the highest-priced item, ensuring callers can substitute `CriticCustomer` for `Customer` without encountering unexpected `NullPointerException` or `IndexOutOfBoundsException` behaviour.
>
> Does this phrasing accurately explain why preserving the guard matters for LSP?

### Claude

Yes, that phrasing is accurate and connects directly to Liskov Substitution Principle theory:
- Under LSP, a subclass method may not strengthen preconditions or throw unchecked exceptions that violate the superclass specification for the same invalid inputs.
- Explicitly noting that `CriticCustomer.chooseMenuItem(Menu)` preserves the `IllegalArgumentException` guard for `null` or empty menus shows that your group checked behavioral subtyping at the method-contract level, not just method signatures.

---

## 3. Deterministic Tie-Breaking in `CriticCustomer.chooseMenuItem(Menu)` (1 Nov 2026)

### Me

> In `CriticCustomer.chooseMenuItem(Menu menu)`, I used strict `>` when comparing prices:
>
> ```java
> MenuItem mostExpensive = menu.getItem(0);
> for (int i = 1; i < menu.size(); i++) {
>     MenuItem candidate = menu.getItem(i);
>     if (candidate.getPriceCents() > mostExpensive.getPriceCents()) {
>         mostExpensive = candidate;
>     }
> }
> return mostExpensive;
> ```
>
> Why is strict `>` better than `>=` here, and should I mention this in `docs/design-log.md`?

### Claude

Using strict `>` (`candidate.getPriceCents() > mostExpensive.getPriceCents()`) provides a clean **first-highest tie-breaking rule**:
- If two menu items ever have the exact same highest price, `>` keeps the first one encountered in menu order (`index 0, 1, 2, ...`), whereas `>=` would overwrite it with the last one.
- Documenting that `CriticCustomer` selects the highest-priced item on the menu (with ties broken by earliest menu index) in `docs/design-log.md` and `docs/key-abstractions-and-solid.md` directly satisfies Section 2.2 of the brief (*"Use a documented deterministic rule... State the rule in your README or design notes"*).

---

## 4. Documenting Stage 2 OCP & LSP Evidence in `docs/key-abstractions-and-solid.md` (1–4 Nov 2026)

### Me

> I updated `docs/key-abstractions-and-solid.md` (`b60d633`) and `docs/design-log.md` (`c69af73`) to cover our Stage 2 additions (`VIPCustomer` and `CriticCustomer`).
>
> To make our Open/Closed Principle (OCP) explanation concrete for the report, I contrasted how `Waiter.takeOrder(Customer, Menu)` works in our code vs. how a procedural/non-OCP design with `instanceof` would look:
>
> ```java
> // Our polymorphic design in Waiter.takeOrder (unchanged from Stage 1):
> MenuItem item = customer.chooseMenuItem(menu);
> int billCents = customer.calculateBill(item.getPriceCents());
> ```
>
> Can you check if tracing Customer C2 (`VIPCustomer`, arrives Turn 4, orders Burger `$15.00 -> $13.50`, loses 5/turn) and Customer C4 (`CriticCustomer`, arrives Turn 10, orders Pasta `$18.00`, loses 12/turn) alongside this code snippet makes the OCP and Polymorphism section strong?

### Claude

Comparing the 2-line polymorphic dispatch in `Waiter.takeOrder` against an `if (customer instanceof VIPCustomer) ... else if (customer instanceof CriticCustomer)` anti-pattern is the clearest way to demonstrate **OCP** and **Polymorphism**:
1. **Concrete Evidence in Git History:** You can point the marker directly to `git diff stage-1..stage-2 -- src/entity/Waiter.java src/control/GameController.java`, which is completely empty (`0` lines changed).
2. **Concrete Runtime Trace:**
   - For C2 (`VIPCustomer`), `chooseMenuItem(menu)` inherits `Customer`'s `(2 - 1) % 3 = 1` (`Burger`, `1500` cents), while `calculateBill(1500)` dispatches to `VIPCustomer.calculateBill`, returning `1350` cents (`$13.50`).
   - For C4 (`CriticCustomer`), `chooseMenuItem(menu)` dispatches to `CriticCustomer.chooseMenuItem`, returning `Pasta` (`1800` cents, 2 prep units), while `calculateBill(1800)` inherits `Customer.calculateBill`, returning `1800` cents (`$18.00`).

---

## Verification & Key Ideas Formulated (Appendix I)

- **Key Ideas Formulated by Me:**
  1. Implementing the Critic's special behaviour via `CriticCustomer.chooseMenuItem(Menu)` (selecting the highest-priced item on the menu) so the Stage 2 Critic extension required zero changes to `Waiter`, `GameController`, or `GameStats`.
  2. Documenting the architectural trade-off in `docs/key-abstractions-and-solid.md` between the localized `chooseMenuItem` override and a cross-cutting "bad review" penalty on `GameStats`.
- **How I Verified AI Suggestions:**
  - Compiled `CriticCustomer.java` with Raghav's `VIPCustomer.java` and Aaryan's `ArrivalSchedule.java`, verified that Customer 4 (arriving on Turn 10) automatically orders `Pasta` (`$18.00`, 2 prep units) and loses 12 satisfaction per waiting turn, and confirmed all Stage 1 and Stage 2 test scripts pass.
