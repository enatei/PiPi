import codedraw.Palette;

import java.awt.*;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * STYLE:
 * this class uses objectoriented programming. It has a strong class cohesion, as it only contains methods that are relevant for World.
 * it uses nominal abstraction and an instance of World can be used where an instance of Simulation is needed, which means that it is replaceable
 * and it contains methods from Simulation (run()). this class has a weak object coupling as most of its methods are private (except getters).
 * the class' state is changeable as the class variables are not final.
 */

//Modul/Class world,
//Abstraction: real world, subtype of Simulation
//uses instances of Field, Ant, Coordinate, HashMap,  therefore it operates on a higher level of abstraction
//implements the interface Simulation, therefore it is in a subtype relationship with Simulation
//gets used by following classes: Test and Draw, therefore it operates on a lower level of abstraction
public class World implements Simulation {
    private Ant[] ants;
    private Field[][] fields;
    //for lambda-abstraction
    private final java.util.function.BiFunction<Integer, Integer, Integer> randomCalculator =  (min, max) -> (int) (Math.random() * (max - min) + min);
    private int maxHeight;
    private boolean shortestPath;
    private Map<Field, Integer> antsShortestPaths = new HashMap<>();
    private int[] antPaths;
    private int[] dijkstraPaths;
    private Coordinate[] food;
    private Coordinate[] homes;
    private final int maxHomes;
    private final int distance;
    private int currentHomes = 1; // how many homes are currently in the world or underConstruction
    private int currentHomesBuild = 0; // how many homes are already build in the world
    private final Building[] colonies = {new Building(0, FieldState.HOME, pickColor(new Color[]{Palette.HOT_PINK, Palette.CRIMSON, new Color(255,135,141)}), Color.BLACK),
                                         new Building(1, FieldState.HOME, pickColor(new Color[]{new Color(65,102,245), new Color (0,204,255), new Color(8,146,208)}), Color.WHITE),
                                         new Building(2, FieldState.HOME, pickColor(new Color[]{new Color(204,255,0), new Color(0,250,154), new Color(0,255,0)}), Color.RED)};
    private final double homeSpawnChance;// how high is the chance a ant spawns a new home in each simulation step
    private final int numberOfAnts;
    private final int maxSpawnDistanceAnts = 10; // how far away the ants at max can be spawned from the colony

    //Class method
    /**
     * this constructor declares the array ants by using the numberOfAnts as the array-length. It places the ants' home
     * randomly within an area of fieldLength * fieldLength and positions the food sources randomly with a certain distance
     * away from home. ants are placed randomly within a certain distance from home. It creates an double array of fields with
     * fieldLength as the arrays' lengths.
     * @param fieldLength         the vertical and horizontal length
     * @param numberOfAnts        how many ants will be placed
     * @param numberOfFood        how much food will be placed
     * @param scentIncrease       how strong will the scent will be increased on normal conditions
     * @param scentIncreaseStrong how strong will the scent will be increased on special conditions
     * @param scentDecrease       how strong will the scent will be decreased on each simulation step
     * @param distance            how far away the food will be placed
     * @param maxHeight           the maximum height for fields
     * @param shortestPath        if the shortest path shall be calculated
     * @param maxHomes            how many Homes will be placed at max (it will only placed one at the start but during the Simulation some ants will start to create new homes) can be between 1 and 3
     * @param homeSpawnChance     the chance of spawning a new ant home in each simulation step (if the maximum of homes is not reached yet)
     * @param maxAntMemory        the depth of an ants memory: if it is 0, it does not remember any fields
     */
    public World(int fieldLength, int numberOfAnts, int numberOfFood, int scentIncrease, int scentIncreaseStrong, double scentDecrease, int distance, int maxHeight, boolean shortestPath, int maxHomes, double homeSpawnChance, int maxAntMemory) {

        this.distance = distance;
        //maximum field height
        this.maxHeight = maxHeight;

        this.numberOfAnts = numberOfAnts;
        Ant.setMaxMemory(Math.max(0,Math.min(maxAntMemory,100)));
        this.maxHomes = Math.max(0,Math.min(maxHomes,3)); // between 1 and 3 Colonies
        this.homeSpawnChance = homeSpawnChance;

        //set the shortest Path calculation to true;
        this.shortestPath = shortestPath;
        this.homes = new Coordinate[maxHomes];
        //random home coordinates
        this.homes[0] = new Coordinate(randomCalculator.apply(0, fieldLength), randomCalculator.apply(0, fieldLength), randomCalculator.apply(0, maxHeight + 1));
        //create firstBuilding

        ants = new Ant[numberOfAnts];
        fields = new Field[fieldLength][fieldLength];
        this.food = new Coordinate[numberOfFood];
        this.dijkstraPaths = new int[numberOfFood];
        this.antPaths = new int[numberOfFood];

        for (int x = 0; x < fieldLength; x++) {
            for (int y = 0; y < fieldLength; y++) {
                //object fields: instance of type Field
                if (new Coordinate(x, y, 1).equals(this.homes[0] )) {
                    fields[y][x] = new Field(FieldState.HOME, this.homes[0] .getPosZ(), scentIncrease, scentIncreaseStrong, scentDecrease);
                    fields[y][x].setBuilding(this.colonies[0]);
                } else {
                    int z = chooseWeightedZCoordinate(x, y, randomCalculator.apply(0, maxHeight + 1), fieldLength, maxHeight);
                    fields[y][x] = new Field(FieldState.NULL, z, scentIncrease, scentIncreaseStrong, scentDecrease);
                }
            }
        }

        for (int i = 0; i < numberOfFood; i++) {
            Coordinate foodPos;
            do {
                //object foodPos: instance of type Coordinate
                foodPos = getNewFoodCoordinate(fieldLength, this.homes[0] , distance);
            } while (Arrays.asList(food).contains(foodPos));
            food[i] = foodPos;
        }
        this.createAnts(0);

        for (Coordinate foodPos : food) {
            fields[foodPos.getPosY()][foodPos.getPosX()].setFieldState(FieldState.FOOD);
            antsShortestPaths.put(fields[foodPos.getPosY()][foodPos.getPosX()], Integer.MAX_VALUE); //antsShortestPaths are max values at the beginning
        }

        if(shortestPath) {
            for(int i = 0; i < numberOfFood; i++) {
                //Note: Dijkstra is using the procedural paradigma. It takes parameters from the objectoriented part and returns the shortest path.
                dijkstraPaths[i] = Dijkstra.shortestPath(food[i], this.homes[0], fields);
                antPaths[i] = Integer.MAX_VALUE;
            }
        }
    }


    //Class method
    /**
     * This method create new Ants for a given antColony
     * @param colonyId id of the colony for which the ants will be created
     */
    public void createAnts(int colonyId) {
        int minY = this.homes[colonyId].getPosY() - Math.min(distance,maxSpawnDistanceAnts);
        int maxY = this.homes[colonyId].getPosY() + Math.min(distance,maxSpawnDistanceAnts);
        int minX = this.homes[colonyId].getPosX() - Math.min(distance,maxSpawnDistanceAnts);
        int maxX = this.homes[colonyId].getPosX() + Math.min(distance,maxSpawnDistanceAnts);
        int numberOfAntsPerColony = (int) Math.floor((double)(this.numberOfAnts)/this.maxHomes);
        for (int i = numberOfAntsPerColony*(this.currentHomesBuild); i < numberOfAntsPerColony*(this.currentHomesBuild+1); i++) {
            int yCoordinateAnts = randomCalculator.apply(minY, maxY);
            int xCoordinateAnts = randomCalculator.apply(minX, maxX);
            //object antsPos: instance of type Coordinate
            ants[i] = new Ant(Coordinate.checkCoordinate(new Coordinate(xCoordinateAnts, yCoordinateAnts, 1), fields.length, fields.length, this.fields),
                    this, antsShortestPaths,this.colonies[colonyId]);
        }
        this.currentHomesBuild++;
    }


    //Class method
    /**
     * @return a double array of fields of type Field
     */
    public Field[][] getFields() {
        return this.fields;
    }


    //Class method
    /**
     * this method decreases the scent on each field, spawns new ant buildings if the maximum of homes has not been reached
     * and moves the ants to a new field.
     * @return the boolean true
     */
    @Override
    public boolean run() {
            if (this.currentHomes< this.maxHomes && Math.random()<this.homeSpawnChance) {
                this.currentHomes++;
                Ant ant = Arrays.stream(ants).filter(a -> a.getAntColony().id ==0).findFirst().orElse(null);
                if(ant != null) {
                        Coordinate homeCoordinate;
                        do {
                            homeCoordinate = this.getNewFoodCoordinate(this.fields.length, this.homes[0],this.distance);
                        } while(this.fields[homeCoordinate.getPosY()][homeCoordinate.getPosX()].getFieldState()!=FieldState.NULL);
                    this.homes[this.currentHomes-1] = homeCoordinate;
                    ant.createNewColony(this.colonies[this.currentHomes-1],homeCoordinate);
                }
            }
            for (Field[] fieldRow : this.fields) {
                for (Field field : fieldRow) {
                    field.decreaseScent();
                }
            }
            for (Ant ant : ants) {
                if(ant != null) {
                    //at first not all ants are existing
                    ant.move();
                }
            }
            return true;
    }


    //Class method
    /**
     * @return the maximum height a field can have in this world
     */
    public int getMaxHeight() { return this.maxHeight; }


    //Class method
    /**
     * @return a boolean if the shortestPath() will be simulated
     */
    public boolean isShortestPath() { return this.shortestPath; };


    //Class method
    /**
     * @return the class' food array which holds the positions of food fields
     */
    public Coordinate[] foods() { return this.food; };


    //Class method
    /**
     * @return an array which holds the dijkstra paths for each food field
     */
    public int[] getDijkstraPaths() { return this.dijkstraPaths; };


    //Class method
    /**
     * @return a map which holds the ants' discovered shortest paths for each food field
     */
    public Map getAntsShortestPath() { return this.antsShortestPaths; };


    //Class method
    /**
     * @return the home coordinates
     */
    public Coordinate getHome() { return this.homes[0]; };


    //Class method
    /**
     * this method returns a coordinate for food if it is placed a certain distance away from home.
     * @param fieldLength    horizontal and vertical length of the field
     * @param homeCoordinate coordinate where home is placed
     * @param distance       how far away the food shall be placed
     * @return random coordinate of food with a specified minimal distance
     */
    private Coordinate getNewFoodCoordinate(int fieldLength, Coordinate homeCoordinate, int distance) {
        int yCoordinateFood = randomCalculator.apply(0, fieldLength);

        while (yCoordinateFood < (distance + homeCoordinate.getPosY()) && yCoordinateFood > (homeCoordinate.getPosY() - distance)) {
            yCoordinateFood += distance;
        }

        int xCoordinateFood = randomCalculator.apply(0, fieldLength);

        while (xCoordinateFood < (distance + homeCoordinate.getPosX()) && xCoordinateFood > (homeCoordinate.getPosX() - distance)) {
            xCoordinateFood += distance;
        }
        //returns object of type Coordinate
        return Coordinate.checkCoordinate(new Coordinate(xCoordinateFood, yCoordinateFood, 1), fieldLength, fieldLength, fields);
    }


    //Class method
    /**
     * this method calculates a Z-Coordinate (height) by using the neighboring fields' height.
     * This way it is much more likely that the field will have same height as most of it's neighboring fields than a different value.
     * @param x the current x position
     * @param y the current y position
     * @param currentZ the current z position
     * @param fieldLength the field lenght of the world
     * @return a weighted integer for z, depending on the neighboring fields
     */
    private int chooseWeightedZCoordinate(int x, int y, int currentZ, int fieldLength, int maxHeight) {
        int[] neighborsZ = new int[maxHeight +1];
        int total = 0;

        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = -1; dy <= 1; dy++) {
                if (dx == 0 && dy == 0) {
                    continue;
                }
                int posX = (x + dx + fieldLength) % fieldLength;
                int posY = (y + dy + fieldLength) % fieldLength;

                if (posX >= 0 && posX < fieldLength && posY >= 0 && posY < fieldLength) {
                    if (fields[posY][posX] != null) {
                        neighborsZ[fields[posY][posX].getHeight()]++;
                        total += fields[posY][posX].getHeight();
                    }
                }
            }
        }

        if (total == 0) {
            return currentZ;
        }

        int randomValue = randomCalculator.apply(0, total + 1);
        int cumulativeWeight = 0;

        for (int i = 0; i < neighborsZ.length; i++) {
            cumulativeWeight += neighborsZ[i];
            if (randomValue < cumulativeWeight) {
                return i;
            }
        }

        return 0;
    }

    private Color pickColor(Color[] colors) {
        return colors[(int)(Math.random()*colors.length)];
    }
}
