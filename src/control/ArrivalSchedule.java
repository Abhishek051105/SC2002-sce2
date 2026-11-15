package control;

import entity.Customer;
import entity.RegularCustomer;
import entity.VIPCustomer;
import entity.CriticCustomer;
import entity.CustomerType;

import java.util.ArrayList;
import java.util.List;

/**
 * Decides who arrives on which turn and creates those customers.
 * This is the only class that names the concrete customer subclasses,
 * so adding a new customer type only changes {@link #createCustomer}.
 *
 * @author Aaryan
 * @since 2026-10-26 (Stage 1)
 */
public class ArrivalSchedule {
    private final List<Integer> arrivalTurns;
    private final List<CustomerType> arrivalTypes;
    private int nextCustomerId = 1;

    /**
     * @param arrivalTurns the turn of each arrival, in order
     * @param arrivalTypes the type of each arrival; position i matches arrivalTurns position i
     */
    public ArrivalSchedule(List<Integer> arrivalTurns, List<CustomerType> arrivalTypes) {
        this.arrivalTurns = arrivalTurns;
        this.arrivalTypes = arrivalTypes;
    }

    /**
     * Creates the customers who arrive on the given turn. IDs are handed out
     * in arrival order (C1, C2, ...).
     *
     * @param turn the current turn
     * @return the new customers, or an empty list if nobody arrives
     */
    public List<Customer> customersArrivingAt(int turn) {
        List<Customer> list = new ArrayList<>();
        for (int i = 0; i < arrivalTurns.size(); i++) {
            if (arrivalTurns.get(i) == turn) {
                list.add(createCustomer(arrivalTypes.get(i), nextCustomerId, turn));
                nextCustomerId++;
            }
        }
        return list;
    }

    /** Builds the customer subclass that matches the type (Regular by default). */
    private Customer createCustomer(CustomerType type, int id, int turn) {
        switch (type) {
            case VIP:
                return new VIPCustomer(id, turn);
            case CRITIC:
                return new CriticCustomer(id, turn);
            default:
                return new RegularCustomer(id, turn);
        }
    }
}
