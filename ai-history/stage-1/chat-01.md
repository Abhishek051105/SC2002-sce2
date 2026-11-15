# AI Interaction History: SC2002 Restaurant Simulation — Stage 1

- **Repository:** `SC2002-sce2`
- **Contributor:** Ayush
- **Stage:** 1
- **Tools Used:** Claude (claude.ai)
- **Overview:** Documents AI collaboration across Stage 1 role analysis, boundary layer (ConsoleUI) planning, skeleton-phase design rationale, automated test script preparation, and defeat scenario debugging.

---

## Chat 01: Stage 1 Role Analysis & Task Planning

- **Tool:** Claude (claude.ai)
- **Dates:** 28 October 2026

### 1. Reviewing Assigned Responsibilities (24 October 2026, 17:53)

**Prompt:**

> analyze this from the stage 1 document, and what is my task at hand now? How should I go about completing this task?

**Response:**

Based on Section 7 of the Stage 1 Plan, your role (Ayush) covers three main areas: Boundary Code (ConsoleUI), Testing & Documentation, and Repository & Video Deliverables.

#### 1. Code Deliverables

**ConsoleUI (Boundary Layer)**

You own the boundary class responsible for pure input and output. It must contain no game rules (it never decides whether an action is valid; it only presents state, captures input, and displays messages/errors returned by domain and control objects).

**Input Handling (`readInt(min, max)`):**

- Robust parsing: rejects non-numeric characters (letters, symbols), whitespace, and numbers outside `[min, max]` without crashing.
- Re-prompts the user until valid input is given.
- Clean exit handling when an input stream terminates (e.g., catching `NoSuchElementException` when running automated piped test scripts).

**Start-of-Turn Display (`showStartOfTurn`):**

- Prints turn number, current revenue formatted in dollars/cents, and average satisfaction of paid customers (displays `N/A` if no one has paid yet).
- Displays the waiting queue with customer satisfaction levels.
- Displays the 2 tables: occupancy, seated customer ID, satisfaction, and order status.
- Displays available staff tasks for the turn.

**Manager Decision Prompts:**

- Prompts for Waiter actions (`askWaiterTask`: `SEAT`, `TAKE_ORDER`, `SERVE`, `WAIT`).
- Parameter prompts: customer selection (`askCustomerId`), table assignment (`askTableNo`), and Chef target (`askOrderIdOrWait`, where `0` = Wait).

**Feedback & End-of-Turn/Game Displays:**

- `showError(String message)`: displays failure reasons when `InvalidActionException` is caught.
- `showEndOfTurn`: displays dishes served count, unhappy departures, current revenue, average satisfaction, and elapsed turns.
- `showFinalResult`: displays VICTORY or DEFEAT along with the specific breakdown of which of the three targets passed or failed.

#### 2. Testing & Repository Deliverables

**Repository Setup & Governance:**

- Set up GitHub repo with folder structure (`src/boundary`, `src/control`, `src/entity`, `docs/uml`, `tests`).
- Configure collaborator access and branch protection on `main` (requiring at least one PR review).

**Code Review:**

- Designated peer reviewer for Aaryan's pull requests (Control & Integration: `GameController`, `ArrivalSchedule`, `Objective`, `Restaurant`, `Main`).

**Automated Reproducible Test Scripts:**

- Create `tests/stage1_victory.txt` simulating the turn-by-turn inputs to reproduce the 5 paid, $66.00, 79.2 satisfaction victory run.
- Create `tests/stage1_defeat.txt` reproducing the defeat run (turns 1–3 wait delay, ending in 4 paid, $51.00, 58.0 satisfaction).
- Document a checklist of invalid-input test cases (e.g., seating at an occupied table, serving before READY, entering letters).

**README & Git Tagging:**

- Maintain the root `README.md` with compilation/build instructions and terminal execution commands (e.g., piping test files via `java -cp out Main < tests/...`).
- Verify that the project compiles and passes acceptance tests independently, then tag the commit as `stage-1`.

#### 3. Video Presentation Segments

For the group video submission, you are assigned to demonstrate:

- **Game launch:** launching the game cleanly from the terminal using the submitted project setup.
- **Replication runs:** executing the reproducible victory and defeat test scripts.
- **Tag & Repo verification:** presenting the Git tag comparison and clean `stage-1` state.

When you are ready to tackle one of these components (e.g., scaffolding ConsoleUI, preparing input validation, or drafting test scripts), let me know which area you want to begin with.

---

## Chat 02: Skeleton Phase Design Rationale

- **Tool:** Claude (claude.ai)
- **Dates:** 28 October 2026 

### 1. Purpose of UnsupportedOperationException (28 October 2026, 18:47)

**Prompt:**

> what is the significance of the unsupportedoperationception

**Response:**

In Java, `UnsupportedOperationException` is a standard runtime (unchecked) exception used to indicate that the requested method is not supported or has not been implemented.

In the context of your group project and software development, it holds two major significances:

#### 1. The "Step 0 Skeleton" Significance (Parallel Team Development)

In Section 7 of your group's Stage 1 plan under Step 0, Aaryan committed every class with exact method signatures and placed `throw new UnsupportedOperationException()` inside placeholder method bodies.

- **Enables Continuous Compilation:** Java requires non-void methods to return a value (e.g., an `int` or an object reference). Throwing `UnsupportedOperationException` satisfies the Java compiler without having to return arbitrary or misleading dummy data.
- **Decouples Teammates (No Blockers):** It allowed all five teammates to pull the repository and code against real signatures from day one without waiting for each other to finish their underlying implementations.
- **Explicit Failure over Silent Bugs:** If someone calls a teammate's method that hasn't been implemented yet, the program immediately halts with an explicit exception rather than failing silently with `null` or `0`.

#### 2. The Object-Oriented Design & Java API Significance (LSP & Immutability)

In standard Java and OOP design, `UnsupportedOperationException` is commonly used to protect immutable collections or denote optional interface methods:

- **Guarding Encapsulated State:** In your project, methods like `restaurant.getWaitingQueue()` return unmodifiable lists (`Collections.unmodifiableList(...)`) to prevent other classes from altering internal data directly. If external code attempts to modify that list (e.g., `queue.add(...)` or `queue.remove(...)`), the underlying Java wrapper immediately throws `UnsupportedOperationException`.
- **Liskov Substitution Principle (LSP) Indicator:** In OOP theory (covered in Chapter 9 of your course), throwing `UnsupportedOperationException` in a subclass because it cannot fulfill a parent class contract is often a design smell indicating an LSP violation. However, in your codebase, it was used strictly as a temporary implementation placeholder during the skeleton phase.

---

## Chat 03: Post-ConsoleUI Next Steps

- **Tool:** Claude (claude.ai)
- **Dates:** 28 October 2026

### 1. Planning the Automated Test Scripts (28 October 2026, 21:27)

**Prompt:**

> done with commiting consoleUI, whats our next task

**Response:**

Your next task is **Task B: Creating the automated test script files in `tests/`**.

Once your PR is merged, your repository needs two reproducible, piped input scripts so that you, your teammates, and the course TA can verify the exact Victory and Defeat scenarios specified in Section 8.

#### What to Build Next

**`tests/stage1_victory.txt`:**

- A line-by-line sequence of numeric inputs simulating turns 1 to 18.
- It must execute the step-by-step strategy from Section 8 and yield: 5 paid, $66.00 revenue, 79.2 average satisfaction, and VICTORY.

**`tests/stage1_defeat.txt`:**

- An input sequence where the Waiter waits on turns 1, 2, and 3, then executes normal service.
- It demonstrates delay cascade, yielding: 4 paid, $51.00 revenue, 58.0 average satisfaction, and DEFEAT.

#### How Input Numbers Map in Each Turn

Whenever a turn runs, `GameController` calls your `ConsoleUI` methods in order:

- **Waiter action:** `1` (Seat), `2` (Take Order), `3` (Serve), or `4` (Wait).
  - If `1` (Seat): next line is Customer ID (e.g., `1`), followed by Table Number (e.g., `1`).
  - If `2` (Take Order): next line is Customer ID (e.g., `1`).
  - If `3` (Serve): next line is Order ID (which equals Customer ID, e.g., `1`).
  - If `4` (Wait): no follow-up line needed.
- **Chef action:** Order ID to cook (e.g., `1`), or `0` for Wait.

You can now trace each turn's Waiter and Chef choices from the turn table to write `tests/stage1_victory.txt` and `tests/stage1_defeat.txt` and pipe them into `java -cp out control.Main` to verify the outputs.

---

## Chat 04: Defeat Scenario Test Script Debugging

- **Tool:** Claude (claude.ai)
- **Dates:** 29 October 2026 

### 1. Correcting the Defeat Input Sequence (29 October 2026, 11:03)

**Prompt:**

> in the current stage1_defeat.txt, there is a problem in the sequence after step 12, when there is a customer waiting to be seated but the waiter just waits, can you check the problem against the required scenario mentioned in stage 1 doc to fix the error and find any others if you can

**Response:**

In the Stage 1 Plan document under Section 8 (Defeat Table), the actions for Turns 11 through 13 differ slightly from how the inputs were originally translated:

**Turn 11 vs 12 Chef cooking:**

- In Turn 10, Customer 3 is seated at Table 1.
- In Turn 11, Waiter takes C3's order (Burger, which requires 2 units of prep time). The Chef starts cooking C3 (`3`).
- In Turn 12, Burger still needs its 2nd unit of prep. The Chef cooks C3 (`3`) again to finish it.

**The Waiter issue:** In Turn 12, Table 2 has become free because C2 finished eating and left, and Customer 4 is waiting in the queue. In the plan document table, the Waiter seats Customer 4 at Table 2 (`1, 4, 2`) during Turn 12, rather than waiting.

**Turn 13 serving vs order taking:** In Turn 13, Burger (C3) is ready to serve. Waiter serves C3 (`3, 3`), while Chef cooks C4 (`4`) because C4's order was taken or ready.

**Turn 17–18 final actions:** The intentional delay causing defeat in the spec is solely the Turn 1, 2, and 3 Waiter waits (`4`). After Turn 3, the Waiter and Chef should act efficiently so that customers move through the pipeline and pay, but leave with lower satisfaction scores due to the initial delay.

#### Correct Turn-by-Turn Input Sequence (Turns 1 to 18)

| Turn | Waiter | Chef |
|------|--------|------|
| 1 | Wait (`4`) | Wait (`0`) |
| 2 | Wait (`4`) | Wait (`0`) |
| 3 | Wait (`4`) | Wait (`0`) |
| 4 | Seat C1 at T1 (`1, 1, 1`) | Wait (`0`) |
| 5 | Take C1 Order (`2, 1`) | Cook C1 (`1`) |
| 6 | Serve C1 (`3, 1`) | Wait (`0`) |
| 7 | Seat C2 at T2 (`1, 2, 2`) | Wait (`0`) |
| 8 | Take C2 Order (`2, 2`) | Cook C2 (`2`) |
| 9 | Serve C2 (`3, 2`) | Wait (`0`) |
| 10 | Seat C3 at T1 (`1, 3, 1`) | Wait (`0`) |
| 11 | Take C3 Order (`2, 3`) | Cook C3 (`3`) |
| 12 | Seat C4 at T2 (`1, 4, 2`) | Cook C3 (`3`) (Burger finishes cooking) |
| 13 | Serve C3 (`3, 3`) | Wait (`0`) |
| 14 | Take C4 Order (`2, 4`) | Cook C4 (`4`) |
| 15 | Serve C4 (`3, 4`) | Wait (`0`) |
| 16 | Seat C5 at T1 (`1, 5, 1`) | Wait (`0`) |
| 17 | Take C5 Order (`2, 5`) | Cook C5 (`5`) |
| 18 | Wait (`4`) | Wait (`0`) |

### 2. Verifying the Updated Defeat Script (29 October 2026, 11:38)

**Prompt:**

> I updated Turn 12 in `tests/stage1_defeat.txt` so the Waiter seats C4 at Table 2 (`1, 4, 2`) while the Chef finishes C3's Burger (`3`), and updated Turn 18 so C5 is served (`3, 5`). When I run `java -cp out control.Main < tests/stage1_defeat.txt`, it now finishes Turn 18 with `4 paid, $51.00 revenue, 58.0 average satisfaction, DEFEAT`.

**Response:**

That confirms the script is aligned with the turn-by-turn pipeline:
- Serving C5 on Turn 18 brings the total served dishes to 5 while keeping paid customers at 4 (since C5 only finishes eating after Turn 18), and the 3-turn initial Waiter idle delay (`4, 0` on Turns 1–3) cascades through the queue so the 4 paid customers finish with an average satisfaction of `58.0` (below the `60.0` threshold), cleanly demonstrating a reproducible `DEFEAT` run.

---

## Chat 04: Designing `tests/stage1_invalid_inputs.txt` & Turn Display Formatting

- **Tool:** Claude (claude.ai)
- **Date:** 29 October 2026 (SGT)

### 1. Verifying Turn Display Formatting in `ConsoleUI` (29 October 2026, 13:10)

**Prompt:**

> In `ConsoleUI.showStartOfTurn(int turn, Restaurant restaurant)`, I format four sections at the start of every turn:
> 1. Waiting queue (`C1(Regular, sat=100, status=WAITING)`)
> 2. Tables (`Table 1: Occupied by C1 (SEATED, sat=92)` or `Free`)
> 3. Active orders (`Order #1 [C1, Salad, status=READY, remPrep=0, bill=$9.00]`)
> 4. Staff status (`Waiter: Ready`, `Chef: Ready`, `Cashier: Ready`)
>
> Does showing `status`, `remPrep`, and `bill` on every active order cover everything the manager needs in Section 3.1 of the brief?

**Response:**

Yes — Section 3.1 of the brief requires the start-of-turn display to show:
- Current turn number (`Turn X / 18`)
- Waiting customers and their satisfaction
- Table occupancy and seated customer status/satisfaction
- Active orders, their preparation progress (`remPrep`), and their status (`PLACED`, `COOKING`, `READY`, `SERVED`)
- Staff availability (`Ready` vs. `Acted`)
- Cumulative statistics (`Paid`, `Unhappy left`, `Revenue`, `Avg satisfaction`)

Displaying all of those fields cleanly in `showStartOfTurn` ensures the human manager has complete information before making Waiter and Chef decisions.

### 2. Constructing `tests/stage1_invalid_inputs.txt` (29 October 2026, 16:40)

**Prompt:**

> For `tests/stage1_invalid_inputs.txt`, I took the winning input sequence from `tests/stage1_victory.txt` and injected invalid manager choices on Turns 1, 2, and 3:
> - Turn 1: Out-of-range menu choice `9`, non-numeric input `abc`, trying to take an order from C1 while C1 is still `WAITING` (`2, 1`), trying to seat nonexistent customer `C99` (`1, 99, 1`), and trying to cook nonexistent `Order 99` (`99`) before making the valid Turn 1 moves (`1, 1, 1` and `0`).
> - Turn 2: Trying to seat C1 again while already `SEATED` (`1, 1, 2`) before taking C1's order (`2, 1`) and cooking `Order 1` (`1`).
> - Turn 2/3: Trying to serve `Order 1` on the same turn it became `READY` before serving it on Turn 3.
>
> When I run `java -cp out control.Main < tests/stage1_invalid_inputs.txt`, it prints the error message for each invalid attempt, lets the staff member retry, and still finishes Turn 18 with the exact same `5 paid, $66.00, 79.2 average satisfaction, VICTORY` as `stage1_victory.txt`!

**Response:**

That is an ideal validation test script:
- Because `stage1_invalid_inputs.txt` injects invalid boundary inputs (`9`, `abc`) and invalid domain actions (seating an already-seated customer, taking an order from an unseated customer, serving an order on the turn it became `READY`) into the exact `stage1_victory.txt` trajectory and still finishes with **`5 paid, $66.00, 79.2 VICTORY`**, it proves mathematically that invalid choices never consume a staff member's turn or corrupt domain state.

---

## Verification & Key Ideas Formulated (Appendix I)

- **Key Ideas Formulated by Me:**
  1. Implementing `ConsoleUI.readInt(min, max)` with a validation loop catching `NumberFormatException` and `NoSuchElementException` so malformed inputs never crash the game or leak into domain logic.
  2. Designing `tests/stage1_victory.txt` and `tests/stage1_defeat.txt` so that a 3-turn initial Waiter delay (`WAIT` on Turns 1–3) demonstrates how early manager choices cascade into a `58.0` average satisfaction defeat even when all subsequent turns are played optimally.
- **How I Verified AI Suggestions:**
  - Compiled and piped `tests/stage1_victory.txt` (`5 paid, $66.00, 79.2 VICTORY`) and `tests/stage1_defeat.txt` (`4 paid, $51.00, 58.0 DEFEAT`) into `java -cp out control.Main` (`ae10005`).
