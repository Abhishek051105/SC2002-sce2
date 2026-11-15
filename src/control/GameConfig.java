package control;

import java.util.List;
import entity.CustomerType;

/**
 * Fixed game settings: number of turns and tables, the arrival list
 * and the victory targets. Kept as configuration so the rules in the
 * controller and entities don't hard-code these numbers.
 *
 * @author Aaryan
 * @since 2026-10-26 (Stage 1)
 */
public final class GameConfig {
    /** The game always lasts this many turns. */
    public static final int MAX_TURNS = 18;
    /** Number of tables in the restaurant. */
    public static final int NUM_TABLES = 2;

    /** Turns on which a customer arrives (one customer each). */
    public static final List<Integer> ARRIVAL_TURNS = List.of(1, 4, 7, 10, 13, 16);

    /** Customer type of each arrival (Stage 2 and 3): arrival 2 is the VIP, arrival 4 the Critic. */
    public static final List<CustomerType> ARRIVAL_TYPES = List.of(
            CustomerType.REGULAR, CustomerType.VIP, CustomerType.REGULAR,
            CustomerType.CRITIC, CustomerType.REGULAR, CustomerType.REGULAR);

    /** Stage 1 arrival list (all Regular), used by the "regular" mode. */
    public static final List<CustomerType> ALL_REGULAR = List.of(
            CustomerType.REGULAR, CustomerType.REGULAR, CustomerType.REGULAR,
            CustomerType.REGULAR, CustomerType.REGULAR, CustomerType.REGULAR);

    /** Victory target: at least this many paid customers. */
    public static final int TARGET_PAID = 4;
    /** Victory target: at least this much revenue, in cents ($45.00). */
    public static final int TARGET_REVENUE_CENTS = 4500;
    /** Victory target: at least this average satisfaction of paid customers. */
    public static final int TARGET_AVG_SATISFACTION = 60;
}
