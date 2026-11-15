package entity;

/**
 * Normal pricing: the bill is unchanged.
 *
 * @author Aekas
 * @since 2026-11-06 (Stage 3)
 */
public class StandardPricing implements PricingPolicy {
    /** @return the same bill, unchanged */
    @Override
    public int applyTo(int billCents) {
        return billCents;
    }
}
