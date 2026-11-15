package entity;
import java.util.ArrayList;

/**
 * The restaurant's menu. Starts with the three base dishes (Salad, Burger,
 * Pasta); Stage 3 adds the combos on the end with addItem().
 *
 * @author Abhishek, Aaryan
 * @since 2026-10-26 (Stage 1)
 */
public class Menu {
    private ArrayList<MenuItem> items = new ArrayList<>();

    /** Creates the base menu: Salad $9.00 (1 unit), Burger $15.00 (1 unit), Pasta $18.00 (2 units). */
    public Menu(){
        // adding items into the menu

        MenuItem salad = new MenuItem("Salad",900,1);
        items.add(salad);

        MenuItem burger = new MenuItem("Burger", 1500, 1);
        items.add(burger);

        MenuItem pasta = new MenuItem("Pasta",1800,2);
        items.add(pasta);
    }

    /**
     * So new items (like combos) can be added on top of the base menu.
     *
     * @param item item added to the end of the menu
     */
    public void addItem(MenuItem item){
        items.add(item);
    }

    /** @return the item at this position (0 = Salad) */
    public MenuItem getItem(int index){
       return items.get(index);
    }
    /** @return number of items on the menu */
    public int size(){
        return items.size();
    }
}
