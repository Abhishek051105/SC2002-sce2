package control;

import entity.GameStats;

/**
 * The victory rule. All three targets must be met at the end of the game.
 * The UI asks this class which targets passed, so the rule lives in one place.
 *
 * @author Aaryan
 * @since 2026-10-26 (Stage 1)
 */
public class Objective {
    private final int minPaidCustomers;
    private final int minRevenueCents;
    private final int minAvgSatisfaction;

    /**
     * @param minPaidCustomers   minimum number of paid customers
     * @param minRevenueCents    minimum revenue, in cents
     * @param minAvgSatisfaction minimum average satisfaction of paid customers
     */
    public Objective(int minPaidCustomers, int minRevenueCents, int minAvgSatisfaction) {
        this.minPaidCustomers = minPaidCustomers;
        this.minRevenueCents = minRevenueCents;
        this.minAvgSatisfaction = minAvgSatisfaction;
    }


    /** @return true (VICTORY) only if all three targets are met */
    public boolean isMet(GameStats stats) {
        return paidTargetMet(stats) && revenueTargetMet(stats) && satisfactionTargetMet(stats);
    }

    /** @return true if enough customers have paid */
    public boolean paidTargetMet(GameStats stats) {
        return minPaidCustomers <= stats.getPaidCount();
    }
    /** @return true if the revenue target is reached */
    public boolean revenueTargetMet(GameStats stats) {
        return minRevenueCents <= stats.getRevenueCents();
    }
    /** @return true if at least one customer paid and their average is high enough */
    public boolean satisfactionTargetMet(GameStats stats) {
        return stats.getPaidCount() > 0 && minAvgSatisfaction <= stats.getAverageSatisfaction();

    }

    /** @return the paid-customers target */
    public int getMinPaidCustomers() {
        return minPaidCustomers;
    }
    /** @return the revenue target, in cents */
    public int getMinRevenueCents() {
        return minRevenueCents;
    }
    /** @return the average-satisfaction target */
    public int getMinAvgSatisfaction() {
        return minAvgSatisfaction;
    }
}
