package entity;

/**
 * Thrown when the manager picks an action the rules don't allow
 * (e.g. "Table 2 is occupied"). Checked, so callers must handle it.
 * The staff member has NOT used their task when this is thrown.
 *
 * @author Aaryan
 * @since 2026-10-26 (Stage 1)
 */
public class InvalidActionException extends Exception {
    /** @param reason message shown to the player */
    public InvalidActionException(String reason) {
        super(reason);
    }
}
