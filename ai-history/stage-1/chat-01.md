# Chat 01: Stage 1 Architecture, Kitchen Entities & Integration

- **Repository:** `SC2002-sce2`
- **Contributor:** Abhishek
- **Stage:** 1 (Only Stage 1 baseline requirements known)
- **Tool:** Claude (claude.ai)
- **Dates:** 20 Oct 2026 – 29 Oct 2026 (SGT)

---

## 1. Breaking Down the Stage 1 Specification & Package Structure (20 Oct 2026)

### Me

> Hi, my group of 5 (Raghav, Abhishek, Aaryan, Aekas, Ayush) is starting Stage 1 of our SC2002 Restaurant Rush project in Java. Right now we only need to build the Stage 1 baseline game: Regular customers, 3 menu items (Salad $9 / 1 unit, Burger $15 / 1 unit, Pasta $18 / 2 units), 2 tables, 1 Waiter, 1 Chef, 1 Cashier, 18 turns, and the 6-step turn sequence.
>
> We want to follow the Boundary-Control-Entity (BCE) structure from our lecture slides and split the classes cleanly across the 5 of us so we don't step on each other's files. Here is our initial idea for splitting the classes:
> - Raghav: `Customer`, `RegularCustomer`, `CustomerStatus`
> - Aaryan: `GameController`, `ArrivalSchedule`, `Objective`, `GameConfig`, `Restaurant`, `Main`
> - Aekas: `Staff`, `Waiter`, `Cashier`, `Table`, `GameStats`, `InvalidActionException`
> - Me (Abhishek): `Money`, `MenuItem`, `Menu`, `Order`, `OrderStatus`, `Chef`
> - Ayush: `ConsoleUI` and test scripts
>
> Does this package and responsibility split follow SRP and BCE properly, and are there any design hooks we should agree on before coding?

### Claude

Your five-way split aligns very cleanly with the **Boundary–Control–Entity (BCE)** architecture and the **Single Responsibility Principle (SRP)**:

1. **Package Boundaries (`boundary`, `control`, `entity`):**
   - **`boundary` (`ConsoleUI`):** Owns only `Scanner` input reading and `System.out` formatting. It should never mutate domain state directly or decide whether a move is legal.
   - **`control` (`GameController`, `ArrivalSchedule`, `Objective`, `GameConfig`, `Main`):** Orchestrates the 6-step turn loop, routes player choices from `ConsoleUI` to the domain objects, and evaluates the end-of-game objective after Turn 18.
   - **`entity` (`Customer`, `RegularCustomer`, `Staff`, `Waiter`, `Chef`, `Cashier`, `Table`, `Menu`, `MenuItem`, `Order`, `Money`, `GameStats`):** Encapsulates state and enforces domain invariants (throwing `InvalidActionException` when a player attempts an illegal action).

2. **Key Interfaces & Hooks to Agree On Before Coding:**
   - **Abstract `Customer` base class:** Even though Stage 1 only has `RegularCustomer`, making `Customer` abstract now with overridable hooks for `getSatisfactionDecay()`, `chooseMenuItem(Menu)`, and `calculateBill(int rawPriceCents)` means `Waiter` and `Cashier` only ever talk to `Customer`. If new customer types are introduced in future stages, `GameController`, `Waiter`, and `Cashier` won't need `instanceof` checks (Open/Closed Principle).
   - **Integer cents in `Money` and `MenuItem`:** The brief specifies storing currency as integer cents (`$9.00` = `900`, `$15.00` = `1500`, `$18.00` = `1800`) and formatting for display in one place (`Money.format(int cents)`).
   - **Order lifecycle (`OrderStatus`):** Keep transitions explicit inside `Order` (`PLACED -> COOKING -> READY -> SERVED -> PAID` plus `CANCELLED` if a customer abandons the restaurant) rather than exposing a public `setStatus(...)` setter.
   - **One-task-per-turn rule in `Staff`:** Have `Waiter`, `Chef`, and `Cashier` extend an abstract `Staff` class that tracks `actedThisTurn` via `protected checkCanAct()` and `protected markActed()`.

---

## 2. Reviewing My `Money`, `MenuItem`, `Menu`, `Order`, and `Chef` Draft (27 Oct 2026)

### Me

> I have written my 5 kitchen/menu classes (`Money.java`, `MenuItem.java`, `Menu.java`, `Order.java`, `Chef.java`). Here are my implementations of `Order` and `Chef`:
>
> ```java
> package entity;
>
> public class Order {
>     private int orderId;
>     private Customer customer;
>     private MenuItem item;
>     private int billCents;
>     private int remainingPrepUnits;
>     private OrderStatus status;
>     private int readyTurn = -1;
>
>     public Order(int orderId, Customer customer, MenuItem item, int billCents) {
>         if (customer == null || item == null) {
>             throw new IllegalArgumentException("Customer and item cannot be null");
>         }
>         this.orderId = orderId;
>         this.customer = customer;
>         this.item = item;
>         this.billCents = billCents;
>         this.remainingPrepUnits = item.getPrepUnits();
>         this.status = OrderStatus.PLACED;
>     }
>
>     public void advancePreparation(int currentTurn) throws InvalidActionException {
>         if (status != OrderStatus.PLACED && status != OrderStatus.COOKING) {
>             throw new InvalidActionException("Order " + orderId + " is not waiting for preparation (status: " + status + ")");
>         }
>         status = OrderStatus.COOKING;
>         remainingPrepUnits--;
>         if (remainingPrepUnits <= 0) {
>             status = OrderStatus.READY;
>             readyTurn = currentTurn;
>         }
>     }
>     // ... markServed(), markPaid(), cancel(), getters
> }
> ```
>
> And in `Chef.java`:
>
> ```java
> package entity;
>
> public class Chef extends Staff {
>     public Chef() {
>         super("Chef");
>     }
>
>     public void workOn(Order order, int currentTurn) throws InvalidActionException {
>         checkCanAct();
>         if (order == null) {
>             throw new InvalidActionException("No such order");
>         }
>         order.advancePreparation(currentTurn);
>         markActed();
>     }
> }
> ```
>
> Can you check if `Chef.workOn` and `Order.advancePreparation` handle the "same-turn serve" restriction and invalid-action rollback properly?

### Claude

Your `Order` and `Chef` implementations handle both requirements cleanly:

1. **Invalid-Action Rollback (`checkCanAct()` before `advancePreparation()`, `markActed()` last):**
   - In `Chef.workOn`, you call `checkCanAct()` first, then `order.advancePreparation(currentTurn)`, and only call `markActed()` on the final line.
   - If the player enters an order ID that is already `READY`, `SERVED`, `PAID`, or `CANCELLED`, `order.advancePreparation(currentTurn)` throws `InvalidActionException` *before* `markActed()` is reached. That guarantees the Chef does not lose their turn on an invalid choice (satisfying Section 3.4 of the brief).

2. **Same-Turn Serve Restriction (`readyTurn`):**
   - Recording `readyTurn = currentTurn` when `remainingPrepUnits` reaches `0` is a great design choice. Even though the Waiter acts before the Chef in Stage 1, storing `readyTurn` inside `Order` lets `Order.canBeServedOnTurn(int currentTurn)` enforce `status == OrderStatus.READY && readyTurn < currentTurn` as a domain invariant inside `entity`, rather than relying only on call order in `GameController`.

3. **Two Small Suggestions to Check:**
   - **Constructor validation in `MenuItem`:** Make sure `MenuItem(String name, int priceCents, int prepUnits)` rejects `priceCents <= 0` or `prepUnits < 1` with an `IllegalArgumentException`, since an item with `0` prep units would never enter `READY` properly.
   - **Matching Aaryan's `Restaurant` skeleton:** Check whether Aaryan's `Restaurant.java` calls `Menu.createDefault()` or `new Menu()`, and whether `ConsoleUI` or `Restaurant` calls `menu.getItems()`. Keeping `public static Menu createDefault() { return new Menu(); }` and returning `Collections.unmodifiableList(items)` from `getItems()` will prevent compile errors when integrating with `Restaurant`.

---

## 3. Fixing Integration with `Restaurant` (27 Oct 2026)

### Me

> Good catch — when I compiled against Aaryan's `Restaurant.java` on `main`, `Restaurant` called `Menu.createDefault()`. Also I noticed an old `src/restaurant/Cashier.java` file from an early test that was in the wrong package. I added `createDefault()` and `Collections.unmodifiableList(items)` to `Menu.java` and deleted the stray `src/restaurant/Cashier.java`. It compiles with zero errors now.

### Claude

That resolves both issues cleanly:
- Providing `Menu.createDefault()` keeps backward compatibility with `Restaurant`'s constructor while still allowing `new Menu()` to populate the three default Stage 1 items (`Salad`, `Burger`, `Pasta`).
- Returning `Collections.unmodifiableList(items)` from `getItems()` protects encapsulation so external callers can iterate over the menu without mutating the internal `List<MenuItem>`.
- Removing the stray `src/restaurant/Cashier.java` ensures all entity classes live consistently under the `entity` package.

---

## 4. Pre-Tag Stage 1 Refactoring & Encapsulation Review (29 Oct 2026)

### Me

> Before we tag `stage-1`, I'm doing an encapsulation and design pass across our classes:
> 1. In `GameController.handleEatingAndPayment()` and `handleEndOfTurnWaiting()`, the controller was manually removing orders, releasing tables, and updating `GameStats`. I want to move those two multi-step cleanups into `Restaurant.completePayment(Customer, Order, Table, int)` and `Restaurant.customerLeft(Customer, Order, Table)` so `Restaurant` keeps its own lists consistent.
> 2. In `Customer`, I want to make the lifecycle state-transition methods (`seat`, `orderTaken`, `serve`, `finishEating`, `markPaid`, `decreaseSatisfaction`) `final` so subclasses can only override the hook methods (`getSatisfactionDecay`, `chooseMenuItem`, `calculateBill`).
>
> Does this strengthen our SRP and encapsulation evidence for the report?

### Claude

Both refactors directly strengthen your OOP design evidence:

1. **Moving `completePayment` and `customerLeft` into `Restaurant` (SRP & Information Expert):**
   - Previously, `GameController` had to remember to update three separate objects (`Table.release()`, `GameStats.recordPaidCustomer(...)` or `recordAbandonment()`, and `waitingQueue`) whenever a customer paid or abandoned the restaurant.
   - Moving that coordination into `Restaurant` means `Restaurant` is the single Information Expert responsible for maintaining consistency across its tables, queue, orders, and stats.

2. **Making `Customer` lifecycle methods `final` (Template Method Pattern):**
   - Marking `seat()`, `orderTaken()`, `serve()`, `finishEating()`, `markPaid()`, and `decreaseSatisfaction()` as `final` locks the state-machine skeleton (`WAITING -> SEATED -> WAITING_FOR_FOOD -> SERVED -> READY_TO_PAY -> PAID / LEFT`) in the superclass.
   - Subclasses can customize *rates and policies* (`getSatisfactionDecay()`, `chooseMenuItem()`, `calculateBill()`) without ever breaking the state-transition invariants required by `Waiter`, `Chef`, and `Cashier`.

---

## Verification & Key Ideas Formulated (Appendix I)

- **Key Ideas Formulated by Me:**
  1. Separating `Order` preparation progress (`remainingPrepUnits`, `readyTurn`) from `Chef` action validation (`checkCanAct()` before mutating `Order`, `markActed()` last) so failed actions never consume the Chef's turn.
  2. Moving multi-object cleanup on payment and abandonment out of `GameController` into `Restaurant.completePayment(...)` and `Restaurant.customerLeft(...)`.
  3. Locking `Customer` lifecycle transitions as `final` methods while leaving `getSatisfactionDecay()`, `chooseMenuItem()`, and `calculateBill()` as extension hooks.
- **How I Verified AI Suggestions:**
  - Compiled the full project with `javac -d out $(find src -name "*.java")` and ran `tests/stage1_victory.txt` (`5 paid, $66.00, 79.2 avg satisfaction, VICTORY`), `tests/stage1_defeat.txt` (`4 paid, $51.00, 58.0 avg satisfaction, DEFEAT`), and `tests/stage1_invalid_inputs.txt` before tagging `stage-1` (`a312b7b`).
