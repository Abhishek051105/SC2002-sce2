package entity;

/**
 * A pricing rule applied to the bill when an order is taken.
 * The Waiter depends only on this interface, so a new promotion can be
 * added without changing the Waiter.
 *
 * @author Aekas
 * @since 2026-11-06 (Stage 3)
 */
public interface PricingPolicy {
    /** Returns the bill after this rule, in cents. */
    int applyTo(int billCents);
}
