package entity;

/**
 * Stage 2 VIP customer: loses only 5 satisfaction per waiting turn and
 * gets 10% off the bill. Orders the same way as a Regular customer.
 *
 * @author Raghav
 * @since 2026-10-31 (Stage 2)
 */
public class VIPCustomer extends Customer {

    /**
     * @param customerId  customer number (C1, C2, ...)
     * @param arrivalTurn turn the customer arrives
     */
    public VIPCustomer(int customerId, int arrivalTurn) {
        super(customerId, arrivalTurn);
    }

    /** @return 5 satisfaction lost per waiting turn */
    @Override
    public int getSatisfactionDecay() {
        return 5;
    }

    /**
     * 10% off the full price, in integer cents (e.g. $15.00 becomes $13.50).
     *
     * @param basePriceCents the menu price, in cents
     * @return the discounted bill, in cents
     */
    @Override
    public int calculateBill(int basePriceCents) {
        int fullPrice = super.calculateBill(basePriceCents);
        return fullPrice * 90 / 100;
    }

    /** @return "VIP" */
    @Override
    public String getTypeLabel() {
        return "VIP";
    }
}

