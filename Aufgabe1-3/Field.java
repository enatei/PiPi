import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


//Modul/Class Field
//Abstraction: real world
//uses instances of FieldState, therefore operates on a higher level of abstraction.
//gets used by following classes: World and Ant, therefore operates on a lower level of abstraction.
public class Field {
    private final List<Ant> antsOnField = new ArrayList<>();
    private FieldState fieldState; // is a home or food on the field
    private Map<Ant,Integer> antScent = new HashMap<>();
    private final int scentIncrease; // amount of scent increase when ants enters field
    private final int scentIncreaseStrong; // amount of scent increase when ants enters field
    private final int maxScent = 100; // max strength of scent
    private final double scentDecrease; // the decreasing factor of scent per simulation step
    private final int height;

    //NOTE: shift from object-oriented to procedural
    private Building building; //when the field has a building like an ant home placed on it, the information is stored here


    //Class method
    /**
     * this constructor sets the objects' variables.
     * @param fieldState the starting fieldState
     * @param height the fields' hight.
     * @param scentIncrease how much the scent increases on normal conditions
     * @param scentIncreaseStrong how much the scent increases on special conditions
     * @param scentDecrease how much the scent decreases after each simulation step
     */
    public Field(FieldState fieldState, int height, int scentIncrease, int scentIncreaseStrong, double scentDecrease) {
        this.height = height;
        this.fieldState = fieldState;
        this.scentIncrease = scentIncrease;
        this.scentIncreaseStrong = scentIncreaseStrong;
        this.scentDecrease = scentDecrease;
    }


    //Class method
    /**
     * @return the fields' FieldState (NULL, HOME or FOOD)
     */
    public FieldState getFieldState() {
        return fieldState;
    }


    //Class method
    /**
     * this method sets the field state of a field to the given parameter of type FieldState.
     * @param fieldState which shall be set for the specific field
     */
    public void setFieldState(FieldState fieldState) {
        this.fieldState = fieldState;
    }


    //Class method
    /**
     * @return the building of the field FieldState (NULL, HOME or FOOD)
     */
    public Building getBuilding() {
        return building;
    }


    //Class method
    /**
     * @param building sets the building of this field
     */
    public void setBuilding(Building building) {
        this.building = building;
    }


    //Class method
    /**
     * @return the height of the field
     */
    public int getHeight() { return height; }


    //Class method
    /**
     * @return the scent for each Ant integer (>= 0 and <= 100)
     */
    public Map<Ant, Integer> getAntScent() {
        return antScent;
    }


    //Class method
    /**
     * @return the scent od a specific Ant integer (>= 0 and <= 100)
     */
    public int getScentOfAnt(Ant ant) {
        return this.antScent.getOrDefault(ant, 0);
    }


    //Class method
    /**
     * @return the total sum of all ant scents
     * */
    //NOTE: shift from object-oriented to functional programming
    public int getTotalScent() {
        return this.antScent.values().stream().reduce(0,Integer::sum);
    }


    //Class method
    /**
     * STYLE: Functional Programming (applicative programming)
     * this method is side effect free and has no direct control flow manipulation and uses only already existing functions.
     * here a shift from object-oriented to functional programming is used because it's more suitable for this kind of problem
     *
     * @return the total amount of scent for each Building (e.G AntColony) in a map key: Building value: TotalAmountOfScent
     */
    public Map<Building,Integer> getScentPerBuilding () {
        return this.antScent.entrySet().stream().collect(
                Collectors.toMap(e-> e.getKey().getAntColony(), Map.Entry::getValue, Integer::sum));
    }


    //Class method
    /**
     * @return the scent of one building
     */
    public int getScentOfBuilding(Building building) {
        return this.getScentPerBuilding().getOrDefault(building,0);
    }


    //Class method
    /**
     * @return the list antsOnField, which lists all ants that are currently on the field
     */
    public List<Ant> getAntsOnField() {
        return antsOnField;
    }


    //Class method
    /**
     * @param ant which shall be added to the antsOnField list
     */
    public void addAnt(Ant ant) {
        if (!antsOnField.contains(ant)) antsOnField.add(ant);
    }


    //Class method
    /**
     * @param ant which shall be removed from the antsOnField list
     */
    public void removeAnt(Ant ant) {
        antsOnField.remove(ant);
    }


    //Class method
    /**
     * this method increases the scent integer of the ant by the flat amount of scentIncrease.
     * @param ant which ant increases there scent
     */
    public void increaseScent(Ant ant) {
        this.increaseFieldScent(false,ant);
    }


    //Class method
    /**
     * this method increases the scent integer by the flat amount of scentIncreaseStrong
     * only when the ants' antState is CARRYING.
     */
    public void increaseScentStrong(Ant ant) {
        this.increaseFieldScent(true,ant);
    }


    //Class method
    /**
     * this method decreases the scent integer for all ants in the map by the factor scentDecrease
     * if the scent goes down to 0 it will be removed from the map
     */
    //NOTE: shift from objectoriented to functional programming
    public void decreaseScent() {
        this.antScent = this.antScent.entrySet().stream().filter(e -> (int) Math.floor(e.getValue()* scentDecrease) > 0
        ).collect(Collectors.toMap(Map.Entry::getKey, e->(int) Math.floor(e.getValue()* scentDecrease)));
    }


    //Class method
    /**
     * this method is the general IncreaseField method. it gets as input if its increased strong or not
     * and which ant is increasing it. The method is checking if the Ant is already existing or not 
     * and when not creating a new entry in the hashmap
     */
    private void increaseFieldScent (boolean strong, Ant ant) {
        int increase = strong? this.scentIncreaseStrong:this.scentIncrease;
        this.antScent.put(ant,Math.min(this.antScent.getOrDefault(ant, 0)+increase,this.maxScent));
    }
}
