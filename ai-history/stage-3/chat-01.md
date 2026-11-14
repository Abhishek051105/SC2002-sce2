# Chat 01: Stage 3 `PricingPolicy`, `HappyHour` & Dependency Inversion

- **Repository:** `SC2002-sce2`
- **Contributor:** Aekas
- **Stage:** 3 (Stage 3 requirements just arrived: `Host`, `ComboMeal`, `HappyHour`)
- **Tool:** Claude (claude.ai)
- **Dates:** 6 Nov 2026 – 14 Nov 2026 (SGT)

---

## 1. Designing `PricingPolicy`, `StandardPricing`, and `HappyHour` (6 Nov 2026)

### Me

> For Stage 3, I am responsible for the Happy Hour feature (`PricingPolicy`, `StandardPricing`, `HappyHour`, `Customer.recoverSatisfaction`, and updating `Waiter.takeOrder`) plus updating `docs/key-abstractions-and-solid.md` and `docs/design-log.md`.
>
> Instead of passing a `HappyHour` object directly into `Waiter.takeOrder` and making `Waiter` check `if (happyHour.isActive())`, I designed a `PricingPolicy` interface:
>
> ```java
> package entity;
>
> public interface PricingPolicy {
>     int applyTo(int billCents);
> }
> ```
>
> With two implementations:
> - `StandardPricing`: `applyTo(int billCents)` returns `billCents` unchanged.
> - `HappyHour`: implements `PricingPolicy` (taking 20% off when active: `((billCents * 80) + 50) / 100`) and manages `used` (`boolean`) and `turnsRemaining` (`int`, set to `2` on `activate()`, decremented by `endTurn()`).
>
> And in `Waiter.takeOrder(Customer customer, Menu menu, PricingPolicy pricing)`:
> ```java
> MenuItem item = customer.chooseMenuItem(menu);
> int billCents = pricing.applyTo(customer.calculateBill(item.getPriceCents()));
> Order order = new Order(customer.getCustomerId(), customer, item, billCents);
> ```
>
> Can you verify whether `pricing.applyTo(customer.calculateBill(item.getPriceCents()))` reproduces the brief's VIP Burger example (`$15.00 x 0.9 x 0.8 = $10.80`) and whether keeping a temporary 2-argument `takeOrder(Customer, Menu)` overload that delegates to `new StandardPricing()` is a good way to merge my PR before Aaryan's `GameController` PR is ready?

### Claude

Both parts of your design are spot-on:

1. **Verifying Stacked Discount Rounding (`VIP` + `HappyHour`):**
   - For a VIP ordering a Burger (`1500` cents) during Happy Hour:
     1. `customer.calculateBill(1500)` runs first in `VIPCustomer`: `((1500 * 90) + 50) / 100 = 1350` cents (`$13.50`).
     2. `pricing.applyTo(1350)` runs next in `HappyHour`: `((1350 * 80) + 50) / 100 = 108050 / 100 = 1080` cents (`$10.80`).
   - Because `1080` cents is locked into `new Order(..., 1080)` at the moment `takeOrder` runs, orders placed before Happy Hour keep their old price and orders placed on the second turn of Happy Hour keep their 20% discount even when paid after Happy Hour expires.

2. **Why `PricingPolicy` Is Genuine Dependency Inversion (DIP):**
   - Section 4.4 of the brief warns: *"Do not create interfaces just to satisfy a checklist."*
   - Here, `PricingPolicy` has **two real implementations** (`StandardPricing` and `HappyHour`) used at runtime. `Waiter.takeOrder` depends only on the `PricingPolicy` abstraction and never knows whether Happy Hour is active or how promotional discounts work.

3. **Temporary 2-Argument `takeOrder(Customer, Menu)` Overload:**
   - Keeping `public Order takeOrder(Customer customer, Menu menu) throws InvalidActionException { return takeOrder(customer, menu, new StandardPricing()); }` while merging `feat/happy-hour` into `main` allows `main` to compile and pass all 6 Stage 1 and Stage 2 test scripts immediately. Once Aaryan updates `GameController` to pass `currentPricing()`, he can remove the 2-argument overload so `Waiter` has zero dependency on `StandardPricing`.

---

## 2. Designing `Customer.recoverSatisfaction(int points)` (6 Nov 2026)

### Me

> For the satisfaction recovery during Happy Hour ("customers still waiting for a table or food recover 15 satisfaction at turn start, maximum 100"), I wrote this `final` method in `Customer.java`:
>
> ```java
> public final boolean recoverSatisfaction(int points) {
>     if (points < 0) {
>         throw new IllegalArgumentException("Recovery points cannot be negative");
>     }
>     if (status != CustomerStatus.WAITING && status != CustomerStatus.SEATED && status != CustomerStatus.WAITING_FOR_FOOD) {
>         return false;
>     }
>     int before = this.satisfaction;
>     this.satisfaction = Math.min(MAX_SATISFACTION, this.satisfaction + points);
>     return this.satisfaction > before;
> }
> ```
>
> I made it return `boolean` (`this.satisfaction > before`) so that when `GameController` loops over customers at the start of a Happy Hour turn, a customer who just arrived on that turn with `100` satisfaction won't print `"C4 recovers to 100."` when their satisfaction didn't actually change. Does this match the brief's rules?

### Claude

Returning `this.satisfaction > before` is a very thoughtful design detail:
- The eligibility check (`WAITING`, `SEATED`, `WAITING_FOR_FOOD`) matches the exact three unserved statuses in `decreaseSatisfaction()`, ensuring customers who are already `SERVED`, `READY_TO_PAY`, `PAID`, or `LEFT` are never affected.
- Returning `true` only when `satisfaction` strictly increased prevents misleading `"recovers to 100"` console messages for a customer who just arrived at `100` on the same turn Happy Hour is activated (such as C4 arriving on Turn 10).
- Marking `recoverSatisfaction` as `final` keeps it consistent with the rest of `Customer`'s template-method lifecycle operations.

---

## Verification & Key Ideas Formulated (Appendix I)

- **Key Ideas Formulated by Me:**
  1. Introducing the `PricingPolicy` interface (`StandardPricing` and `HappyHour`) so `Waiter.takeOrder` applies promotional pricing via Dependency Inversion without checking `HappyHour` state.
  2. Having `Customer.recoverSatisfaction(int)` return `boolean` (`this.satisfaction > before`) so customers already at `100` satisfaction do not trigger redundant recovery messages.
  3. Keeping a temporary 2-argument `Waiter.takeOrder` overload during PR #6 so `main` remained compilable before `GameController` was wired up.
- **How I Verified AI Suggestions:**
  - Compiled `main` after merging `feat/happy-hour` (`8852873`) and verified that all 6 Stage 1 and Stage 2 test scripts produced identical outputs (`79.2 / 58.0 / 79.2` and `75.6 / 53.8 / 75.6`).
  - Verified the line ranges of `ComboMeal.java`, `Customer.java`, `PricingPolicy.java`, `HappyHour.java`, and `Waiter.java` for the demo video presentation.
