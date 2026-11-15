# Chat 01: Stage 2 Extension, Sequence Diagram & Code Review Fixes

- **Repository:** `SC2002-sce2`
- **Contributor:** Abhishek
- **Stage:** 2 (Stage 2 requirements just arrived; Stage 3 unknown)
- **Tool:** Claude (claude.ai)
- **Dates:** 31 Oct 2026 – 4 Nov 2026 (SGT)

---

## 1. Planning Stage 2 Extensions on Top of `stage-1` (31 Oct 2026)

### Me

> We just finished tagging `stage-1` and are starting Stage 2. Stage 2 adds two new customer types:
> - `VIPCustomer`: loses 5 satisfaction per waiting turn, gets 10% off the bill.
> - `CriticCustomer`: loses 12 satisfaction per waiting turn, full price, and needs a distinct order preference or reaction to service that we define and document.
>
> Since we already put `getSatisfactionDecay()`, `chooseMenuItem(Menu)`, and `calculateBill(int)` in `Customer` during Stage 1, we decided that `CriticCustomer` will always choose the most expensive item on the menu (`Pasta`, $18.00, 2 prep units). And for our 6 arrivals (turns 1, 4, 7, 10, 13, 16), we'll make Customer 2 (Turn 4) a VIP and Customer 4 (Turn 10) a Critic.
>
> Here is how we are splitting Stage 2:
> - Raghav: `VIPCustomer` and adding `abstract String getTypeLabel()` to `Customer`
> - Aekas: `CriticCustomer` and updating the SOLID & design docs
> - Aaryan: `CustomerType` enum, `ArrivalSchedule` creation switch, `GameConfig` arrival type list, `regular` mode flag in `Main`, and README Stage 1 -> Stage 2 change analysis
> - Ayush: `ConsoleUI` updates (showing customer type label and locked bill) and Stage 2 test scripts (`stage2_victory.txt`, `stage2_defeat.txt`, `stage2_invalid_inputs.txt`)
> - Me (Abhishek): Stage 2 UML sequence diagram (`docs/uml/stage2/sequence-diagram.puml`), Stage 2 class diagram updates (`docs/uml/stage2/class-diagram.puml`), and PR integration/tagging `stage-2`.
>
> Can you check if this design keeps `GameController`, `Waiter`, `Chef`, `Cashier`, and `Order` completely untouched from `stage-1`?

### Claude

Yes — with that design, **`GameController`, `Waiter`, `Chef`, `Cashier`, `Order`, `Menu`, `MenuItem`, `Table`, `Staff`, `GameStats`, `Objective`, and `Money` require zero lines of modification** between `stage-1` and `stage-2`:

1. **Why `Waiter.takeOrder` stays unchanged:**
   - In `stage-1`, `Waiter.takeOrder(Customer customer, Menu menu)` already calls:
     ```java
     MenuItem item = customer.chooseMenuItem(menu);
     int bill = customer.calculateBill(item.getPriceCents());
     ```
   - When `customer` is a `VIPCustomer`, dynamic dispatch calls `VIPCustomer.calculateBill(1500)`, returning `1350` (`$13.50`) and locking `1350` into the `Order` at creation time.
   - When `customer` is a `CriticCustomer`, dynamic dispatch calls `CriticCustomer.chooseMenuItem(menu)`, returning `Pasta` (`$18.00`, 2 prep units).

2. **Why `GameController` and `Restaurant` stay unchanged:**
   - Satisfaction decay at the end of each turn already calls `customer.decreaseSatisfaction()`, which dispatches to `getSatisfactionDecay()` (`8` for Regular, `5` for VIP, `12` for Critic).

3. **Single Creation Switch in `ArrivalSchedule`:**
   - Only `ArrivalSchedule.createCustomer(...)` needs a `switch` over `CustomerType` (`REGULAR`, `VIP`, `CRITIC`) to instantiate the concrete subclass. Centralizing object creation in one factory method while all downstream code operates on the abstract `Customer` type is the textbook way to satisfy the Open/Closed Principle.

---

## 2. Reviewing My Stage 2 VIP Sequence Diagram in PlantUML (4 Nov 2026)

### Me

> I drafted `docs/uml/stage2/sequence-diagram.puml` in PlantUML to trace Customer C2 (the `VIPCustomer` arriving on Turn 4 in `tests/stage2_victory.txt`) from arrival on Turn 4 through ordering Burger at a 10% discount (`$13.50`) on Turn 5, cooking on Turn 5, serving on Turn 6, eating on Turn 7, and Cashier payment on Turn 8.
>
> Here is my Turn 5 `takeOrder` section in PlantUML:
>
> ```plantuml
> == Turn 5: Manager chooses Waiter -> Take Order for C2, Chef -> Cook Order 2 ==
> Controller -> UI : askWaiterTask()
> UI --> Controller : WaiterTask.TAKE_ORDER
> Controller -> UI : askCustomerId("Take order from")
> UI --> Controller : 2
> Controller -> Waiter : takeOrder(c2, menu)
> activate Waiter
> Waiter -> Waiter : checkCanAct()
> Waiter -> C2 : chooseMenuItem(menu)
> activate C2
> C2 -> Menu : getItem(1)
> Menu --> C2 : Burger ($15.00, 1 unit)
> C2 --> Waiter : Burger
> deactivate C2
> Waiter -> C2 : calculateBill(1500)
> activate C2
> C2 --> Waiter : 1350 ($13.50, 10% VIP discount)
> deactivate C2
> create Order2
> Waiter -> Order2 : new Order(2, c2, Burger, 1350)
> Waiter -> C2 : orderTaken()
> Waiter -> Waiter : markActed()
> Waiter --> Controller : order2
> deactivate Waiter
> Controller -> Restaurant : addOrder(order2)
> ```
>
> Does this match the exact method calls in `GameController.handleWaiter()` and `Waiter.takeOrder()`, and is there anything the brief requires in the sequence diagram that I should add?

### Claude

Your Turn 5 interaction matches `GameController.handleWaiter()` and `Waiter.takeOrder()` accurately:
- `Waiter.takeOrder(c2, menu)` calls `checkCanAct()`, then `c2.chooseMenuItem(menu)` (inherited from `Customer`, which picks index `(customerId - 1) % menu.size()` = index `1`, `Burger`), then `c2.calculateBill(1500)` (overridden in `VIPCustomer` to return `(1500 * 90 + 50) / 100 = 1350`), constructs `new Order(2, c2, Burger, 1350)`, calls `c2.orderTaken()`, and calls `markActed()`.

**Two Improvements to Make Sure Your Diagram Meets All Rubric Criteria (Section 5 & Briefing Slide 6):**
1. **State the Use Case, Stage, Starting Conditions, and Outcome in a Header/Legend Note:**
   - Slide 6 of the briefing requires stating the stage, starting conditions (e.g., Turn 4 start state: C1 seated at Table 1 with Order 1 ready to serve, Table 2 free, revenue `$0.00`), and outcome (Turn 8: C2 pays `$13.50` at satisfaction `90`, total revenue `$22.50`, Table 2 freed).
2. **Include Guards, Alternatives (`alt`), and Loops (`loop`):**
   - Wrap the turn progression in a `loop [for each turn 1..18]` fragment.
   - Inside Turn 4 or Turn 5, include an `alt` fragment showing the Waiter retry loop in `handleWaiter()` when an invalid action is attempted (for example, trying to seat C2 at occupied Table 1 throws `InvalidActionException("Table 1 is occupied")`, `ui.showError(...)` displays the message, and the `while (true)` loop prompts the manager again without calling `markActed()`).
   - Include an `alt` showing how a manager choice (e.g., taking C2's order on Turn 5 vs. choosing `WAIT`) changes C2's satisfaction and bill outcome, plus the Turn 18 `Objective.isMet(stats)` check.

---

## 3. Fixing Two Code Review Issues Before Tagging `stage-2` (4 Nov 2026)

### Me

> While reviewing the Stage 2 classes before tagging `stage-2`, I noticed two small issues:
> 1. In `Customer.java`, when `getTypeLabel()` was added, the `@return true if the customer leaves` Javadoc comment above `decreaseSatisfaction()` ended up above `getTypeLabel()` instead, and `RegularCustomer.getTypeLabel()` had tab indentation.
> 2. In `CriticCustomer.chooseMenuItem(Menu menu)`, it immediately does `MenuItem best = menu.getItem(0);` without checking `if (menu == null || menu.size() == 0)` like `Customer.chooseMenuItem(Menu menu)` does.
>
> Does missing that null/empty check in `CriticCustomer.chooseMenuItem` violate LSP?

### Claude

Yes — that is a classic **Liskov Substitution Principle (LSP)** point:
- In the superclass `Customer.chooseMenuItem(Menu menu)`, passing `null` or an empty `Menu` throws an explicit `IllegalArgumentException("Menu cannot be null or empty")`.
- Without that guard in `CriticCustomer.chooseMenuItem(Menu menu)`, passing `null` throws a `NullPointerException` and passing an empty menu throws an `IndexOutOfBoundsException` from `menu.getItem(0)`.
- Adding the exact same precondition check at the top of `CriticCustomer.chooseMenuItem(Menu menu)`:
  ```java
  if (menu == null || menu.size() == 0) {
      throw new IllegalArgumentException("Menu cannot be null or empty");
  }
  ```
  ensures `CriticCustomer` honors the superclass contract identically. Fixing the misplaced Javadoc on `Customer.decreaseSatisfaction()` and the indentation in `RegularCustomer` in a separate small commit before tagging `stage-2` keeps the commit history clean and easy to review.

---

## 4. Colour-Coding the Stage 2 Class Diagram (`docs/uml/stage2/class-diagram.puml`) (4 Nov 2026)

### Me

> In `docs/uml/stage2/class-diagram.puml`, I want to make it visually obvious to the marker which classes were newly added in Stage 2 (`VIPCustomer`, `CriticCustomer`, `CustomerType`) and which existing classes were modified (`Customer`, `RegularCustomer`, `ArrivalSchedule`, `GameConfig`, `Main`, `ConsoleUI`), while leaving the 12 untouched Stage 1 classes in the default background colour.
>
> In PlantUML, can I apply `#D9F2D9` (light green) to new Stage 2 classes and `#FFF2C2` (light yellow) to modified Stage 1 classes directly on the `class` / `enum` declarations?

### Claude

Yes! PlantUML supports per-class background hex colours right after the stereotype:

```plantuml
abstract class Customer <<entity>> #FFF2C2 {
    ...
    + {abstract} getTypeLabel() : String
}

class VIPCustomer <<entity>> #D9F2D9 {
    - {static} DECAY_PER_TURN : int = 5
    + VIPCustomer(customerId : int, arrivalTurn : int)
    + getSatisfactionDecay() : int
    + calculateBill(rawPriceCents : int) : int
    + getTypeLabel() : String
}

class CriticCustomer <<entity>> #D9F2D9 {
    - {static} DECAY_PER_TURN : int = 12
    + CriticCustomer(customerId : int, arrivalTurn : int)
    + getSatisfactionDecay() : int
    + chooseMenuItem(menu : Menu) : MenuItem
    + getTypeLabel() : String
}

enum CustomerType <<control>> #D9F2D9 {
    REGULAR
    VIP
    CRITIC
}
```

Adding a small `legend` block at the bottom of the diagram explaining:
- **White:** Unchanged from Stage 1 (`GameController`, `Waiter`, `Chef`, `Cashier`, `Order`, `Menu`, `MenuItem`, `Table`, `Staff`, `Restaurant`, `GameStats`, `Objective`, `Money`)
- **Yellow (`#FFF2C2`):** Modified in Stage 2
- **Green (`#D9F2D9`):** Added in Stage 2

visually proves your **Open/Closed Principle (OCP)** claim at a single glance: the core game engine, kitchen, and staff hierarchies are completely white (untouched).

---

## 5. Verifying Guard Conditions & Activation Lifelines in the Stage 2 Sequence Diagram (4 Nov 2026)

### Me

> In my Stage 2 sequence diagram (`docs/uml/stage2/sequence-diagram.puml`), I added two `alt` fragments:
> 1. On Turn 4, an `alt` inside `loop [until valid Waiter task or WAIT]` showing what happens if the manager tries to seat C2 (`VIPCustomer`) at occupied Table 1 (`Waiter.seat(c2, table1)` throws `InvalidActionException("Table 1 is occupied")`, `ui.showError(...)` prints the error, and the loop re-prompts without calling `markActed()`) vs. seating C2 at free Table 2.
> 2. On Turn 5, an `alt` comparing the manager choosing `TAKE_ORDER` for C2 (locking in `$13.50` and serving on Turn 6 at satisfaction `90`) vs. choosing `WAIT` (delaying C2 by 1 turn so C2's satisfaction drops to `85`).
>
> Can you check if my activation bars (`activate` / `deactivate`) on `Waiter` need to be closed inside both branches of the Turn 4 `alt` block so PlantUML doesn't draw an unclosed activation bar?

### Claude

Good catch — PlantUML processes `activate` and `deactivate` sequentially from top to bottom rather than branching state across `alt` / `else` blocks:
- If you call `activate Waiter` inside the `[invalid choice: Table 1 is occupied]` branch, you must call `deactivate Waiter` right after `Waiter --x Controller : throws InvalidActionException("Table 1 is occupied")` *before* the `else [valid choice: Table 2 is free]` divider.
- Then, inside the `else` branch, call `activate Waiter` when `Controller -> Waiter : seat(c2, table2)` is invoked and `deactivate Waiter` when `Waiter --> Controller : void` returns.
- Doing that keeps the lifeline activation bars properly balanced in both branches of the `alt` fragment.

---

## Verification & Key Ideas Formulated (Appendix I)

- **Key Ideas Formulated by Me:**
  1. Selecting Customer C2 (the Turn 4 `VIPCustomer`) in `tests/stage2_victory.txt` as the Stage 2 sequence diagram scenario to showcase polymorphic dispatch of `VIPCustomer.calculateBill(1500) -> 1350` and `VIPCustomer.getSatisfactionDecay() -> 5` without modifying `Waiter` or `GameController`.
  2. Identifying and fixing the missing `menu == null || menu.size() == 0` guard in `CriticCustomer.chooseMenuItem(Menu)` so our LSP claim in `docs/key-abstractions-and-solid.md` holds strictly in code.
- **How I Verified AI Suggestions:**
  - Rendered `docs/uml/stage2/class-diagram.png` and `docs/uml/stage2/sequence-diagram.png` with PlantUML, verified every arrow and method signature against `GameController.java`, `Waiter.java`, and `VIPCustomer.java`, and ran all 3 Stage 2 test scripts (`73.50 / 75.6`, `58.50 / 53.8`, `73.50 / 75.6`) plus all 3 Stage 1 regression scripts (`regular` mode: `79.2 / 58.0 / 79.2`) before tagging `stage-2` (`ac92db9`).
