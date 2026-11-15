package entity;

/**
 * A table that holds at most one customer.
 *
 * @author Aekas, Aaryan
 * @since 2026-10-26 (Stage 1)
 */
public class Table {
    private final int tableNo;
    private Customer occupant;

    /** @param tableNo table number shown to the player (1-based) */
    public Table(int tableNo) {
        this.tableNo = tableNo;
        this.occupant = null;
    }

    /** @return true if nobody is sitting here */
    public boolean isFree() {
        return occupant == null;
    }

    /**
     * Puts a customer at this table. The Waiter/Host check isFree() first,
     * so this only fails on a programming bug.
     */
    public void assign(Customer c) {
        if (!isFree()) {
            throw new IllegalStateException("Table " + tableNo + " is occupied");
        }
        occupant = c;
    }

    /** Frees the table (after payment or when the customer leaves). */
    public void release() {
        occupant = null;
    }

    /** @return the table number */
    public int getTableNo() {
        return tableNo;
    }

    /** null when free. */
    public Customer getOccupant() {
        return occupant;
    }
}
