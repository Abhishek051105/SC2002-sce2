package entity;

/**
 * Shared "one task per turn" rule. Each role checks its own rules first
 * and calls markActed() only when the action succeeds.
 *
 * @author Aekas, Abhishek, Aaryan
 * @since 2026-10-26 (Stage 1)
 */
public abstract class Staff {
    private final String roleName;
    private boolean actedThisTurn;

    /** @param roleName name used in error messages, e.g. "Waiter" */
    protected Staff(String roleName) {
        this.roleName = roleName;
        this.actedThisTurn = false;
    }

    /** Called at the start of every turn so the staff member can act again. */
    public void startNewTurn() {
        actedThisTurn = false;
    }

    /** @return true if this staff member has used their task this turn */
    public boolean hasActedThisTurn() {
        return actedThisTurn;
    }

    /** Throws "Waiter has already acted this turn" etc. */
    protected void checkCanAct() throws InvalidActionException {
        if (actedThisTurn) {
            throw new InvalidActionException(roleName + " has already acted this turn");
        }
    }

    /** Uses up this turn's task. Only called after an action succeeds. */
    protected void markActed() {
        actedThisTurn = true;
    }
}
