# Chat 01: Stage 1 Staff Hierarchy, Table, GameStats & Verification

- **Repository:** `SC2002-sce2`
- **Contributor:** Aekas
- **Stage:** 1 (Only Stage 1 baseline requirements known)
- **Tool:** Claude (claude.ai)
- **Dates:** 28 Oct 2026 – 29 Oct 2026 (SGT)

---

## 1. Designing `Staff`, `Waiter`, `Cashier`, `Table`, and `GameStats` (28 Oct 2026)

### Me

> Hi, I am implementing my assigned Stage 1 entity classes (`Staff`, `Table`, `GameStats`, `Waiter`, and `Cashier`) plus drafting `docs/key-abstractions-and-solid.md`.
>
> For `Staff`, every staff member (Waiter, Chef, Cashier) can only perform at most one action per turn. I wrote `Staff.java` like this:
>
> ```java
> package entity;
>
> public abstract class Staff {
>     private final String roleName;
>     private boolean actedThisTurn;
>
>     protected Staff(String roleName) {
>         if (roleName == null || roleName.isBlank()) {
>             throw new IllegalArgumentException("Role name cannot be empty");
>         }
>         this.roleName = roleName;
>         this.actedThisTurn = false;
>     }
>
>     public void startNewTurn() {
>         this.actedThisTurn = false;
>     }
>
>     public boolean hasActedThisTurn() {
>         return actedThisTurn;
>     }
>
>     protected void checkCanAct() throws InvalidActionException {
>         if (actedThisTurn) {
>             throw new InvalidActionException(roleName + " has already acted this turn");
>         }
>     }
>
>     protected void markActed() {
>         this.actedThisTurn = true;
>     }
>
>     public String getRoleName() {
>         return roleName;
>     }
> }
> ```
>
> And in `Waiter.java`, I implemented `seat(Customer, Table)`, `takeOrder(Customer, Menu)`, and `serve(Order, int currentTurn)`. Why is it better to keep `seat`, `takeOrder`, and `serve` on `Waiter` (and `workOn` on `Chef`, `processNextPayment` on `Cashier`) rather than putting abstract `performAction(...)` on `Staff`?

### Claude

Your `Staff` base class and role-specific methods illustrate both **Inheritance** and the **Interface Segregation Principle (ISP)** / **Liskov Substitution Principle (LSP)**:

1. **What Belongs in `Staff` (Shared Invariant):**
   - Every staff role shares the exact same per-turn availability lifecycle: `startNewTurn()`, `hasActedThisTurn()`, `checkCanAct()`, and `markActed()`.
   - Making `actedThisTurn` `private` and exposing `checkCanAct()` and `markActed()` as `protected` ensures outside classes (`GameController`, `ConsoleUI`) cannot arbitrarily flip a staff member's acted flag, while every `Staff` subclass enforces the one-task-per-turn rule in one line.

2. **Why `performAction(...)` Does Not Belong on `Staff` (ISP & Type Safety):**
   - A `Waiter` needs a `Customer` and a `Table` (to seat), a `Customer` and a `Menu` (to take an order), or an `Order` and `currentTurn` (to serve).
   - A `Chef` needs an `Order` and `currentTurn`.
   - A `Cashier` scans the list of `Customer`s at occupied tables to find the earliest `READY_TO_PAY` customer.
   - Forcing a single abstract `performAction(...)` onto `Staff` would require passing dummy parameters or `Object` casts that don't apply to the other roles. Keeping role-specific operations on each concrete subclass while sharing turn-state management in `Staff` avoids fat interfaces and dummy no-op methods.

---

## 2. Reviewing `Cashier.processNextPayment` and `GameStats` (28 Oct 2026)

### Me

> Here is my implementation of `Cashier.processNextPayment` and `GameStats`:
>
> ```java
> public Customer processNextPayment(List<Customer> candidates, int currentTurn) throws InvalidActionException {
>     checkCanAct();
>     Customer oldestReady = null;
>     for (Customer c : candidates) {
>         if (c != null && c.getStatus() == CustomerStatus.READY_TO_PAY && c.getReadyToPayTurn() < currentTurn) {
>             if (oldestReady == null || c.getReadyToPayTurn() < oldestReady.getReadyToPayTurn()
>                     || (c.getReadyToPayTurn() == oldestReady.getReadyToPayTurn() && c.getCustomerId() < oldestReady.getCustomerId())) {
>                 oldestReady = c;
>             }
>         }
>     }
>     if (oldestReady == null) {
>         return null;
>     }
>     oldestReady.markPaid();
>     markActed();
>     return oldestReady;
> }
> ```
>
> And in `GameStats`, instead of storing a separate `int paidCount` field alongside `List<Integer> paidSatisfactions`, I derived `getPaidCount()` as `paidSatisfactions.size()`. Can you check if my tie-breaker in `Cashier` and derived count in `GameStats` have any edge cases?

### Claude

Both choices are very solid:

1. **`Cashier.processNextPayment`:**
   - Checking `c.getReadyToPayTurn() < currentTurn` enforces the rule in Section 3.2 Step 4 (*"A customer who became READY_TO_PAY this turn pays next turn at the earliest"*).
   - Comparing `c.getReadyToPayTurn() < oldestReady.getReadyToPayTurn()` with a deterministic tie-breaker on `c.getCustomerId() < oldestReady.getCustomerId()` ensures that if two customers ever became `READY_TO_PAY` on the same turn, the earlier customer ID always pays first.
   - Notice that when `oldestReady == null`, you return `null` *without* calling `markActed()`, which is right because the Cashier had no customer to charge.

2. **`GameStats` Derived `getPaidCount()`:**
   - Deriving `getPaidCount()` from `paidSatisfactions.size()` eliminates redundant state: it is impossible for `paidCount` and `paidSatisfactions` to get out of sync, and `getAverageSatisfaction()` is guaranteed not to divide by zero when `getPaidCount() > 0`.

---

## 3. Debugging Piped Input on Windows PowerShell & Cashier Status Display (29 Oct 2026)

### Me

> When I tested `tests/stage1_victory.txt` using `Get-Content tests/stage1_victory.txt | java -cp out control.Main` in Windows PowerShell, the first line `"1"` was rejected as `"Please enter a valid number"`! When I printed the string characters, the first line had character code `65279` (`﻿`) at index 0.
>
> Also, in `ConsoleUI.showStartOfTurn()`, I noticed the Cashier line was hardcoded to `"Cashier : Ready"` instead of checking `restaurant.getCashier().hasActedThisTurn()`.
>
> I added `if (line.startsWith("﻿")) { line = line.substring(1); }` before `.trim()` in `ConsoleUI.readInt`, and updated the Cashier status line to check `hasActedThisTurn()`. Why does PowerShell add `﻿` when piping?

### Claude

Great debugging! `﻿` is the **UTF-8 Byte-Order Mark (BOM)**:
- In Windows PowerShell 5.1, the pipeline operator (`|`) re-encodes text streams passed to external native processes (`java.exe`) and prepends a UTF-8 BOM (`﻿`) at the very beginning of `System.in`.
- Because `Integer.parseInt("﻿1")` does not treat `﻿` as whitespace (and `String.trim()` only strips characters `<= ' '`), `parseInt` throws `NumberFormatException` on the very first line of piped input, shifting every subsequent scripted answer by one line.
- Stripping `﻿` at the start of `line` in `ConsoleUI.readInt` makes your test scripts (`stage1_victory.txt`, `stage1_defeat.txt`, `stage1_invalid_inputs.txt`) work identically across macOS/Linux `bash`/`zsh`, Windows `cmd.exe`, and Windows PowerShell.

---

## Verification & Key Ideas Formulated (Appendix I)

- **Key Ideas Formulated by Me:**
  1. Centralizing the per-turn staff action guard (`checkCanAct()` / `markActed()`) in the abstract `Staff` superclass while keeping role-specific action signatures on `Waiter`, `Chef`, and `Cashier`.
  2. Deriving `GameStats.getPaidCount()` from `paidSatisfactions.size()` so paid customer count and satisfaction list cannot diverge.
  3. Diagnosing and stripping the UTF-8 Byte-Order Mark (`﻿`) in `ConsoleUI.readInt` for cross-platform piped test scripts.
- **How I Verified AI Suggestions:**
  - Compiled and ran `tests/stage1_victory.txt` (`5 paid, $66.00, 79.2`), `tests/stage1_defeat.txt` (`4 paid, $51.00, 58.0`), and `tests/stage1_invalid_inputs.txt` on both Bash and PowerShell.
