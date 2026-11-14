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

## Verification & Key Ideas Formulated (Appendix I)

- **Key Ideas Formulated by Me:**
  1. Implementing the Critic's special behaviour via `CriticCustomer.chooseMenuItem(Menu)` (selecting the highest-priced item on the menu) so the Stage 2 Critic extension required zero changes to `Waiter`, `GameController`, or `GameStats`.
  2. Documenting the architectural trade-off in `docs/key-abstractions-and-solid.md` between the localized `chooseMenuItem` override and a cross-cutting "bad review" penalty on `GameStats`.
- **How I Verified AI Suggestions:**
  - Compiled `CriticCustomer.java` with Raghav's `VIPCustomer.java` and Aaryan's `ArrivalSchedule.java`, verified that Customer 4 (arriving on Turn 10) automatically orders `Pasta` (`$18.00`, 2 prep units) and loses 12 satisfaction per waiting turn, and confirmed all Stage 1 and Stage 2 test scripts pass.
