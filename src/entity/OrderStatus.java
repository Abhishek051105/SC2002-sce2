package entity;

/**
 * Where an order is: IN_PREPARATION until all prep units are cooked, then
 * READY, SERVED and PAID. CANCELLED if the customer leaves before being served.
 *
 * @author Abhishek, Aaryan
 * @since 2026-10-26 (Stage 1)
 */
public enum OrderStatus {
    IN_PREPARATION, READY, SERVED, PAID, CANCELLED
}
