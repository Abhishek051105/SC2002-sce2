# Chat 01: Stage 3 `ComboMeal` Design & Sequence Diagram Upgrade

- **Repository:** `SC2002-sce2`
- **Contributor:** Abhishek
- **Stage:** 3 (Stage 3 requirements just arrived: `Host`, `ComboMeal`, `HappyHour`)
- **Tool:** Claude (claude.ai)
- **Dates:** 5 Nov 2026 – 14 Nov 2026 (SGT)

---

## 1. Designing `ComboMeal` and `Menu.addItem` (5 Nov 2026)

### Me

> We are starting Stage 3, which adds three features on top of `stage-2`:
> 1. `Host`: seats at most 1 queued customer per turn (in addition to the Waiter).
> 2. `ComboMeal`: made of two existing `MenuItem`s; prep units = sum of the two components; price = 90% of their combined price rounded to the nearest cent (e.g., Salad $9 + Pasta $18 = $27 * 0.9 = $24.30, 3 units).
> 3. `HappyHour`: activated once per game at turn start, lasts 2 turns, gives 20% off new orders (applied after VIP discount) and +15 satisfaction recovery (max 100) to waiting/unserved customers.
>
> My code part for Stage 3 is `ComboMeal.java` and `Menu.addItem(MenuItem)`. Because I made `MenuItem` a concrete class in Stage 1 with `(String name, int priceCents, int prepUnits)`, I want `ComboMeal` to extend `MenuItem` and compute the combined name, 90% rounded price in cents, and summed prep units in `super(...)`. Here is my code:
>
> ```java
> package entity;
>
> // a combo is 2 existing menu items sold together, so it is still a MenuItem
> public class ComboMeal extends MenuItem {
>     private final MenuItem first;
>     private final MenuItem second;
>
>     public ComboMeal(MenuItem first, MenuItem second){
>         // name, price and prep units all come from the 2 items
>         // price = 90% of both added up, the +50 makes it round to the nearest cent
>         super(first.getName() + " + " + second.getName(),
>               ((first.getPriceCents() + second.getPriceCents()) * 90 + 50) / 100,
>               first.getPrepUnits() + second.getPrepUnits());
>         this.first = first;
>         this.second = second;
>     }
>
>     public MenuItem getFirst(){
>         return this.first;
>     }
>
>     public MenuItem getSecond(){
>         return this.second;
>     }
> }
> ```
>
> If we register two combos in `Main` (`Salad + Pasta` at index 3 and `Burger + Salad` at index 4) using `menu.addItem(...)`, will `Order`, `Chef`, `RegularCustomer`, `VIPCustomer`, and `CriticCustomer` work with combos automatically without changing any of those classes?

### Claude

Yes — making `ComboMeal` extend `MenuItem` and computing the derived attributes in `super(...)` means **zero changes are needed in `Order`, `Chef`, `RegularCustomer`, `VIPCustomer`, or `CriticCustomer`**:

1. **Price & Prep Units Verification:**
   - **Combo 1 (`Salad + Pasta`):**
     - Combined price: `900 + 1800 = 2700` cents.
     - `((2700 * 90) + 50) / 100 = 243050 / 100 = 2430` cents (`$24.30`), matching the brief's exact example.
     - Prep units: `1 + 2 = 3` units.
   - **Combo 2 (`Burger + Salad`):**
     - Combined price: `1500 + 900 = 2400` cents.
     - `((2400 * 90) + 50) / 100 = 2160` cents (`$21.60`).
     - Prep units: `1 + 1 = 2` units.

2. **How Existing Customer Subclasses React Automatically (OCP & LSP):**
   - `CriticCustomer.chooseMenuItem(menu)` scans `menu` for the highest `getPriceCents()`. Once `Salad + Pasta` (`2430` cents) is added to `menu`, the Critic automatically picks `Salad + Pasta` (`$24.30`, 3 prep units) instead of `Pasta` (`$18.00`, 2 prep units) without changing a single line of `CriticCustomer`!
   - With 5 items on the menu (`0: Salad`, `1: Burger`, `2: Pasta`, `3: Salad + Pasta`, `4: Burger + Salad`), `Customer.chooseMenuItem(menu)` uses `(customerId - 1) % menu.size()`, so Customer 5 (`(5 - 1) % 5 = 4`) automatically orders `Burger + Salad` (`$21.60`, 2 prep units).
   - `Order` reads `item.getPrepUnits()` and `item.getPriceCents()` polymorphically through `MenuItem`, so `Chef` cooks 3 units for `Salad + Pasta` and 2 units for `Burger + Salad` with no special-case code.

3. **One Edge Case to Check:**
   - If `first` or `second` is `null`, calling `first.getName()` inside `super(...)` will throw a `NullPointerException` (which is standard in Java before `super(...)` runs). In `Menu.addItem(MenuItem item)`, add `if (item == null) throw new IllegalArgumentException("Menu item cannot be null");` so `Menu` never stores a `null` entry.

---

## 2. Designing the Stage 3 Sequence Diagram Following C4 (12–14 Nov 2026)

### Me

> For our final submission (`docs/uml/stage3/sequence-diagram.puml`), I want the sequence diagram to cover a Stage 3 scenario that hits all three new Stage 3 features (`Host`, `ComboMeal`, and `HappyHour`) plus every item on the sequence diagram rubric:
> - Customer arrival and order
> - Waiter and Chef assignments (including a manager choice on the Chef that changes the outcome)
> - `READY` then `SERVED`, eating, and Cashier payment
> - The Turn 18 result check (`Objective.isMet(stats)`)
> - Relevant guards, `loop` fragments, and `alt` fragments
>
> I traced Customer C4 (the `CriticCustomer` arriving on Turn 10 in `tests/stage3_victory.txt`):
> - At start-up: `Main` creates `ComboMeal(Salad, Pasta)` (`$24.30`, 3 units) and calls `menu.addItem(sp)`.
> - Turn 10: C4 arrives (sat 100). Manager activates `HappyHour` (`alt` branch: activate Happy Hour vs. decline). `Host` tries to seat C4 at Table 1 first (rejected by `Table 1 is occupied` inside the `loop [until valid Host choice]` retry loop), then seats C4 at free Table 2. Waiter takes C4's order during Happy Hour: `CriticCustomer.chooseMenuItem(menu)` picks `Salad + Pasta` (`2430` cents), `c4.calculateBill(2430)` returns `2430`, and `happyHour.applyTo(2430)` locks the bill at `1944` cents (`$19.44`). Chef does Unit 1 of 3 (`PLACED -> COOKING`). End of Turn 10: C4 loses 12 sat (`100 -> 88`).
> - Turn 11: Happy Hour is still active (1 turn left), so C4 recovers `+15` capped at `100` (`88 -> 100`). Chef does Unit 2 of 3; end of Turn 11 sat `100 -> 88`.
> - Turn 12: Manager chooses Chef to cook Order 4 (Unit 3 of 3 -> `READY`, `readyTurn = 12`) instead of newly placed Order 5 (`alt` branch showing that delaying Order 4 would drop C4's paid satisfaction by 12 points). End of Turn 12 sat `88 -> 76`.
> - Turn 13: Waiter serves Order 4 (`SERVED`, sat locked at `76`).
> - Turn 14: C4 finishes eating (`READY_TO_PAY`).
> - Turn 15: Cashier collects `$19.44` (`PAID`, Table 2 released, revenue `$62.10 -> $81.54`, average satisfaction `84.6`).
> - After Turn 18: `Objective.isMet(stats)` returns `true` (`5 >= 4 paid`, `8154 >= 4500 cents`, `84.6 >= 60.0` -> `VICTORY`).
>
> Can you check my `alt` fragment comparing "Manager activates Happy Hour on Turn 10" vs "Manager declines Happy Hour" to make sure the numbers on the declined branch are exact?

### Claude

Let's trace C4's exact satisfaction and bill numbers on both branches of your Turn 10 `alt` fragment:

1. **Branch 1 — Manager activates Happy Hour on Turn 10 (`askStartHappyHour() == true`, as in `tests/stage3_victory.txt`):**
   - **Turn 10 start:** `happyHour.activate()` sets `turnsRemaining = 2`. C4 just arrived at `100`, so `c4.recoverSatisfaction(15)` returns `false` (already at 100).
   - **Turn 10 order:** `currentPricing()` returns `happyHour`. Bill locked at `happyHour.applyTo(2430) = (2430 * 80 + 50) / 100 = 1944` cents (`$19.44`).
   - **Turn 10 end:** C4 is `WAITING_FOR_FOOD`, loses 12 -> `88`. `happyHour.endTurn()` decrements `turnsRemaining` to `1`.
   - **Turn 11 start:** `happyHour.isActive()` is `true`. `c4.recoverSatisfaction(15)` raises C4 from `88` to `Math.min(100, 88 + 15) = 100` (a net gain of +12 points).
   - **Turn 11 end:** C4 loses 12 -> `88`. `happyHour.endTurn()` decrements `turnsRemaining` to `0`.
   - **Turn 12 end:** Order 4 reaches `READY`, C4 loses 12 -> `76`.
   - **Turn 13–15:** Served on Turn 13 at satisfaction **`76`**, pays **`$19.44`** on Turn 15.

2. **Branch 2 — Manager declines Happy Hour (`askStartHappyHour() == false`):**
   - **Turn 10 order:** `currentPricing()` returns `standardPricing`. Bill locked at `2430` cents (**`$24.30`**).
   - **Turn 10 end:** C4 loses 12 -> `88`.
   - **Turn 11 start:** No Happy Hour recovery (`88` stays `88`).
   - **Turn 11 end:** C4 loses 12 -> `76`.
   - **Turn 12 end:** C4 loses 12 -> `64`.
   - **Turn 13–15:** Served on Turn 13 at satisfaction **`64`** (12 points lower), pays **`$24.30`** on Turn 15.

Including both the Turn 10 Happy Hour `alt` (`$19.44` at sat `76` vs. `$24.30` at sat `64`) and the Turn 12 Chef priority `alt` (finishing Order 4 on Turn 12 so C4 is served at `76` vs. cooking Order 5 first which drops C4 to `64`) gives your sequence diagram two concrete, verified examples of manager decisions changing the game outcome.

---

## Verification & Key Ideas Formulated (Appendix I)

- **Key Ideas Formulated by Me:**
  1. Designing `ComboMeal` as a subclass of `MenuItem` that calculates its combined name, 90% rounded price (`((p1 + p2) * 90 + 50) / 100`), and summed prep units in `super(...)`, enabling `Order`, `Chef`, and `CriticCustomer` to support combo meals with zero code changes.
  2. Designing the Stage 3 sequence diagram around Customer C4 (Critic) across Turns 10–15 + Turn 18 so a single scenario demonstrates `ComboMeal` registration, `HappyHour` pricing & satisfaction recovery (`PricingPolicy` DIP), `Host` seating validation retry loops, Chef prioritization, and `Objective.isMet(stats)`.
- **How I Verified AI Suggestions:**
  - Compiled and ran `tests/stage3_victory.txt` (`5 paid, $81.54, 84.6 avg satisfaction, VICTORY`) and also ran a modified script with Happy Hour declined on Turn 10 to verify empirically that C4 pays `$24.30` at satisfaction `64` instead of `$19.44` at satisfaction `76`.
  - Rendered `docs/uml/stage3/sequence-diagram.png` with PlantUML and verified all lifelines, activation bars, and method signatures against the final Stage 3 codebase.
