package entity;

/**
 * Formats money. All amounts in the game are stored as integer cents so
 * there are no rounding errors; this class only turns them into "$9.00" text.
 *
 * @author Abhishek, Aekas, Aaryan
 * @since 2026-10-26 (Stage 1)
 */
public final class Money { // bc we dont want anything to extend

    /**
     * @param cents an amount in cents, e.g. 1350
     * @return the amount as dollars with 2 decimals, e.g. "$13.50"
     */
    public static String format(int cents){
        int dollar = cents / 100;
        int remainder = cents % 100;
        if (remainder <10){
            return ("$"+dollar+"."+"0"+remainder);
        }
        return ("$"+dollar+"."+remainder);

    }

}