package entity;

/**
 * One dish on the menu: a name, a price in cents and how many Chef turns
 * (preparation units) it takes to cook. Immutable.
 *
 * @author Abhishek, Aekas, Aaryan
 * @since 2026-10-26 (Stage 1)
 */
public class MenuItem {
    private final int priceCents;
    private final String name;
    private final int prepUnits;

    /**
     * @param name       dish name, not empty
     * @param priceCents price in cents, greater than 0
     * @param prepUnits  Chef turns needed, at least 1
     * @throws IllegalArgumentException if any value is invalid
     */
    public MenuItem( String name,int priceCents, int prepUnits){
        if (name == null || name.isEmpty()){
            throw new IllegalArgumentException("Name cannot be empty");
        }

        if (priceCents <= 0 ){
            throw new IllegalArgumentException("Price must be greater than 0");
        }

        if (prepUnits < 1 ){
            throw new IllegalArgumentException("Prep units must be greater than 0");
        }
        this.priceCents = priceCents;
        this.name = name;
        this.prepUnits = prepUnits;
    }

    /** @return price in cents */
    public int getPriceCents (){
        return this.priceCents;
    }

    /** @return dish name */
    public String getName(){
        return this.name;
    }

    /** @return number of Chef turns needed to cook it */
    public int getPrepUnits(){
        return this.prepUnits;
    }

}

