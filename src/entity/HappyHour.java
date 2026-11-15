package entity;

/**
 * Happy Hour: 20% off orders taken while it is on. Can be started once per
 * game and lasts that turn and the next.
 *
 * @author Aekas
 * @since 2026-11-06 (Stage 3)
 */
public class HappyHour implements PricingPolicy {
    /** Happy Hour lasts the turn it starts and the next one. */
    public static final int DURATION_TURNS = 2;
    /** Satisfaction each waiting customer recovers per Happy Hour turn. */
    public static final int RECOVERY_POINTS = 15;

    private boolean used;
    private int turnsLeft;

    /** Starts Happy Hour for this turn and the next. */
    public void activate() throws InvalidActionException {
        if (used) {
            throw new InvalidActionException("Happy Hour has already been used this game");
        }
        used = true;
        turnsLeft = DURATION_TURNS;
    }

    /** @return true while Happy Hour is running */
    public boolean isActive() {
        return turnsLeft > 0;
    }

    /** @return true if Happy Hour hasn't been used yet this game */
    public boolean isAvailable() {
        return !used;
    }

    /** @return turns left, including the current one */
    public int getTurnsLeft() {
        return turnsLeft;
    }

    /** Called once at the end of every turn. */
    public void endTurn() {
        if (turnsLeft > 0) {
            turnsLeft = turnsLeft - 1;
        }
    }

    /** @return the bill with 20% off, rounded to the nearest cent */
    @Override
    public int applyTo(int billCents) {
        return (billCents * 80 + 50) / 100;   // 20% off, rounded to the nearest cent
    }
}
