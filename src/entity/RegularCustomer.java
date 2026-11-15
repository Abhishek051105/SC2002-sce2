package entity;

/**
 * The Stage 1 customer: loses 8 satisfaction per waiting turn, pays full
 * price and orders using the default rule in {@link Customer}.
 *
 * @author Raghav, Abhishek, Aaryan
 * @since 2026-10-22 (Stage 1)
 */
public class RegularCustomer extends Customer {
    /**
     * @param customerId  customer number (C1, C2, ...)
     * @param arrivalTurn turn the customer arrives
     */
    public RegularCustomer(int customerId, int arrivalTurn) {
        super(customerId, arrivalTurn);
    }

    /** @return 8 satisfaction lost per waiting turn */
    @Override
    public int getSatisfactionDecay() {
        return 8;
    }

    /** @return "Regular" */
    @Override
    public String getTypeLabel() {
        return "Regular";
    }
}
