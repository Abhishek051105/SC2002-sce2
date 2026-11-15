package entity;

import java.util.ArrayList;
import java.util.List;

/**
 * Running totals for the game: revenue, paid customers and their
 * satisfaction, dishes served and unhappy departures.
 *
 * @author Aekas, Aaryan
 * @since 2026-10-26 (Stage 1)
 */
public class GameStats {
    private int revenueCents;
    private final List<Integer> paidSatisfactions = new ArrayList<>();
    private int servedCount;
    private int unhappyDepartures;

    /** Satisfaction is recorded at the moment of payment. */
    public void recordPayment(int billCents, int satisfaction) {
        revenueCents += billCents;
        paidSatisfactions.add(satisfaction);
    }

    /** Counts one dish served. */
    public void recordServed() {
        servedCount++;
    }

    /** Counts one customer who left unhappy. */
    public void recordDeparture() {
        unhappyDepartures++;
    }

    /** @return total revenue, in cents */
    public int getRevenueCents() {
        return revenueCents;
    }

    /** @return number of customers who have paid */
    public int getPaidCount() {
        return paidSatisfactions.size();
    }

    /** @return number of dishes served */
    public int getServedCount() {
        return servedCount;
    }

    /** @return number of customers who left unhappy */
    public int getUnhappyDepartures() {
        return unhappyDepartures;
    }

    /** Average satisfaction of paid customers. Check getPaidCount() > 0 first (the UI shows N/A). */
    public double getAverageSatisfaction() {
        int total = 0;
        for (int s : paidSatisfactions) {
            total += s;
        }
        return (double) total / paidSatisfactions.size();
    }
}
