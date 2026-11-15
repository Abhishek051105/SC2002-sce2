package entity;

/**
 * Stage 2 Critic customer: loses 12 satisfaction per waiting turn and
 * always orders the most expensive item on the menu. Pays full price.
 *
 * @author Aekas, Abhishek
 * @since 2026-11-01 (Stage 2)
 */
public class CriticCustomer extends Customer {

    /**
     * @param customerId  customer number (C1, C2, ...)
     * @param arrivalTurn turn the customer arrives
     */
    public CriticCustomer(int customerId, int arrivalTurn) {
        super(customerId, arrivalTurn);
    }

    /** @return 12 satisfaction lost per waiting turn */
    @Override
    public int getSatisfactionDecay() {
        return 12;
    }

    /** Always the most expensive dish; on a tie the earlier menu item wins. */
    @Override
    public MenuItem chooseMenuItem(Menu menu) {
        // same checks as Customer.chooseMenuItem(), so a Critic fails the same way
        if (menu == null) {
            throw new IllegalArgumentException("Menu cannot be null.");
        }
        if (menu.size() == 0) {
            throw new IllegalArgumentException("Menu cannot be empty.");
        }

        MenuItem mostExpensive = menu.getItem(0);
        for (int i = 1; i < menu.size(); i++) {
            if (menu.getItem(i).getPriceCents() > mostExpensive.getPriceCents()) {
                mostExpensive = menu.getItem(i);
            }
        }
        return mostExpensive;
    }

    /** @return "Critic" */
    @Override
    public String getTypeLabel() {
        return "Critic";
    }
}
