# SC2002 Restaurant Rush – Stage 3 AI History

- **Repository:** `SC2002-sce2`
- **Contributor:** Raghav (`Raghavsu001`)
- **Stage:** 3 (Stage 3 requirements just arrived: `Host`, `ComboMeal`, `HappyHour`)
- **Tool:** ChatGPT
- **Dates:** 6 Nov 2026 – 14 Nov 2026 (SGT)
- **Overview:** Documents my full conversations with ChatGPT on understanding the Stage 3 architecture (`Host`, `ComboMeal`, `PricingPolicy` / `HappyHour`), validating my Stage 3 PlantUML class diagram (`docs/uml/stage3/class-diagram.puml`) against the Java source code, and writing the Stage 2 -> Stage 3 change analysis in `README.md`.

---

## Chat 01: Understanding Stage 3 Extensions & Dependency Inversion (6 Nov 2026)

### Me (6 Nov)

> We are now working on Stage 3 of our restaurant project, which adds `Host`, `ComboMeal`, and `HappyHour` (`PricingPolicy` and `StandardPricing`). I am responsible for the final Stage 3 UML class diagram (`docs/uml/stage3/class-diagram.puml`, worth 25 marks) and the `README.md` Stage 2 -> Stage 3 change analysis.
>
> Before I update the class diagram, can you explain why we introduced the `PricingPolicy` interface (`applyTo(int billCents)`) implemented by `StandardPricing` and `HappyHour` instead of having `Waiter.takeOrder` directly call `HappyHour`?

### ChatGPT

Introducing the `PricingPolicy` interface between `Waiter` and `HappyHour` is a direct application of the **Dependency Inversion Principle (DIP)** and the **Strategy Pattern**:

1. **Without `PricingPolicy` (Direct Coupling):**
   - If `Waiter.takeOrder` accepted a concrete `HappyHour` object (or checked `if (happyHour != null && happyHour.isActive())`), the `Waiter` entity class would be tightly coupled to one specific promotional campaign and its activation state.
   - Any future pricing rule (such as a lunch special or weekend surcharge) would require modifying `Waiter.takeOrder` again.

2. **With `PricingPolicy` (Dependency Inversion):**
   - `PricingPolicy` defines a single abstraction: `int applyTo(int billCents)`.
   - Both `StandardPricing` (no discount) and `HappyHour` (20% discount when active) implement `PricingPolicy`.
   - `GameController.currentPricing()` selects the active `PricingPolicy` strategy for the turn and passes it into `Waiter.takeOrder(customer, menu, pricing)`.
   - `Waiter` depends only on the `PricingPolicy` interface (`pricing.applyTo(customer.calculateBill(item.getPriceCents()))`) and never knows which concrete pricing rule is active.

In your Stage 3 UML class diagram, you should show:
- `PricingPolicy <<interface>>` with `+ applyTo(billCents : int) : int`
- `StandardPricing` and `HappyHour` connected to `PricingPolicy` with dashed realization arrows (`..|>`)
- `GameController` owning `happyHour : HappyHour` and `standardPricing : PricingPolicy` via composition (`*--`)
- `Waiter` having a dashed dependency arrow (`..>`) to `PricingPolicy`.

---

## Chat 02: Validating the Stage 3 PlantUML Class Diagram Against the Code (9–11 Nov 2026)

### Me (11 Nov)

> I have drafted `docs/uml/stage3/class-diagram.puml` and rendered the PNG. Here is my `GameController` class block from my PlantUML file:
>
> ```plantuml
> class GameController <<control>> #FFF2C2 {
>     - currentTurn : int
>     + GameController(restaurant : Restaurant, ui : ConsoleUI, schedule : ArrivalSchedule, objective : Objective)
>     + run() : void
>     - playTurn() : void
>     - handleArrivals() : void
>     - handleHappyHour() : void
>     - handleHost() : void
>     - handleWaiter() : void
>     - handleChef() : void
>     - handleEatingAndPayment() : void
>     - handleEndOfTurnWaiting() : void
>     - currentPricing() : PricingPolicy
>     - getCustomerOrThrow(id : int) : Customer
>     - getOrderOrThrow(id : int) : Order
> }
> ```
>
> When I compare this against `src/control/GameController.java`, is anything missing in the attributes list?

### ChatGPT

Comparing your PlantUML block against `src/control/GameController.java`, two `private final` fields added in Stage 3 are missing from the attribute compartment of `GameController`:

- `- happyHour : HappyHour`
- `- standardPricing : PricingPolicy`

Even though you already drew the composition arrows (`GameController *-- "1" HappyHour` and `GameController *-- "1" PricingPolicy`), listing all key fields inside the class box (`- happyHour : HappyHour` and `- standardPricing : PricingPolicy` alongside `- restaurant : Restaurant`, `- ui : ConsoleUI`, `- schedule : ArrivalSchedule`, `- objective : Objective`, `- currentTurn : int`) ensures the attribute compartment matches the Java source code 1-to-1:

```plantuml
class GameController <<control>> #FFF2C2 {
    - restaurant : Restaurant
    - ui : ConsoleUI
    - schedule : ArrivalSchedule
    - objective : Objective
    - happyHour : HappyHour
    - standardPricing : PricingPolicy
    - currentTurn : int

    + GameController(restaurant : Restaurant, ui : ConsoleUI, schedule : ArrivalSchedule, objective : Objective)
    + run() : void
    - playTurn() : void
    - handleArrivals() : void
    - handleHappyHour() : void
    - handleHost() : void
    - handleWaiter() : void
    - handleChef() : void
    - handleEatingAndPayment() : void
    - handleEndOfTurnWaiting() : void
    - currentPricing() : PricingPolicy
    - getCustomerOrThrow(id : int) : Customer
    - getOrderOrThrow(id : int) : Order
}
```

### My Reflection on This Check

This was a small correction, but it made me check every class compartment in `docs/uml/stage3/class-diagram.puml` against the `.java` files in `src/`. A UML diagram is not just a high-level sketch — its attributes, visibility markers (`-`, `#`, `+`), method parameter types, and return types must match the actual implementation.

---

## Chat 03: Structuring the Stage 2 -> Stage 3 Change Analysis in `README.md` (9–14 Nov 2026)

### Me (9 Nov)

> I am writing the Stage 2 -> Stage 3 change analysis in `README.md` (`85739cf` and `c93090c`). Briefing Slide 4 says to include:
> - What did you add? (New classes and their responsibilities)
> - What did you modify? (Existing classes changed, with reasons)
> - What still works? (Checks that earlier behavior remains correct, plus explaining the effect on the design — line count alone is insufficient)
>
> In Stage 3, we added 5 new classes (`Host`, `ComboMeal`, `PricingPolicy`, `StandardPricing`, `HappyHour`), modified 7 existing classes (`Customer`, `Menu`, `Waiter`, `Restaurant`, `GameController`, `Main`, `ConsoleUI`), and left 13 existing classes completely untouched (`RegularCustomer`, `VIPCustomer`, `CriticCustomer`, `CustomerStatus`, `Staff`, `Chef`, `Cashier`, `Table`, `Order`, `OrderStatus`, `GameStats`, `Money`, `InvalidActionException`, `ArrivalSchedule`, `CustomerType`, `Objective`, `GameConfig`).
>
> How should I explain *why* those 13 classes stayed unchanged so we get full marks for design evolution?

### ChatGPT

To explain the design impact beyond raw file counts, group the **unchanged classes** by the OOP design decision that shielded them from modification:

1. **Why `Order`, `Chef`, `RegularCustomer`, `VIPCustomer`, and `CriticCustomer` did not change when `ComboMeal` was added (OCP & LSP):**
   - Because `ComboMeal` extends `MenuItem` and computes its 90% price and summed preparation units in `super(...)`, every class that consumes `MenuItem` treats a combo identically to a single dish. In particular, `CriticCustomer.chooseMenuItem(Menu)` automatically selects `Salad + Pasta` (`$24.30`) as the highest-priced item without changing a single line of `CriticCustomer`.
2. **Why `Staff`, `Chef`, `Cashier`, and `Table` did not change when `Host` was added (Inheritance & Composition):**
   - `Host` extends `Staff` to reuse `checkCanAct()` / `markActed()`, and `Restaurant` adds `host` to `getAllStaff()` so `playTurn()` resets the Host alongside the existing staff.
3. **Why `Order`, `Cashier`, and `GameStats` did not change when `HappyHour` was added (DIP & Information Hiding):**
   - Because `Waiter.takeOrder` applies `PricingPolicy.applyTo(...)` at the moment the order is placed and locks the resulting integer cents into `Order.billCents`, `Order`, `Cashier`, and `GameStats` never need to know whether an order was placed during Happy Hour.

---

## Verification & Key Ideas Formulated (Appendix I)

- **Key Ideas Formulated by Me:**
  1. Colour-coding `docs/uml/stage3/class-diagram.puml` (green `#D9F2D9` for the 5 new Stage 3 classes, yellow `#FFF2C2` for the 7 modified classes, default for the unchanged classes) and annotating encapsulation, polymorphism, and composition directly on the diagram.
  2. Auditing every class compartment in `docs/uml/stage3/class-diagram.puml` against the `src/` Java files (`f452149`) and documenting the 5 added / 7 modified / 13+ unchanged class breakdown in `README.md` (`85739cf`, `c93090c`).
- **How I Verified AI Suggestions:**
  - Cross-checked all fields, constructors, methods, and visibility modifiers in `docs/uml/stage3/class-diagram.puml` against `src/entity/*.java`, `src/control/*.java`, and `src/boundary/*.java`, and verified all compare links and regression tables in `README.md`.
