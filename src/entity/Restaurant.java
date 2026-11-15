package entity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Holds the game state: tables, queue, customers, orders, menu, staff and
 * stats. Also does the clean-up when a customer leaves (customerLeft).
 *
 * @author Aaryan, Abhishek, Aekas
 * @since 2026-10-26 (Stage 1)
 */
public class Restaurant {
    private final List<Table> tables = new ArrayList<>();
    private final List<Customer> waitingQueue = new ArrayList<>();
    private final List<Customer> allCustomers = new ArrayList<>();
    private final List<Order> allOrders = new ArrayList<>();
    private final Menu menu;
    private final Waiter waiter;
    private final Host host;
    private final Chef chef;
    private final Cashier cashier;
    private final GameStats stats;

    /** Builds numTables tables, the default menu, the staff and stats. */
    public Restaurant(int numTables) {
        for (int i = 0; i < numTables; i++) {
            tables.add(new Table(i + 1));
        }
        menu = new Menu();
        waiter = new Waiter();
        host = new Host();
        chef = new Chef();
        cashier = new Cashier();
        stats = new GameStats();
    }

    /** New arrival joins the queue and the all-customers list. */
    public void addArrival(Customer c) {
        waitingQueue.add(c);
        allCustomers.add(c);
    }

    /** Takes a customer out of the queue (once seated, or when they leave). */
    public void removeFromQueue(Customer c) {
        waitingQueue.remove(c);
    }

    /**
     * Clean-up when a customer gets fed up and leaves:
     * take them out of the queue, cancel their order and free their table.
     * A leaving customer was never served, so cancel() is always allowed.
     */
    public void customerLeft(Customer c) {
        removeFromQueue(c);

        Order o = findOrder(c.getCustomerId());
        if (o != null) {
            o.cancel();
        }

        Table t = findTableOf(c);
        if (t != null) {
            t.release();
        }
    }

    /** @return the queue in arrival order (read-only) */
    public List<Customer> getWaitingQueue() {
        return Collections.unmodifiableList(waitingQueue);
    }

    /** @return every customer who has arrived, including paid and departed (read-only) */
    public List<Customer> getAllCustomers() {
        return Collections.unmodifiableList(allCustomers);
    }

    /** @return the tables (read-only) */
    public List<Table> getTables() {
        return Collections.unmodifiableList(tables);
    }

    /** @return all staff, so the controller can reset them at the start of each turn */
    public List<Staff> getAllStaff() {
        return List.of(waiter, host, chef, cashier);
    }

    /** null if not found (matches getCustomerId()). */
    public Customer findCustomer(int id) {
        for (Customer c : allCustomers) {
            if (c.getCustomerId() == id) {
                return c;
            }
        }
        return null;
    }

    /** null if not found. Order id == customer id. */
    public Order findOrder(int orderId) {
        for (Order o : allOrders) {
            if (o.getOrderId() == orderId) {
                return o;
            }
        }
        return null;
    }

    /** 1-based; null if out of range. */
    public Table getTable(int tableNo) {
        if (tableNo < 1 || tableNo > tables.size()) {
            return null;
        }
        return tables.get(tableNo - 1);
    }

    /** The table this customer sits at, or null. */
    public Table findTableOf(Customer c) {
        for (Table t : tables) {
            if (t.getOccupant() == c) {
                return t;
            }
        }
        return null;
    }

    /** Stores a newly taken order. */
    public void addOrder(Order o) {
        allOrders.add(o);
    }

    /** @return the menu */
    public Menu getMenu() {
        return menu;
    }

    /** @return the Waiter */
    public Waiter getWaiter() {
        return waiter;
    }

    /** @return the Host (Stage 3) */
    public Host getHost() {
        return host;
    }

    /** @return the Chef */
    public Chef getChef() {
        return chef;
    }

    /** @return the Cashier */
    public Cashier getCashier() {
        return cashier;
    }

    /** @return the game's running totals */
    public GameStats getStats() {
        return stats;
    }
}
