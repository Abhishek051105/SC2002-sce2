# AI Interaction History: SC2002 Restaurant Simulation – Stage 2

- **Repository:** `SC2002-sce2`
- **Contributor:** Raghav (`Raghavsu001`)
- **Stage:** 2 (Stage 2 requirements just arrived; Stage 3 unknown)
- **Tools Used:** Microsoft Copilot
- **Date:** 31 Oct 2026 (SGT)
- **Overview:** Documents my full conversation with Microsoft Copilot on adding `getTypeLabel()` to `Customer` and implementing `VIPCustomer` for Stage 2 in compliance with the Open/Closed Principle.

---

## 1. Adding Polymorphic Type Identification to `Customer` (31 Oct 2026)

### Me (31 Oct)

> We have completed Stage 1 (`stage-1` tag) and are now starting Stage 2, which adds `VIPCustomer` (loses 5 satisfaction per waiting turn, 10% off the bill) and `CriticCustomer` (loses 12 satisfaction per waiting turn, full price, custom order preference).
>
> My Stage 2 code tasks are:
> 1. Adding a clean way for `ConsoleUI` to display whether a customer is `"Regular"`, `"VIP"`, or `"Critic"` without using `instanceof` checks.
> 2. Implementing `VIPCustomer.java`.
>
> I plan to add `public abstract String getTypeLabel();` to `Customer.java` and return `"Regular"` in `RegularCustomer.java`. Why is an abstract `getTypeLabel()` method better than checking `if (c instanceof VIPCustomer)` in `ConsoleUI`?

### Copilot

Adding `public abstract String getTypeLabel();` to `Customer` is directly aligned with the **Open/Closed Principle (OCP)** and **Polymorphism**:

1. **Avoids Type-Check Chains in `ConsoleUI`:**
   - If `ConsoleUI` used `if (c instanceof VIPCustomer) ... else if (c instanceof CriticCustomer) ...`, then `ConsoleUI` would be tightly coupled to every concrete subclass of `Customer`. Adding a fourth customer type in the future would force you to hunt down and edit every `if/else` chain in the UI.
2. **Compiler-Enforced Completeness:**
   - Making `getTypeLabel()` `abstract` on `Customer` means the Java compiler forces every concrete `Customer` subclass (`RegularCustomer`, `VIPCustomer`, `CriticCustomer`) to provide its display label. `ConsoleUI` simply calls `c.getTypeLabel()` on any `Customer` reference.

---

## 2. Implementing `VIPCustomer` and Integer-Cent Discount Rounding (31 Oct 2026)

### Me (31 Oct)

> Here is my implementation of `VIPCustomer.java`:
>
> ```java
> package entity;
>
> public class VIPCustomer extends Customer {
>     private static final int DECAY_PER_TURN = 5;
>
>     public VIPCustomer(int customerId, int arrivalTurn) {
>         super(customerId, arrivalTurn);
>     }
>
>     @Override
>     public int getSatisfactionDecay() {
>         return DECAY_PER_TURN;
>     }
>
>     @Override
>     public int calculateBill(int rawPriceCents) {
>         int base = super.calculateBill(rawPriceCents);
>         return (base * 90 + 50) / 100;
>     }
>
>     @Override
>     public String getTypeLabel() {
>         return "VIP";
>     }
> }
> ```
>
> Can you check my `calculateBill` method? Why is calling `super.calculateBill(rawPriceCents)` and using `(base * 90 + 50) / 100` better than `(int) Math.round(rawPriceCents * 0.9)`?

### Copilot

Your `VIPCustomer` implementation is clean and avoids floating-point pitfalls:

1. **Calling `super.calculateBill(rawPriceCents)` (LSP Precondition Preservation):**
   - `Customer.calculateBill(int rawPriceCents)` already validates `if (rawPriceCents < 0) throw new IllegalArgumentException("Price cannot be negative");`. Calling `super.calculateBill(rawPriceCents)` reuses that precondition check so `VIPCustomer` never duplicates validation logic and preserves the superclass contract under LSP.
2. **Exact Integer-Cent Rounding `(base * 90 + 50) / 100`:**
   - Using integer arithmetic `(base * 90 + 50) / 100` computes exact half-up rounding to the nearest cent without converting to `double`.
   - For the Stage 1 menu items:
     - `Salad` (`900` cents): `(900 * 90 + 50) / 100 = 810` cents (`$8.10`)
     - `Burger` (`1500` cents): `(1500 * 90 + 50) / 100 = 1350` cents (`$13.50`)
     - `Pasta` (`1800` cents): `(1800 * 90 + 50) / 100 = 1620` cents (`$16.20`)
3. **Zero Impact on Other Classes:**
   - Because `VIPCustomer` follows the same ordering rule and lifecycle as `Customer`, it inherits `chooseMenuItem(Menu)` and all `final` state-transition methods unchanged.

---

## Verification & Key Ideas Formulated (Appendix I)

- **Key Ideas Formulated by Me:**
  1. Adding `abstract String getTypeLabel()` to `Customer` (`5e78441`, `8256a11`) so `ConsoleUI` displays `"Regular"`, `"VIP"`, and `"Critic"` polymorphically without `instanceof`.
  2. Implementing `VIPCustomer.calculateBill(int)` (`b8c00e8`) by delegating precondition validation to `super.calculateBill(rawPriceCents)` and applying integer-cent half-up rounding `(base * 90 + 50) / 100`.
- **How I Verified AI Suggestions:**
  - Verified that `VIPCustomer` calculates `$8.10` (`810`), `$13.50` (`1350`), and `$16.20` (`1620`) for the three menu items and loses `5` satisfaction per waiting turn (`100 -> 95 -> 90` in `tests/stage2_victory.txt`).
