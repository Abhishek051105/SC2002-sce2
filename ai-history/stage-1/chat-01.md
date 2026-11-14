# Chat 01: Stage 1 Control Layer, Turn Loop & Package Skeleton

- **Repository:** `SC2002-sce2`
- **Contributor:** Aaryan
- **Stage:** 1 (Only Stage 1 baseline requirements known)
- **Tool:** Claude (claude.ai)
- **Dates:** 22 Oct 2026 – 29 Oct 2026 (SGT)

---

## 1. Aligning Package Structure and `Customer` Method Signatures (22–23 Oct 2026)

### Me (22 Oct)

> Hi, I want to get started with my Stage 1 control layer (`GameController`, `ArrivalSchedule`, `Objective`, `GameConfig`, `Main`) and `Restaurant`. Raghav has already pushed `Customer.java`, `CustomerStatus.java`, and `RegularCustomer.java`.
>
> Before I write `GameController` and `Restaurant`, how should I structure the packages (`boundary`, `control`, `entity`) and stubs so my code compiles while my teammates are still finishing `Waiter`, `Chef`, `Cashier`, `Menu`, `Order`, and `ConsoleUI`? Also, are there any methods `Customer` needs so `Waiter` and `Cashier` don't hardcode dish selection or bill calculation?

### Claude

To let all five teammates work in parallel without blocking each other, set up the three-package **Boundary–Control–Entity (BCE)** layout (`src/boundary`, `src/control`, `src/entity`) with method-signature stubs (`throw new UnsupportedOperationException("TODO")`) for the classes your teammates own:

1. **Moving `Customer`, `CustomerStatus`, and `RegularCustomer` into `src/entity/`:**
   - Having all domain classes in `package entity;`, `GameController` / `ArrivalSchedule` / `Objective` / `GameConfig` / `Main` in `package control;`, and `ConsoleUI` in `package boundary;` makes the BCE separation explicit in the directory tree and UML diagrams.

2. **Two Methods `Customer` Needs for Encapsulation & Future Extensibility:**
   - **`chooseMenuItem(Menu menu)`:** Section 2.2 of the brief states *"Customers choose their own food. The manager never chooses it."* Putting `chooseMenuItem(Menu)` on `Customer` (for example, deterministically picking `menu.getItem((customerId - 1) % menu.size())`) means `Waiter.takeOrder` asks the customer object for its choice instead of deciding inside `Waiter`.
   - **`calculateBill(int rawPriceCents)`:** Putting `calculateBill(int)` on `Customer` (returning `rawPriceCents` for `RegularCustomer`) means the customer object is responsible for any customer-specific pricing rules.
   - **Turn tracking getters (`getServedTurn()` and `getReadyToPayTurn()`):** Section 3.2 Step 4 states that a customer served on an earlier turn finishes eating to become `READY_TO_PAY`, and a customer who became `READY_TO_PAY` this turn pays next turn at the earliest. Storing `servedTurn` and `readyToPayTurn` on `Customer` lets `GameController` and `Cashier` enforce the one-turn delay accurately.

---

## 2. Designing `GameController`'s 6-Step Turn Loop (25–26 Oct 2026)

### Me (25 Oct)

> I'm writing `GameController.java` now. Section 3.2 of the brief lists 6 steps per turn:
> 1. Arrivals
> 2. Manager choices (Waiter & Chef)
> 3. Kitchen and serving
> 4. Eating and payment
> 5. Waiting and abandonment
> 6. Result (after Turn 18)
>
> In Step 2, if the player picks an invalid Waiter or Chef action (like trying to seat a customer at an occupied table, or serving an order that isn't `READY`), Section 3.4 says: *"An invalid choice must explain why it failed and let the manager choose again, without using up the staff task."*
>
> Here is my `handleWaiter()` loop:
>
> ```java
> private void handleWaiter() {
>     Waiter waiter = restaurant.getWaiter();
>     while (true) {
>         WaiterTask task = ui.askWaiterTask();
>         if (task == WaiterTask.WAIT) {
>             ui.showMessage("Waiter waits this turn.");
>             return;
>         }
>         try {
>             switch (task) {
>                 case SEAT: {
>                     int cid = ui.askCustomerId("Customer to seat");
>                     int tno = ui.askTableNo(restaurant.getTables().size());
>                     Customer c = getCustomerOrThrow(cid);
>                     Table t = restaurant.getTableByNumber(tno);
>                     if (t == null) {
>                         throw new InvalidActionException("Table " + tno + " does not exist");
>                     }
>                     waiter.seat(c, t);
>                     restaurant.removeFromQueue(c);
>                     ui.showMessage("Waiter seated C" + cid + " at Table " + tno + ".");
>                     return;
>                 }
>                 case TAKE_ORDER: {
>                     int cid = ui.askCustomerId("Take order from");
>                     Customer c = getCustomerOrThrow(cid);
>                     Order order = waiter.takeOrder(c, restaurant.getMenu());
>                     restaurant.addOrder(order);
>                     ui.showMessage("C" + cid + " ordered " + order.getItem().getName() + ".");
>                     return;
>                 }
>                 case SERVE: {
>                     int oid = ui.askCustomerId("Serve order for customer");
>                     Order order = getOrderOrThrow(oid);
>                     waiter.serve(order, currentTurn);
>                     ui.showMessage("Waiter served Order " + oid + " (" + order.getItem().getName() + ").");
>                     return;
>                 }
>                 default:
>                     return;
>             }
>         } catch (InvalidActionException e) {
>             ui.showError(e.getMessage());
>         }
>     }
> }
> ```
>
> Does this `while (true)` + `try / catch (InvalidActionException e)` structure handle retry without consuming the Waiter's turn?

### Claude

Yes, that `while (true)` + `try / catch (InvalidActionException e)` loop is the cleanest way to implement Section 3.4:
- When `waiter.seat(...)`, `waiter.takeOrder(...)`, `waiter.serve(...)`, or `getCustomerOrThrow(...)` throws `InvalidActionException`, execution jumps immediately to `catch (InvalidActionException e)`, prints the reason via `ui.showError(e.getMessage())`, and repeats the `while (true)` loop so the manager can choose again.
- Notice the order in `case SEAT`: you call `waiter.seat(c, t)` **before** `restaurant.removeFromQueue(c)`. That is crucial—if `t` is already occupied, `waiter.seat(c, t)` throws `InvalidActionException` before `c` is removed from the waiting queue.
- Once a valid action succeeds, `return;` exits `handleWaiter()` immediately, and the `Chef` step runs next (`handleChef()`), which means an order created in `case TAKE_ORDER` is already in `restaurant.getOrders()` and available for the Chef to cook on the very same turn (satisfying Section 3.2 Step 2: *"The Chef may work on an order the Waiter took this turn"*).

---

## 3. Recording the Staff Turn Reset Design Decision (29 Oct 2026)

### Me (29 Oct)

> At the start of each turn in `GameController.playTurn()`, we call `s.startNewTurn()` on every `Staff` in `restaurant.getAllStaff()` before calling `ui.showStartOfTurn(...)`. I'm adding an entry to `docs/design-log.md` explaining why `startNewTurn()` runs at the very top of `playTurn()`. Can you check if my explanation is clear?
>
> > Calling `startNewTurn()` on every staff member in `restaurant.getAllStaff()` at the start of `playTurn()` before `ui.showStartOfTurn()` ensures the start-of-turn display always shows `Ready` for the new turn, and adding any future `Staff` subclass to `getAllStaff()` resets it automatically.

### Claude

That explanation is clear and highlights two good points:
1. **Correct UI state at turn start:** Resetting before `ui.showStartOfTurn()` guarantees that staff who acted on Turn $t-1$ display as `Ready` at the start of Turn $t$.
2. **Polymorphic collection (`List<Staff>`):** Iterating over `restaurant.getAllStaff()` treats all staff uniformly through the `Staff` abstraction (`startNewTurn()`), so `GameController` does not need separate reset calls for each role.

---

## Verification & Key Ideas Formulated (Appendix I)

- **Key Ideas Formulated by Me:**
  1. Structuring `handleWaiter()` and `handleChef()` around a `while (true)` retry loop catching `InvalidActionException` so invalid manager inputs display a specific domain error and re-prompt without consuming the staff member's turn.
  2. Calling `waiter.seat(c, t)` before `restaurant.removeFromQueue(c)` so failed seating attempts never corrupt the waiting queue.
  3. Resetting all staff polymorphically via `restaurant.getAllStaff()` at the top of `playTurn()`.
- **How I Verified AI Suggestions:**
  - Compiled my control classes (`GameController`, `ArrivalSchedule`, `Objective`, `GameConfig`, `Main`) and `Restaurant` (`513676a`) against my teammates' entity and boundary classes, and verified `docs/design-log.md` (`b088ecd`).
