package entity;


/**
 * One customer's order. The bill is worked out once, when the order is
 * taken, and locked in. Tracks how many prep units the Chef has cooked.
 *
 * @author Abhishek, Aekas, Aaryan
 * @since 2026-10-26 (Stage 1)
 */
public class Order {
    private final int orderId;
    private final Customer customer;
    private final MenuItem item;
    private final int billCents;
    private int unitsDone;
    private OrderStatus status;

    /**
     * New orders start IN_PREPARATION with no units cooked.
     *
     * @param orderId   order number (same as the customer number)
     * @param customer  who ordered
     * @param item      what they ordered
     * @param billCents the locked bill, in cents
     */
    public Order(int orderId, Customer customer, MenuItem item, int billCents) {
        this.orderId = orderId;
        this.customer = customer;
        this.item = item;
        this.billCents = billCents;
        this.unitsDone = 0;
        this.status = OrderStatus.IN_PREPARATION;
    }
       /** Cooks one unit; becomes READY once all of the item's units are done. */
       public void advanceOneUnit() {
        if (status != OrderStatus.IN_PREPARATION) {
            throw new IllegalStateException("Order " + orderId + " is not being prepared");
        }
        unitsDone = unitsDone + 1;
        if (unitsDone == item.getPrepUnits()) {
            status = OrderStatus.READY;
        }
    }
 
    /** READY -> SERVED. */
    public void markServed() {
        if (status != OrderStatus.READY) {
            throw new IllegalStateException("Order " + orderId + " is not ready");
        }
        status = OrderStatus.SERVED;
    }
 
    /** SERVED -> PAID. */
    public void markPaid() {
        if (status != OrderStatus.SERVED) {
            throw new IllegalStateException("Order " + orderId + " has not been served");
        }
        status = OrderStatus.PAID;
    }
 
    /** Customer left before getting their food. Not allowed once served. */
    public void cancel() {
        if (status == OrderStatus.SERVED || status == OrderStatus.PAID) {
            throw new IllegalStateException("Order " + orderId + " was already served");
        }
        status = OrderStatus.CANCELLED;
    }
 
    /** @return true if the dish is cooked and waiting to be served */
    public boolean isReady() {
        return status == OrderStatus.READY;
    }

    /** @return the order number (same as the customer number) */
    public int getOrderId() {
        return orderId;
    }

    /** @return who ordered */
    public Customer getCustomer() {
        return customer;
    }

    /** @return what was ordered */
    public MenuItem getItem() {
        return item;
    }

    /** @return the locked bill, in cents */
    public int getBillCents() {
        return billCents;
    }

    /** @return prep units cooked so far */
    public int getUnitsDone() {
        return unitsDone;
    }

    /** @return the order's current status */
    public OrderStatus getStatus() {
        return status;
    }
}
