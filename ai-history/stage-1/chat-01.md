# AI Interaction History: SC2002 Restaurant Rush – Stage 1

- **Repository:** `SC2002-sce2`
- **Contributor:** Raghav (`Seshadri Raghav Subramanian`)
- **Stage:** 1 (Only Stage 1 baseline requirements known)
- **Tools Used:** Microsoft Copilot
- **Dates:** 22 Oct 2026 – 24 Oct 2026 (SGT)
- **Overview:** Documents my full conversation with Microsoft Copilot on customer lifecycle state transitions, satisfaction encapsulation, `RegularCustomer` inheritance, and designing extension hooks in `Customer`.

---

## 1. Understanding `Customer` Responsibilities Under SRP (22 Oct 2026)

### Me (22 Oct)

> I'm implementing `Customer.java`, `CustomerStatus.java`, and `RegularCustomer.java` for Stage 1 of our SC2002 Restaurant Rush project. Looking at the specification, I want to make sure `Customer` doesn't become a God Class. What responsibilities belong inside `Customer` and what should be handled by `Waiter`, `Cashier`, and `GameController`?

### Copilot

To follow the **Single Responsibility Principle (SRP)** and keep `Customer` cohesive, `Customer` should only own **its own identity, lifecycle status, satisfaction score, and customer-specific rules**, while restaurant operations stay in the staff and controller classes:

1. **What belongs inside `Customer`:**
   - **Identity & timing:** `customerId`, `arrivalTurn`, `servedTurn`, and `readyToPayTurn`.
   - **Satisfaction state:** starting at `100`, decaying at the end of each waiting turn (clamped to a minimum of `0`), and transitioning to `LEFT` if satisfaction hits `0`.
   - **Lifecycle state machine (`CustomerStatus`):** validating and executing transitions across `WAITING -> SEATED -> WAITING_FOR_FOOD -> SERVED -> READY_TO_PAY -> PAID` (or `LEFT`).
   - **Customer-level policy hooks:** choosing a dish from the `Menu` (`chooseMenuItem(Menu)`) and calculating the customer's bill from the item's base price (`calculateBill(int)`).

2. **What belongs outside `Customer`:**
   - **Table assignment:** `Waiter` and `Table` manage which table is occupied or free.
   - **Order creation and cooking:** `Waiter` creates the `Order`, and `Chef` advances preparation units.
   - **Payment priority and revenue recording:** `Cashier` selects the oldest `READY_TO_PAY` customer, and `GameStats` records revenue and paid satisfaction scores.

That way, `Customer` only changes if customer rules change, not when kitchen or table rules change.

---

## 2. Implementing the Customer Lifecycle State Machine (22 Oct 2026)

### Me (22 Oct)

> The specification has states `WAITING`, `SEATED`, `WAITING_FOR_FOOD`, `SERVED`, `READY_TO_PAY`, `PAID`, and `LEFT`. How should I structure the transition methods in `Customer` so other classes can't put a customer into an invalid state?

### Copilot

Instead of providing a public `setStatus(CustomerStatus status)` setter — which would allow any caller to jump directly from `WAITING` to `PAID` — keep the `status` field `private` and provide dedicated transition methods that guard the current state:

```text
WAITING -> seat() -> SEATED -> orderTaken() -> WAITING_FOR_FOOD -> serve(turn) -> SERVED -> finishEating(turn) -> READY_TO_PAY -> markPaid() -> PAID
```

For each transition method:
1. Check that `this.status` is in the expected prior state (for example, `seat()` requires `status == CustomerStatus.WAITING`, and `orderTaken()` requires `status == CustomerStatus.SEATED`).
2. If the state is invalid, throw an `IllegalStateException` with a clear message showing the current status.
3. Update `this.status` (and record `servedTurn = currentTurn` in `serve(int currentTurn)` and `readyToPayTurn = currentTurn` in `finishEating(int currentTurn)`).

Encapsulating transitions this way guarantees that no external class can bypass a step in the customer lifecycle.

---

## 3. Encapsulating Satisfaction Decay (23 Oct 2026)

### Me (23 Oct)

> At the end of each turn, every waiting or seated customer who has not been served yet loses satisfaction (8 points for Regular, minimum 0), and if they hit 0 they leave immediately. Here is the method I wrote in `Customer.java`:
>
> ```java
> public abstract int getSatisfactionDecay();
>
> public boolean decreaseSatisfaction() {
>     if (status == CustomerStatus.WAITING
>             || status == CustomerStatus.SEATED
>             || status == CustomerStatus.WAITING_FOR_FOOD) {
>         satisfaction = Math.max(MIN_SATISFACTION, satisfaction - getSatisfactionDecay());
>         if (satisfaction == MIN_SATISFACTION) {
>             status = CustomerStatus.LEFT;
>             return true;
>         }
>     }
>     return false;
> }
> ```
>
> Does returning `boolean` (`true` when the customer transitions to `LEFT`) work cleanly for `GameController`?

### Copilot

Yes — returning `true` when `satisfaction` reaches `0` (`MIN_SATISFACTION`) and `status` transitions to `CustomerStatus.LEFT` is very clean:
- **Encapsulation:** No external class has a `setSatisfaction(...)` setter, and `GameController` does not need to know the numeric decay rate (`getSatisfactionDecay()`) or check `if (c.getSatisfaction() <= 0)` itself.
- **Correct Status Scope:** Only `WAITING`, `SEATED`, and `WAITING_FOR_FOOD` lose satisfaction. Once `serve(turn)` sets `status = CustomerStatus.SERVED` (or `READY_TO_PAY` / `PAID`), `decreaseSatisfaction()` does nothing and returns `false`, matching Section 3.2 Step 5 of the brief (*"Customers who are served, eating or ready to pay lose nothing"*).
- **Actionable Return Value:** When `decreaseSatisfaction()` returns `true`, `GameController` immediately knows that this customer abandoned the restaurant on this turn so it can cancel any unfinished order, release their table, and increment the unhappy departure count.

---

## 4. Designing General Extension Hooks in `Customer` (24 Oct 2026)

### Me (24 Oct)

> My teammates and I want `Customer` in Stage 1 to follow the Open/Closed Principle so that if later stages introduce new customer subclasses with different ordering preferences or bill discounts, `Waiter` and `Cashier` won't need to be modified. I added these two methods to `Customer.java`:
>
> ```java
> public MenuItem chooseMenuItem(Menu menu) {
>     if (menu == null || menu.size() == 0) {
>         throw new IllegalArgumentException("Menu cannot be null or empty");
>     }
>     int index = (customerId - 1) % menu.size();
>     return menu.getItem(index);
> }
>
> public int calculateBill(int rawPriceCents) {
>     if (rawPriceCents < 0) {
>         throw new IllegalArgumentException("Price cannot be negative");
>     }
>     return rawPriceCents;
> }
> ```
>
> And here is my `RegularCustomer.java`:
>
> ```java
> package entity;
>
> public class RegularCustomer extends Customer {
>     private static final int DECAY_PER_TURN = 8;
>
>     public RegularCustomer(int customerId, int arrivalTurn) {
>         super(customerId, arrivalTurn);
>     }
>
>     @Override
>     public int getSatisfactionDecay() {
>         return DECAY_PER_TURN;
>     }
> }
> ```
>
> Is keeping default implementations of `chooseMenuItem` and `calculateBill` in `Customer` while making `getSatisfactionDecay()` abstract a good balance for Stage 1?

### Copilot

Yes — that is a textbook application of **Template Method** and the **Open/Closed Principle (OCP)**:
1. **`getSatisfactionDecay()` as `abstract`:** Every customer type must define its per-turn waiting decay rate (`8` in `RegularCustomer`), so making it `abstract` forces every subclass to state its rate explicitly.
2. **Default hooks for `chooseMenuItem(Menu)` and `calculateBill(int)`:**
   - Using `(customerId - 1) % menu.size()` gives a deterministic, reproducible dish choice across all 6 Stage 1 arrivals (C1 Salad, C2 Burger, C3 Pasta, C4 Salad, C5 Burger, C6 Pasta), satisfying Section 2.2 (*"Use a documented deterministic rule"*).
   - Defaulting `calculateBill(int rawPriceCents)` to return `rawPriceCents` means `RegularCustomer` stays minimal (only overriding `getSatisfactionDecay()`), while any future subclass that needs custom menu selection or discounted billing can override just the relevant hook without changing `Waiter` or `GameController`.

---

## Verification & Key Ideas Formulated (Appendix I)

- **Key Ideas Formulated by Me:**
  1. Encapsulating the customer state machine inside `Customer` via guarded transition methods (`seat()`, `orderTaken()`, `serve(turn)`, `finishEating(turn)`, `markPaid()`) with no public `setStatus` or `setSatisfaction` setters.
  2. Designing `decreaseSatisfaction()` to clamp satisfaction at `0`, transition automatically to `CustomerStatus.LEFT`, and return `true` when abandonment occurs.
  3. Providing deterministic `(customerId - 1) % menu.size()` dish selection in `chooseMenuItem(Menu)` and `calculateBill(int)` as overridable hooks on `Customer`.
- **How I Verified AI Suggestions:**
  - Compiled `Customer.java`, `CustomerStatus.java`, and `RegularCustomer.java` (`48a4893`, `43efdf9`) and verified that a `RegularCustomer` arriving on Turn 1 and ordering Pasta reproduces the exact satisfaction progression in Section 3.3 of the brief (`100 -> 92 -> 84 -> 76 -> SERVED -> READY_TO_PAY -> PAID $18.00`).
