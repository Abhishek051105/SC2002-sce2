package entity;

/**
 * The Chef: cooks one preparation unit of one order per turn.
 * An order is READY once all of its item's units are done.
 *
 * @author Abhishek, Aaryan
 * @since 2026-10-26 (Stage 1)
 */
public class Chef extends Staff{
     /** Creates the Chef. */
     public Chef() {
        super("Chef");
    }

    /**
     * Cooks one unit of an order.
     *
     * @param o the order to cook; must be IN_PREPARATION
     * @throws InvalidActionException if the Chef already acted, the order is missing,
     *                                or it is not being prepared
     */
    public void prepare(Order o) throws InvalidActionException {
        checkCanAct();
        if (o == null) {
            throw new InvalidActionException("That order does not exist");
        }
        if (o.getStatus() != OrderStatus.IN_PREPARATION) {
            throw new InvalidActionException("Order " + o.getOrderId() + " cannot be cooked, it is " + o.getStatus());
        }
        o.advanceOneUnit();
        markActed();   // only reached if all checks passed
    }
}

