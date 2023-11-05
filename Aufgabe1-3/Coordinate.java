import java.util.Objects;

/*
 *  This class represents a point on a field with x and y position
 */


//Modul/Class Coordinate
//Abstraction: real world, subtype
//this Class implements the Comparable Interface, therefore is a subtype of Comparable
//gets used in following classes: World, Field and Ant, therefore operates on a lower level of abstraction
public class Coordinate implements Comparable {
    private final int posX;
    private final int posY;
    private int posZ;


    //Class method
    /**
     * this constructor sets the objects' variables.
     * @param posX x-coordinate
     * @param posY y-coordinate
     * @param posZ z-coordinate
     */
    public Coordinate(int posX, int posY, int posZ) {
        this.posX = posX;
        this.posY = posY;
        this.posZ = posZ;
    }


    //Class method
    @Override
    /**
     * this method check if the coordinates of a given object are the same as objects' coordinates
     */
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Coordinate that = (Coordinate) o;
        return posX == that.posX && posY == that.posY;
    }


    //Class method
    @Override
    /**
     * this method creates a hash code using the objects' variables.
     */
    public int hashCode() {
        return Objects.hash(posX, posY, posZ);
    }


    //Class method
    @Override
    /**
     * this method defines how the objects' coordinates are printed
     */
    public String toString() {
        return "Coordinate{" +
                "posX=" + posX +
                ", posY=" + posY +
                ", posZ=" + posZ +
                '}';
    }


    //Class method
    /**
     * this method checks if the Coordinate is in bound of the world. If not, it will be transformed:
     * i.e. if the index is smaller than 0 it will start again from the highest index.
     * @param coordinate the initial coordinate
     * @param xLength    the horizontal length of the field
     * @param yLength    the vertical length of the field
     * @return the new Coordinate which is in bound of the specified field
     */
    public static Coordinate checkCoordinate(Coordinate coordinate, int xLength, int yLength, Field[][] fields) {
        int newY = coordinate.getPosY();
        int newX = coordinate.getPosX();

        while (newY < 0) {
            newY += yLength;
        }
        while (newY >= yLength) {
            newY -= yLength;
        }
        while (newX < 0) {
            newX += xLength;
        }
        while (newX >= xLength) {
            newX -= xLength;
        }

        return new Coordinate(newX, newY, fields[newX][newY].getHeight());
    }


    //Class method
    /**
     * this method returns the objects' x position
     * @return x coordinate
     */
    public int getPosX() {
        return posX;
    }


    //Class method
    /**
     * this method returns the objects' y position
     * @return y coordinate
     */
    public int getPosY() {
        return posY;
    }


    //Class method
    /**
     * this method returns the objects' z position
     * @return z coordinate
     */
    public int getPosZ() {
        return posZ;
    }


    //Class method
    /**
     * @param o Object which shall be compared to this Coordinate
     * @return a negative Integer if this posZ is smaller than that of the given object, a positive Integer if
     * this posZ is bigger and 0 if it is the same
     */
    @Override
    public int compareTo(Object o) {
        if (this == o) return 0;
        if (o == null || getClass() != o.getClass()) return Integer.MIN_VALUE;
        Coordinate that = (Coordinate) o;
        return Integer.compare(this.posZ, that.getPosZ());
    }
}
