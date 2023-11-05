//Enumeration Direction
//this defines the directions an ant could possibly take: north, northeast, east, southeast, south, southwest, west, northwest.
public enum Direction {
    NORTH,
    NORTHEAST,
    EAST,
    SOUTHEAST,
    SOUTH,
    SOUTHWEST,
    WEST,
    NORTHWEST;

    //class variable, saves enum-values into an array which will be used in the ant-class
    private static final Direction[] directions = values();


    //Module method
    /**
     * @return a random direction which will be used in the ant-class
     */
    public static Direction randomDirection() {
        return directions[(int) Math.floor(Math.random() * directions.length)];
    }


    //Module method
    /**
     * @param currentDirection the direction we are currently facing
     * @return the opposite direction of the current direction
     */
    public static Direction getOppositeDirection(Direction currentDirection) {
        int newIndex = (currentDirection.ordinal()+(values().length/2))%values().length;
        return directions[newIndex];
    }
}
