# Chat 01: Stage 3 `Host` Entity, `GameController` Wiring & `Main`

- **Repository:** `SC2002-sce2`
- **Contributor:** Aaryan
- **Stage:** 3 (Stage 3 requirements just arrived: `Host`, `ComboMeal`, `HappyHour`)
- **Tool:** Claude (claude.ai)
- **Dates:** 5 Nov 2026 – 8 Nov 2026 (SGT)

---

## 1. Designing `Host` and Integrating It into `Restaurant` (5 Nov 2026)

### Me (5 Nov)

> We are starting Stage 3 (`Host`, `ComboMeal`, `HappyHour`). My tasks for Stage 3 are:
> 1. `Host.java` in `src/entity/` (a new staff member who seats at most one queued customer per turn, in addition to the Waiter)
> 2. Adding `Host` to `Restaurant.java` (`getHost()` and including `host` in `getAllStaff()`)
> 3. Wiring `HappyHour`, `StandardPricing`, `currentPricing()`, `handleHappyHour()`, and `handleHost()` into `GameController.java`
> 4. Removing the temporary 2-argument `Waiter.takeOrder` wrapper once `GameController` passes `currentPricing()`
> 5. Updating `Main.java` to register the two `ComboMeal`s (`Salad + Pasta` and `Burger + Salad`) and add a `stage2` mode flag for Stage 2 regression scripts.
>
> For `Host.java`, I had `Host extend Staff` (`super("Host")`) and wrote `seat(Customer customer, Table table)` checking `checkCanAct()`, `customer.getStatus() == CustomerStatus.WAITING`, and `table.isOccupied()` before calling `table.assignCustomer(customer)`, `customer.seat()`, and `markActed()`.
>
> Why did we put `seat(Customer, Table)` on both `Host` and `Waiter` instead of putting `seat()` on `Staff` or creating a `SeatingStaff` superclass?

### Claude

Putting `seat(Customer, Table)` on `Host` and `Waiter` while keeping `Host extends Staff` is a clean design trade-off to document in your design log:

1. **Why `seat()` Does Not Belong on `Staff` (ISP & LSP):**
   - `Chef` and `Cashier` are also `Staff` subclasses, and neither of them seats customers. Putting `seat()` on `Staff` would force `Chef` and `Cashier` to inherit an operation they cannot perform (violating ISP and LSP).
2. **Trade-Off: Direct Subclassing (`Host extends Staff`) vs. Intermediate `SeatingStaff`:**
   - Introducing an extra abstract class `SeatingStaff extends Staff` just to share 8 lines of validation between `Host` and `Waiter` would add another layer to the inheritance hierarchy for only two classes. Keeping the hierarchy flat (`Waiter`, `Chef`, `Cashier`, `Host` all directly extending `Staff`) keeps the class diagram simple while `Staff` still shares the per-turn `checkCanAct()` / `markActed()` invariant.
3. **Adding `host` to `Restaurant.getAllStaff()`:**
   - Because `GameController.playTurn()` already loops over `restaurant.getAllStaff()` and calls `s.startNewTurn()`, adding `host` to `getAllStaff()` in `Restaurant.java` resets the Host every turn automatically.

---

## 2. Wiring `PricingPolicy` and `HappyHour` into `GameController` (7 Nov 2026)

### Me (7 Nov)

> Aekas has merged `feat/happy-hour` (`PricingPolicy`, `StandardPricing`, `HappyHour`, `Customer.recoverSatisfaction`, and the 3-argument `Waiter.takeOrder(Customer, Menu, PricingPolicy)`) into `main`.
>
> When I ran `git pull origin main` on my `feat/stage3-controller` branch (where I already committed `Host.java` and `Restaurant.java` on Nov 5), Git said:
> `You have divergent branches and need to specify how to reconcile them.`
> Should I merge or rebase here?
>
> Also, in `GameController`, I added:
> ```java
> private final HappyHour happyHour = new HappyHour();
> private final PricingPolicy standardPricing = new StandardPricing();
>
> private PricingPolicy currentPricing() {
>     if (happyHour.isActive()) {
>         return happyHour;
>     }
>     return standardPricing;
> }
> ```
> And in `handleWaiter()`'s `TAKE_ORDER` case, I updated `waiter.takeOrder(c, restaurant.getMenu(), currentPricing())` and added `happyHour.endTurn()` at the very end of `playTurn()`. Can I delete Aekas's temporary 2-argument `Waiter.takeOrder(Customer, Menu)` overload now?

### Claude

1. **Merging `main` into `feat/stage3-controller`:**
   - Because your Nov 5 `Host` commit was already pushed to your branch, use `git pull --no-rebase origin main` so Git records a clean merge commit (`Merge branch 'main' into feat/stage3-controller`) without rewriting your existing branch commit.

2. **`currentPricing()` and Dependency Inversion (DIP):**
   - Declaring `private final PricingPolicy standardPricing = new StandardPricing();` and having `currentPricing()` return `PricingPolicy` means `Waiter.takeOrder` receives a `PricingPolicy` interface reference (`happyHour` when active, `standardPricing` otherwise) without ever knowing which concrete pricing policy is running.
   - Placing `happyHour.endTurn()` at the **very end** of `playTurn()` (after `ui.showEndOfTurn(...)`) is critical: when the manager activates Happy Hour at the start of Turn 10 (`turnsRemaining = 2`), orders taken on Turn 10 get 20% off, then `endTurn()` decrements `turnsRemaining` to `1` at the end of Turn 10; on Turn 11, `isActive()` is still `true` (`turnsRemaining == 1`), waiting customers recover `+15` satisfaction and Turn 11 orders get 20% off, and `endTurn()` decrements `turnsRemaining` to `0` at the end of Turn 11.

3. **Removing the 2-Argument `Waiter.takeOrder(Customer, Menu)` Wrapper:**
   - Yes! Now that `GameController` calls the 3-argument `takeOrder(c, restaurant.getMenu(), currentPricing())`, nothing in the codebase calls the 2-argument wrapper. Deleting those 4 lines from `Waiter.java` removes `new StandardPricing()` from `Waiter`, making `Waiter` 100% decoupled from concrete `PricingPolicy` implementations.

---

## 3. Implementing `handleHappyHour()`, `handleHost()`, and `Main.java` (8 Nov 2026)

### Me (8 Nov)

> Ayush's `feat/stage3-ui` branch and Abhishek's `feat/combo-meal` branch are merged into `main` now. I wrote `handleHappyHour()`, `handleHost()`, and the `Main.java` combo registration:
>
> ```java
> private void handleHappyHour() {
>     if (happyHour.isAvailable() && ui.askStartHappyHour()) {
>         try {
>             happyHour.activate();
>         } catch (InvalidActionException e) {
>             ui.showError(e.getMessage());
>         }
>     }
>     if (happyHour.isActive()) {
>         for (Customer c : restaurant.getAllCustomers()) {
>             if (c.recoverSatisfaction(HappyHour.SATISFACTION_RECOVERY)) {
>                 ui.showMessage("C" + c.getCustomerId() + " recovers to " + c.getSatisfaction() + ".");
>             }
>         }
>         ui.showHappyHourStatus(happyHour.getTurnsRemaining());
>     }
> }
> ```
>
> And in `Main.java`:
> ```java
> String mode = args.length > 0 ? args[0] : "";
> List<CustomerType> types = mode.equalsIgnoreCase("regular")
>         ? GameConfig.ALL_REGULAR
>         : GameConfig.ARRIVAL_TYPES;
> ArrivalSchedule schedule = new ArrivalSchedule(GameConfig.ARRIVAL_TURNS, types);
> Objective objective = GameConfig.createBaseObjective();
> Restaurant restaurant = new Restaurant();
> if (!mode.equalsIgnoreCase("regular") && !mode.equalsIgnoreCase("stage2")) {
>     Menu menu = restaurant.getMenu();
>     MenuItem salad = menu.getItem(0);
>     MenuItem burger = menu.getItem(1);
>     MenuItem pasta = menu.getItem(2);
>     menu.addItem(new ComboMeal(salad, pasta));
>     menu.addItem(new ComboMeal(burger, salad));
> }
> ```
>
> Can you check the order of conditions in `handleHappyHour()` and the order of adding the two combos in `Main.java`?

### Claude

Both details are exact:
1. **Why `happyHour.isAvailable() && ui.askStartHappyHour()` Order Matters:**
   - Because Java's `&&` operator short-circuits when the left-hand side is `false`, `ui.askStartHappyHour()` is only prompted while Happy Hour has not yet been used (`isAvailable() == true`). Once activated on Turn 10, Turns 11–18 short-circuit on `isAvailable() == false` and never prompt the player again.
   - Using two separate `if` statements (`if (happyHour.isAvailable() && ...)` followed by `if (happyHour.isActive())`) ensures that on the turn Happy Hour is activated, `activate()` runs in the first `if` and then the second `if` immediately runs satisfaction recovery and displays `"2 turn(s) left including this one"`.
2. **Order of Adding Combo Meals in `Main.java`:**
   - Adding `new ComboMeal(salad, pasta)` (`$24.30`, 3 units) at index 3 and `new ComboMeal(burger, salad)` (`$21.60`, 2 units) at index 4 before starting `GameController` ensures C4 (the Critic on Turn 10) selects `Salad + Pasta` and C5 (Turn 13) selects `Burger + Salad`.
   - Skipping combo registration when `mode` is `"regular"` or `"stage2"` preserves the exact 3-item menu for Stage 1 and Stage 2 regression tests.

---

## Verification & Key Ideas Formulated (Appendix I)

- **Key Ideas Formulated by Me:**
  1. Implementing `Host extends Staff` and adding `host` to `Restaurant.getAllStaff()` so per-turn resets happen polymorphically without extra controller bookkeeping.
  2. Structuring `handleHappyHour()` with short-circuit `happyHour.isAvailable() && ui.askStartHappyHour()` followed by a separate `if (happyHour.isActive())` block and placing `happyHour.endTurn()` at the end of `playTurn()`.
  3. Adding the `stage2` CLI mode in `Main` alongside `regular` so all 6 Stage 1 and Stage 2 test scripts can be verified on the Stage 3 build.
- **How I Verified AI Suggestions:**
  - Compiled the Stage 3 build and verified that:
    - Host seating C1 followed by Waiter taking C1's order in the same turn works cleanly.
    - All 6 Stage 1 and Stage 2 regression scripts (`*_on_stage3.txt`) produce their exact historical outputs (`79.2 / 58.0 / 79.2` in `regular` mode and `75.6 / 53.8 / 75.6` in `stage2` mode).
    - `tests/stage3_victory.txt` completes with `5 paid, $81.54, 84.6 avg satisfaction, VICTORY`.
