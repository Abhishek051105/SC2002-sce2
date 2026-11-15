package entity;

/**
 * Stage 3 combo meal: two existing menu items sold together. It is still a
 * MenuItem, so Order, Chef and the customers use it with no changes.
 * Price is 90% of the two prices added up; prep units are added up.
 *
 * @author Abhishek
 * @since 2026-11-05 (Stage 3)
 */
public class ComboMeal extends MenuItem {
    private final MenuItem first;
    private final MenuItem second;

    /**
     * @param first  the first item in the combo
     * @param second the second item in the combo
     */
    public ComboMeal(MenuItem first, MenuItem second){
        // name, price and prep units all come from the 2 items
        // price = 90% of both added up, the +50 makes it round to the nearest cent
        super(first.getName() + " + " + second.getName(),
              ((first.getPriceCents() + second.getPriceCents()) * 90 + 50) / 100,
              first.getPrepUnits() + second.getPrepUnits());
        this.first = first;
        this.second = second;
    }

    /** @return the first item in the combo */
    public MenuItem getFirst(){
        return this.first;
    }

    /** @return the second item in the combo */
    public MenuItem getSecond(){
        return this.second;
    }

}
