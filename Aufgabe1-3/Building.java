import java.awt.*;

/**
 * STYLE: Procedural programming
 * this class is used as a record that can store the relevant information for the building
 * this class has no methods (except a constructor)
 * all variables are public therefore no getters or setters are needed
 */

//Class/module building
public class Building {

    public int id;
    //which type the building is e.G Home of ants
    public FieldState buildingType;
    //which Color does the scent of the Ants have that belongs to this building
    public Color scentColor;
    //which Color do the Ants have that belongs to this building
    public Color antColor;

    public Building(int id, FieldState buildingType, Color scentColor, Color antColor) {
        this.id = id;
        this.buildingType = buildingType;
        this.scentColor = scentColor;
        this.antColor = antColor;
    }

}
