# AI Interaction History: SC2002 Restaurant Simulation — Stage 3

- **Repository:** `SC2002-sce2`
- **Contributor:** Ayush
- **Stage:** 3 (Stage 3 requirements just arrived: `Host`, `ComboMeal`, `HappyHour`)
- **Tools Used:** Claude (claude.ai)
- **Overview:** Documents AI collaboration across Stage 3 `ConsoleUI` prompt design for Happy Hour and Host, fixing the start-of-turn staff status display, and verifying the Stage 3 test, abandonment, and regression script suite.

---

## Chat 01: Stage 3 `ConsoleUI` Prompts & Staff Status Display

- **Tool:** Claude (claude.ai)
- **Dates:** 7–8 November 2026 (SGT)

### 1. Reviewing My Stage 3 `ConsoleUI` Prompt Methods (7 November 2026, 17:15)

**Prompt:**

> For Stage 3, I am updating `ConsoleUI.java` on branch `feat/stage3-ui` to add the manager prompts for Happy Hour and the Host:
> - `askStartHappyHour()`: prompts `"Start Happy Hour now? It can be used once and lasts 2 turns (1 = yes, 0 = no): "` and returns `readInt(0, 1) == 1`.
> - `showHappyHourStatus(int turnsLeft)`: prints `"*** HAPPY HOUR ON: 20% off new orders, " + turnsLeft + " turn(s) left including this one ***"`.
> - `askHostCustomerIdOrWait()`: prints `"--- Host Action ---"` and `"Customer to seat (0 = wait): C"` and returns `readInt(0, 999)`.
>
> Does keeping these three methods in `ConsoleUI` using `readInt(0, 1)` and `readInt(0, 999)` satisfy the rule that `ConsoleUI` contains no game rules?

**Response:**

Yes — those three methods keep the boundary layer (`ConsoleUI`) strictly focused on presentation and input parsing:
1. **`askStartHappyHour()`:** Only validates that the input integer is `0` or `1` (`readInt(0, 1) == 1`). It does not decide *whether* Happy Hour is available to be used — `GameController` checks `happyHour.isAvailable()` before calling `ui.askStartHappyHour()`, and `HappyHour.activate()` enforces the once-per-game domain rule.
2. **`askHostCustomerIdOrWait()`:** Only captures the customer ID (`0` for wait, or `1..999`) via `readInt(0, 999)`. It does not check whether that customer is in the waiting queue — `Host.seat(c, t)` validates customer status and table occupancy in the entity layer.
3. **`showHappyHourStatus(int turnsLeft)`:** Displays the remaining turns passed in by `GameController` without storing any turn counter inside `ConsoleUI`.

### 2. Fixing the Start-of-Turn Staff Status Lines (8 November 2026, 18:05)

**Prompt:**

> In Aaryan's review of my `feat/stage3-ui` PR, he noted that when I added the `Host` line to `showStartOfTurn()` before `restaurant.getHost()` was merged on `main`, I had temporarily written `"Ready"` for the staff status lines instead of checking `hasActedThisTurn()`. Now that `restaurant.getHost()` is merged on `main`, I updated `showStartOfTurn()` to check `hasActedThisTurn()` for all four staff members (`Host`, `Waiter`, `Chef`, `Cashier`).

**Response:**

Restoring the dynamic `hasActedThisTurn() ? "Acted" : "Ready"` check for `Host`, `Waiter`, `Chef`, and `Cashier` ensures the start-of-turn display always reflects the true `Staff` state from `Restaurant` rather than hardcoded strings, and stays consistent with the Stage 1 and Stage 2 status formatting.

---

## Chat 02: Stage 3 Test Suite, Slow-Start Host Comparison & Regression Scripts

- **Tool:** Claude (claude.ai)
- **Dates:** 9–14 November 2026 (SGT)

### 1. Designing the Stage 3 Regression Suite & Host Comparison Run (9 November 2026, 10:05)

**Prompt:**

> I am building the Stage 3 test scripts in `tests/` and documenting them in `docs/test-notes.md`:
> 1. To prove backward compatibility on the Stage 3 build (`*_on_stage3.txt`), since every turn in Stage 3 now asks `askStartHappyHour()` (while available) and `askHostCustomerIdOrWait()` before the Waiter prompt, I prepended `0` (don't start Happy Hour) and `0` (Host waits) to each turn of our 6 Stage 1 and Stage 2 scripts.
> 2. I also created `tests/stage3_slow_start_with_host.txt` where the player idles for the first 3 turns (just like `stage1_defeat.txt` and `stage2_defeat.txt`), and never uses Happy Hour or combos (`stage2` mode), but uses the `Host` from Turn 4 onwards to seat waiting customers in parallel with the Waiter taking orders. It turns the 3-turn idle defeat (`53.8 DEFEAT`) into a `5 paid, $64.80, 72.3 VICTORY`!
> 3. On Nov 14, I also added `tests/stage3_abandonment.txt` so we have a dedicated script demonstrating a Critic reaching `0` satisfaction on Turn 18 and abandoning their table (`Unhappy left: 1`).
>
> Can you check if these scripts cover all functional and regression evidence required by Sections 4 and 5 of the brief?

**Response:**

That test suite covers every functional and regression requirement in Sections 4 and 5:
- **6 Regression Scripts (`stage1_*_on_stage3.txt` in `regular` mode and `stage2_*_on_stage3.txt` in `stage2` mode):** Passing `0` for Happy Hour and `0` for Host on every turn proves that the Stage 3 codebase reproduces the exact Stage 1 (`79.2 / 58.0 / 79.2`) and Stage 2 (`75.6 / 53.8 / 75.6`) numbers when Stage 3 features are left unused.
- **Stage 3 Core Runs (`stage3_victory.txt`, `stage3_defeat.txt`, `stage3_invalid_inputs.txt`):** Demonstrate all three Stage 3 features (`Host`, `ComboMeal`, `HappyHour`) working together (`5 paid, $81.54, 84.6 VICTORY`), how poor manager choices lead to `DEFEAT` (`4 paid, $62.10, 56.0`), and that invalid Host/Waiter/Happy Hour inputs are rejected without consuming staff turns.
- **Controlled Manager-Choice Comparison (`stage3_slow_start_with_host.txt` & `stage3_abandonment.txt`):** `stage3_slow_start_with_host.txt` isolates the exact impact of the `Host` (recovering from a 3-turn idle start that lost in Stages 1 and 2), and `stage3_abandonment.txt` provides reproducible verification of customer abandonment (`CustomerStatus.LEFT`, order cancellation, and table release).

---

### 2. Designing `tests/stage3_invalid_inputs.txt` and `tests/stage3_abandonment.txt` (9–14 November 2026)

**Prompt (9 November 2026, 11:30):**

> For `tests/stage3_invalid_inputs.txt` (`6ae785d`), I injected Stage 3-specific invalid actions into the `stage3_victory.txt` sequence:
> - Out-of-range input `9` at the `askStartHappyHour()` prompt (`0` or `1` required).
> - Having the `Host` try to seat nonexistent customer `C99` on Turn 1 before seating `C1` at Table 1.
> - Having the `Host` try to seat `C2` at occupied Table 1 on Turn 4 (`Table 1 is already occupied`) before seating `C2` at free Table 2.
> - Having the `Waiter` try to seat `C2` again right after the `Host` already seated `C2` (`Customer 2 is not WAITING`).
>
> And on 14 November (`991d1a3`), I also created `tests/stage3_abandonment.txt` where C4 (`CriticCustomer`, arriving on Turn 10 with `-12` decay per turn) is seated by the Host at Table 2 on Turn 10 and orders `Salad + Pasta`, but the Chef never finishes cooking Order 4. Can you check C4's exact satisfaction countdown from Turn 10 to Turn 18 in `stage3_abandonment.txt`?

**Response:**

Let's verify C4's satisfaction countdown in `tests/stage3_abandonment.txt` (with Happy Hour never activated, so C4 loses `12` points at the end of every turn from Turn 10 onwards):
- **Turn 10 end:** `100 - 12 = 88`
- **Turn 11 end:** `88 - 12 = 76`
- **Turn 12 end:** `76 - 12 = 64`
- **Turn 13 end:** `64 - 12 = 52`
- **Turn 14 end:** `52 - 12 = 40`
- **Turn 15 end:** `40 - 12 = 28`
- **Turn 16 end:** `28 - 12 = 16`
- **Turn 17 end:** `16 - 12 = 4`
- **Turn 18 end:** `Math.max(0, 4 - 12) = 0` -> `decreaseSatisfaction()` transitions C4 to `CustomerStatus.LEFT` and returns `true`!

When `decreaseSatisfaction()` returns `true` at the end of Turn 18, `GameController.handleEndOfTurnWaiting()` calls `restaurant.customerLeft(c4, order4, table2)`, which:
1. Cancels C4's unfinished `Order 4` (`OrderStatus.CANCELLED`),
2. Releases `Table 2` (`table2.release()`), and
3. Increments `stats.recordAbandonment()` (`Unhappy left: 1`), resulting in `3 paid, 1 unhappy left, $42.30 revenue, 91.0 avg satisfaction, DEFEAT`.

---

## Verification & Key Ideas Formulated (Appendix I)

- **Key Ideas Formulated by Me:**
  1. Constructing the 6 `*_on_stage3.txt` regression scripts by inserting `0` (decline Happy Hour) and `0` (Host waits) at each turn to prove zero regression on Stage 1 and Stage 2 scenarios.
  2. Designing `tests/stage3_slow_start_with_host.txt` (`96f9785`) and `tests/stage3_abandonment.txt` (`991d1a3`) to isolate the impact of the `Host` and verify customer abandonment at `0` satisfaction.
- **How I Verified AI Suggestions:**
  - Executed all 11 test scripts on the final Stage 3 build and verified every output against `docs/test-notes.md` and `README.md`.
