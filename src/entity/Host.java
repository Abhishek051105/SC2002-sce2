package entity;

/**
 * Stage 3 Host: seats one queued customer per turn, separately from the
 * Waiter. Uses the same checks as the Waiter's seat; the Waiter can still
 * seat as well.
 *
 * @author Aaryan
 * @since 2026-11-05 (Stage 3)
 */
public class Host extends Staff {

    /** Creates the Host. */
    public Host() {
        super("Host");
    }

    /**
     * Seats a queued customer at a free table.
     *
     * @param c the customer; must be WAITING
     * @param t the table; must be free
     * @throws InvalidActionException if the Host already acted, the customer
     *                                isn't queued, or the table is occupied
     */
    public void seat(Customer c, Table t) throws InvalidActionException {
        checkCanAct();

        if (c.getStatus() != CustomerStatus.WAITING) {
            throw new InvalidActionException("Customer " + c.getCustomerId() + " is not in the queue");
        }

        if (!t.isFree()) {
            throw new InvalidActionException("Table " + t.getTableNo() + " is occupied");
        }

        t.assign(c);
        c.seat();
        markActed();
    }
}