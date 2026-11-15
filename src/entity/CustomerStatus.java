package entity;

/**
 * Where a customer is in their visit. The normal order is
 * WAITING, SEATED, WAITING_FOR_FOOD, SERVED, READY_TO_PAY, PAID.
 * LEFT means they ran out of satisfaction and walked out.
 *
 * @author Raghav, Aaryan
 * @since 2026-10-22 (Stage 1)
 */
public enum CustomerStatus {
    /** In the queue, waiting for a table. */
    WAITING,
    /** At a table, order not taken yet. */
    SEATED,
    /** Order taken, food not served yet. */
    WAITING_FOR_FOOD,
    /** Food served, eating this turn. */
    SERVED,
    /** Finished eating, waiting for the Cashier. */
    READY_TO_PAY,
    /** Paid and done. */
    PAID,
    /** Satisfaction hit 0 and the customer left. */
    LEFT
}
