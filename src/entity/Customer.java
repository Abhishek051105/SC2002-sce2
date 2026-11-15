package entity;

/**
 * Base class for every customer type. Holds the visit's state (satisfaction,
 * status, turns) and the fixed order of steps: seat, order, serve, finish
 * eating, pay. Subclasses only change satisfaction decay, food choice and
 * bill, so the controller and staff never need to know the type.
 *
 * @author Raghav, Abhishek, Aekas, Aaryan
 * @since 2026-10-22 (Stage 1)
 */
public abstract class Customer {

    private final int customerId;
    private final int arrivalTurn;

    private int satisfaction;
    private CustomerStatus status;

    private int servedTurn;
    private int readyToPayTurn;
    private int paidSatisfaction;

    /**
     * New customers start WAITING in the queue with satisfaction 100.
     *
     * @param customerId  customer number (C1, C2, ...), positive
     * @param arrivalTurn turn the customer arrives, positive
     * @throws IllegalArgumentException if either value is not positive
     */
    public Customer(int customerId, int arrivalTurn) {

        if (customerId <= 0 || arrivalTurn <= 0) {
            throw new IllegalArgumentException(
                    "ID and arrival turn must be positive.");
        }

        this.customerId = customerId;
        this.arrivalTurn = arrivalTurn;

        this.satisfaction = 100;
        this.status = CustomerStatus.WAITING;

        this.servedTurn = -1;
        this.readyToPayTurn = -1;
        this.paidSatisfaction = -1;
    }

    /** @return the customer number (also used as their order number) */
    public int getCustomerId() {
        return customerId;
    }

    /** @return current satisfaction, 0 to 100 */
    public int getSatisfaction() {
        return satisfaction;
    }

    /** @return where the customer is in their visit */
    public CustomerStatus getStatus() {
        return status;
    }

    /** @return satisfaction at the moment of payment, or -1 if not paid yet */
    public int getPaidSatisfaction() {
        return paidSatisfaction;
    }

    /** @return the turn the food was served, or -1 if not served yet */
    public int getServedTurn() {
        return servedTurn;
    }

    /** @return the turn they finished eating, or -1 if not yet */
    public int getReadyToPayTurn() {
        return readyToPayTurn;
    }

    /**
     * Default deterministic food selection rule.
     * Customer 1 -> Salad
     * Customer 2 -> Burger
     * Customer 3 -> Pasta
     * Repeat...
     *
     * CriticCustomer can override this.
     */
    public MenuItem chooseMenuItem(Menu menu) {

        if (menu == null) {
            throw new IllegalArgumentException(
                    "Menu cannot be null.");
        }

        if (menu.size() == 0) {
            throw new IllegalArgumentException(
                    "Menu cannot be empty.");
        }

        int index = (customerId - 1) % menu.size();

        return menu.getItem(index);
    }

    /**
     * Full price by default. VIPCustomer overrides this with its discount.
     */
    public int calculateBill(int basePriceCents) {

        if (basePriceCents < 0) {
            throw new IllegalArgumentException(
                    "Price cannot be negative.");
        }

        return basePriceCents;
    }

    /**
     * Stage 1: RegularCustomer returns 8.
     * Stage 2:
     * VIP -> 5
     * Critic -> 12
     */
    public abstract int getSatisfactionDecay();

    /**
     * Short name the UI shows, e.g. "Regular", "VIP", "Critic".
     * Each subclass names itself, so the UI never needs instanceof.
     */
    public abstract String getTypeLabel();

    /**
     * Called once at the end of the turn.
     *
     * @return true if the customer leaves.
     */
    public final boolean decreaseSatisfaction() {

        if (status == CustomerStatus.WAITING
                || status == CustomerStatus.SEATED
                || status == CustomerStatus.WAITING_FOR_FOOD) {

            satisfaction = Math.max(
                    0,
                    satisfaction - getSatisfactionDecay());

            if (satisfaction == 0) {
                status = CustomerStatus.LEFT;
                return true;
            }
        }

        return false;
    }

    /**
     * Happy Hour recovery, at the start of the turn. Only customers still
     * waiting for a table or food (the same statuses that lose satisfaction)
     * recover, up to a maximum of 100.
     *
     * @return true if the satisfaction actually went up.
     */
    public final boolean recoverSatisfaction(int points) {

        if (status == CustomerStatus.WAITING
                || status == CustomerStatus.SEATED
                || status == CustomerStatus.WAITING_FOR_FOOD) {

            int before = satisfaction;
            satisfaction = Math.min(100, satisfaction + points);
            return satisfaction > before;
        }

        return false;
    }

    // These are final so a subclass (VIP, Critic, ...) can't change the
    // order of the steps. Subclasses only change decay, food choice and bill.

    /** WAITING -> SEATED. Throws IllegalStateException if not waiting. */
    public final void seat() {

        if (status != CustomerStatus.WAITING) {
            throw new IllegalStateException(
                    "Customer is not waiting for a table.");
        }

        status = CustomerStatus.SEATED;
    }

    /** SEATED -> WAITING_FOR_FOOD. Throws IllegalStateException if not seated. */
    public final void orderTaken() {

        if (status != CustomerStatus.SEATED) {
            throw new IllegalStateException(
                    "Customer must be seated before ordering.");
        }

        status = CustomerStatus.WAITING_FOR_FOOD;
    }

    /** WAITING_FOR_FOOD -> SERVED, and remembers the turn. */
    public final void serve(int turn) {

        if (status != CustomerStatus.WAITING_FOR_FOOD) {
            throw new IllegalStateException(
                    "Customer cannot be served yet.");
        }

        if (turn < arrivalTurn) {
            throw new IllegalStateException(
                    "Invalid serving turn.");
        }

        status = CustomerStatus.SERVED;
        servedTurn = turn;
    }

    /** SERVED -> READY_TO_PAY. Only allowed on a turn after the one they were served. */
    public final void finishEating(int turn) {

        if (status != CustomerStatus.SERVED) {
            throw new IllegalStateException(
                    "Customer has not been served.");
        }

        if (turn <= servedTurn) {
            throw new IllegalStateException(
                    "Customer cannot finish eating yet.");
        }

        status = CustomerStatus.READY_TO_PAY;
        readyToPayTurn = turn;
    }

    /**
     * READY_TO_PAY -> PAID, on a turn after they finished eating.
     * Records the satisfaction at payment for the average.
     */
    public final void markPaid(int turn) {

        if (status != CustomerStatus.READY_TO_PAY) {
            throw new IllegalStateException(
                    "Customer is not ready to pay.");
        }

        if (turn <= readyToPayTurn) {
            throw new IllegalStateException(
                    "Customer cannot pay yet.");
        }

        paidSatisfaction = satisfaction;
        status = CustomerStatus.PAID;
    }

    /** @return true if the customer is waiting for the Cashier */
    public boolean isReadyToPay() {
        return status == CustomerStatus.READY_TO_PAY;
    }

    /** @return a one-line summary, for debugging */
    @Override
    public String toString() {

        return "Customer #"
                + customerId
                + " | Satisfaction: "
                + satisfaction
                + " | Status: "
                + status;
    }
}
