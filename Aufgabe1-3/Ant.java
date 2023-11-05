import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Style: Object oriented.
 * uses nominal world abstraction and has a strong class cohesion and weak object coupling.
 * The state of an ant will be changed regularly (e.g position or moving state)
 */

/*
    The ant gets a start position, the fields of the world and a random starting direction.
    It can move depending on the direction (north, northeast, east, southeast, south, southwest, west and northwest)
    and the current antstate (exploring, searching and carrying).
    It will only choose relevant neighbour fields, depending on the direction of the ant.
    If the ant would move out of the field, it will move to the opposing edge (e.G from position top left moving northwest would lead
    to position bottom right).
    In general, the ant can only move left, half left, forward, half right and right.
    And when moving on a new field, the field increases its scent.
    exploring: if there is a strong scent (>= the scentThreshhold) on a relevant neighbour field
               or it finds an ant in carrying-state on that field the ant will switch the state to searching.
               if an ant is on field with food it will change the state to carrying.
               when none of these cases are true, the ant will move to a random allowed location (according to move restrictions)
    searching: The ant will move to random relevant neighbour fields with strong scent. If no strong scent is available it will move
               to any random relevant neighbour field, but when the ant detects only a weak scent (< scentThreshold) 3 times in a row
               it will change the state back to searching.
               If an ant is on a field with food it changes the state to carrying.
    carrying: The ant will move to a random relevant neighbour field with strong scent.
              If no strong scent is available it will move to any random relevant neighbour.
              If the ant reaches the home field it will switch the state back to searching.
    The Ant will start int the state exploring.
 */


//Modul/Class Ant
//Abstraction: real world
//uses instances/references of Field, Coordinate, Direction, HashMap and AntState therefore operates on a higher level of abstraction
//gets used in class World, therefore operates on a lower level of abstraction
public class Ant {
    // the current postion of the ant
    private Coordinate position;
    // the current direction the ant is moving
    private Direction direction;
    //the current state of the ant
    private AntState antstate = AntState.EXPLORING;
    //counter to checking how many weak scent field the ant moved in a row in state searching
    private int noStrongScentCtr = 0;
    // the whole world
    private final World world;
    // Chance multiplier for Strong scent field to take this field in Search or carry
    private static final int strongScentMultiplier = 3;
    // Chance multiplier for the field in the current direction to take this field in Search or carry
    private static final int directionMultiplier = 2;
    // constant when the field is counted as strong scent
    private static final int scentThreshhold = 51; // threshhold for scent. when scent on field is below threshhold it is a weak scent
    private double difference;
    private final Map<Field, Integer> shortestPaths;
    private int pathCount;
    private boolean counting;
    private Field foodField;
    //NOTE: shift from object-oriented to procedural
    private Building antColony;
    private Coordinate newHomePostion;
    private Map<Coordinate,Integer> usedFields = new HashMap<>(); // stores how often a path was taken

    private static int maxMemory = 50; // how strong a field can be memorized


    //Class method
    /**
     * this constructor sets the ants' variables.
     * @param position start position of the ant
     * @param world reference of the world
     */
    public Ant(Coordinate position, World world, Map<Field, Integer> shortestPaths, Building antColony) {
        this.position = position;
        this.world = world;
        this.direction = Direction.randomDirection();
        this.difference = 0;
        this.counting = false;
        this.shortestPaths = shortestPaths;
        this.pathCount = Integer.MIN_VALUE;
        this.antColony = antColony;
    }


    //Class method
    /**
     * this method moves the ant to the next field according to the current antState.
     * When the ant moves it will increase the new fields' scent and updates the antsOnField list of the old and new field.
     */
    public void move() {

        if(difference > 0) {
            difference -= 1;
            return;
        }

        Coordinate[] relevantFieldPositions = this.getRelevantFieldCoordinates();
        Coordinate newCoordinate;
        List<Coordinate> coordinatesStrongScent = new ArrayList<>();
        List<Coordinate> coordinatesWeakScent = new ArrayList<>();
        switch (this.antstate) {
            case CARRYING:
                if (this.getCurrentField().getFieldState() == FieldState.HOME && this.getCurrentField().getBuilding().id == this.antColony.id) {
                    if(this.antColony.id == 0) {
                        if(pathCount < shortestPaths.get(foodField)) {
                            shortestPaths.put(foodField, pathCount);
                        }
                        pathCount = Integer.MIN_VALUE;
                        counting = false;
                    }
                    counting = false;
                    this.antstate = AntState.SEARCHING;
                    turnAround();
                    return;
                }
                
                Coordinate homeField = getHomefield(relevantFieldPositions);

                if (homeField != null) {
                    newCoordinate = homeField;
                } else {
                    newCoordinate = this.chooseRandomCoordinateWithScentWeight(relevantFieldPositions);
                }

                changeField(newCoordinate, position);
                break;

            case SEARCHING:
                if (this.getCurrentField().getFieldState() == FieldState.FOOD) {
                    this.antstate = AntState.CARRYING;
                    this.turnAround();
                    if(this.getAntColony().id == 0) {
                        this.foodField = this.getCurrentField();
                        this.counting = true;
                        this.pathCount = 0;
                    }
                    return;
                }

                Coordinate foodField = getRandomNeighbourFieldOfState(relevantFieldPositions, FieldState.FOOD);

                if (foodField != null) {
                    newCoordinate = foodField;
                } else {
                    this.splitScent(relevantFieldPositions, coordinatesStrongScent, coordinatesWeakScent);

                    if (coordinatesStrongScent.isEmpty()) {
                        noStrongScentCtr++;

                        if (noStrongScentCtr >= 3) {
                            noStrongScentCtr = 0;
                            this.antstate = AntState.EXPLORING; // switch to exploring but still making the random step.
                        }

                    } else {
                        noStrongScentCtr = 0; // resetting the counter because a strong scent was found.
                    }

                    newCoordinate = this.chooseRandomCoordinateWithScentWeight(relevantFieldPositions);
                }

                changeField(newCoordinate, position);
                break;

            case EXPLORING:
                if (this.getCurrentField().getFieldState() == FieldState.FOOD) {
                    this.antstate = AntState.CARRYING;
                    this.turnAround();
                    if(this.getAntColony().id == 0) {
                        this.foodField = this.getCurrentField();
                        this.counting = true;
                        this.pathCount = 0;
                    }
                    return;
                }

                // check if ant with food is on the same field or a strong scent on a neighbour field
                for (Ant ant : this.getCurrentField().getAntsOnField()) {
                    if (ant.getAntState() == AntState.CARRYING) {
                        this.antstate = AntState.SEARCHING;
                        this.noStrongScentCtr = 0;
                        return;
                    }
                }

                for (Coordinate coordinate : relevantFieldPositions) {
                    if (this.getEffectiveScent(coordinate) >= scentThreshhold) {
                        this.antstate = AntState.SEARCHING;
                        this.noStrongScentCtr = 0;
                        return;
                    }
                }

                newCoordinate = relevantFieldPositions[(int) Math.floor(Math.random() * relevantFieldPositions.length)]; // select a random field of the array
                changeField(newCoordinate, position);
                break;
            case CREATINGHOME:
                if(this.position.equals(this.newHomePostion)) {
                    this.createColony();
                    this.newHomePostion = null;
                    this.antstate = AntState.SEARCHING;
                } else {
                    assert this.newHomePostion != null;
                    newCoordinate = this.getNextFieldToPosition(this.newHomePostion);
                    changeField(newCoordinate, position);
                }
                break;
        }
    }


    //Class method
    /**
     * @return current AntState of the ant
     */
    public AntState getAntState() {
        return antstate;
    }


    //Class method
    /**
     * @return current getAntColony the ant belongs to
     */
    public Building getAntColony() {
        return antColony;
    }


    //Class method
    /**
     * @param antColony  change the colony the ant belongs to
     */
    public void setAntColony(Building antColony) {
        this.antColony = antColony;
    }


    //Class method
    /**
     *
     * @param newAntColony the information of the new Colony
     * @param newColonyPosition the position where it has to be placed
     */
    public void createNewColony(Building newAntColony, Coordinate newColonyPosition) {
        this.setAntColony(newAntColony);
        this.newHomePostion=newColonyPosition;
        this.antstate=AntState.CREATINGHOME;
    }


    //Class method
    /**
     * this method creates the new colony on the field where the ant is currently on
     */
    private void createColony () {
        this.getCurrentField().setBuilding(this.antColony);
        this.getCurrentField().setFieldState(FieldState.HOME);
        this.world.createAnts(this.antColony.id);
    }


    //Module method
    /**
     * This is a module variable because all ants shall have the same memory limit
     * @param maxMemory sets the maximal Memory for each field
     */
    public static void setMaxMemory(int maxMemory) {
        Ant.maxMemory = maxMemory;
    }


    //Class method
    /**
     * this method checks the given coordinates and splits them up into fields with strong and weak scent
     * @param coordinates the list of relevant coordinates
     * @param coordinatesStrongScent a list for coordinates with strong scent
     * @param coordinatesWeakScent a list for coordinates with weak/no scent
     */
    private void splitScent(Coordinate[] coordinates, List<Coordinate> coordinatesStrongScent, List<Coordinate> coordinatesWeakScent) {
        for (Coordinate coordinate : coordinates) {
            if (this.getEffectiveScent(coordinate) >= scentThreshhold) {
                coordinatesStrongScent.add(coordinate);
            } else {
                coordinatesWeakScent.add(coordinate);
            }
        }
    }


    //Class method
    /**
     * @param coordinate the field where we want to get the relevant scent
     * @return the effective Scent which is when in Mode Searching (Scent from own Colony (except own) - scent from other Colonies)
     *         in CARRYING the effective Scent is (Scent from own Colony (with own scent) - scent from other Colonies)
     */
    private int getEffectiveScent(Coordinate coordinate) {
        int totalScent = this.world.getFields()[coordinate.getPosY()][coordinate.getPosX()].getTotalScent();
        int colonyScent = this.world.getFields()[coordinate.getPosY()][coordinate.getPosX()].getScentOfBuilding(this.antColony);
        int ownScent = this.world.getFields()[coordinate.getPosY()][coordinate.getPosX()].getScentOfAnt(this);
        return switch (this.antstate) {
            case CARRYING -> (colonyScent ) - (totalScent - colonyScent);
            case SEARCHING, EXPLORING -> (colonyScent - ownScent) - (totalScent - colonyScent);
            default -> 0;
        };
    }


    //Class method
    /**
     * this method updates the position and direction of the ant according to the new field and updates the antsOnField list
     * on the old and the new field
     * If the height between the two fields is different, the double integer difference gets added the hypotenuse value
     * If the shortest path shall be calculated, pathCount gets the difference value added
     * @param newCoordinate the coordinates of the new field
     * @param oldCoordinate the coordinates of the old field
     */
    private void changeField(Coordinate newCoordinate, Coordinate oldCoordinate) {
        Field newField = getFieldFromCoordinate(newCoordinate);
        this.memorizeField(newCoordinate);
        this.getCurrentField().removeAnt(this);
        this.direction = getNewDirection(newCoordinate);
        this.position = newCoordinate;

        newField.addAnt(this);

        if (this.antstate == AntState.CARRYING) {
            newField.increaseScentStrong(this);
        } else {
            newField.increaseScent(this);
        }
        difference = Math.sqrt(1 + Math.pow(getFieldFromCoordinate(oldCoordinate).getHeight() - getFieldFromCoordinate(newCoordinate).getHeight(), 2));
        if(counting) {
            pathCount += (int) Math.ceil(difference);
            if(pathCount == Integer.MAX_VALUE || pathCount < 0) {
                counting = false;
                pathCount = Integer.MIN_VALUE;
            }
        }
    }


    //Class method
    /**
     * This method memorize the field the ant is moving on (which will increase the chance of  taking the field again)
     * when the has reached the maximum amount of memorizing it will decrease the memorize value of each other field
     * @param coordinate the coordinate to memorize
     */
    private void memorizeField(Coordinate coordinate) {
        int count = usedFields.getOrDefault(coordinate,0)+1;
        if (count > maxMemory) {
            count = maxMemory;
            //reduce other fields the current field wont be filtered out because it will overwrite the value at the end
            // of the method
            usedFields= usedFields.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, e->Math.max(0,e.getValue()-1)));
        }
        usedFields.put(coordinate,count);
    }


    //Class method
    /**
     * @return the current field of the ant
     */
    private Field getCurrentField() {
        return this.getFieldFromCoordinate(this.position);
    }


    //Class method
    /**
     * @param coordinate the coordinates of the wanted field
     * @return field of the given coordinates
     */
    private Field getFieldFromCoordinate(Coordinate coordinate) {
        return this.world.getFields()[coordinate.getPosY()][coordinate.getPosX()];
    }


    //Class method
    /**
     * this method returns the new direction the ant is facing when moving to a new field
     * @param newCoordinate the coordinates the ant wants to move to
     * @return the direction the ant faces when moving to the new field
     */
    private Direction getNewDirection(Coordinate newCoordinate) {
        if (this.position.equals(newCoordinate)) return this.direction; // case shouldn't be possible

        if (this.position.getPosY() == newCoordinate.getPosY()) {
            // East or West
            if ((this.position.getPosX() > newCoordinate.getPosX() ||
                    this.position.getPosX() == 0 && newCoordinate.getPosX() == this.world.getFields()[this.position.getPosY()].length - 1) &&
                    (this.position.getPosX() != this.world.getFields()[this.position.getPosY()].length - 1 || newCoordinate.getPosX() != 0)) {
                return Direction.WEST;
            }

            return Direction.EAST;
        } else if ((this.position.getPosY() > newCoordinate.getPosY() ||
                this.position.getPosY() == 0 && newCoordinate.getPosY() == this.world.getFields().length - 1) &&
                (this.position.getPosY() != this.world.getFields().length - 1 || newCoordinate.getPosY() != 0)) {
            // northwest north or northeast
            if (this.position.getPosX() == newCoordinate.getPosX()) return Direction.NORTH;

            if ((this.position.getPosX() > newCoordinate.getPosX() ||
                    this.position.getPosX() == 0 && newCoordinate.getPosX() == this.world.getFields()[this.position.getPosY()].length - 1) &&
                    (this.position.getPosX() != this.world.getFields()[this.position.getPosY()].length - 1 || newCoordinate.getPosX() != 0)) {
                return Direction.NORTHWEST;
            }

            return Direction.NORTHEAST;
        } else {
            // southeast south or southwest
            if (this.position.getPosX() == newCoordinate.getPosX()) return Direction.SOUTH;

            if ((this.position.getPosX() > newCoordinate.getPosX() ||
                    this.position.getPosX() == 0 && newCoordinate.getPosX() == this.world.getFields()[this.position.getPosY()].length - 1) &&
                    (this.position.getPosX() != this.world.getFields()[this.position.getPosY()].length - 1 || newCoordinate.getPosX() != 0)) {
                return Direction.SOUTHWEST;
            }

            return Direction.SOUTHEAST;
        }
    }


    //Class method
    /**
     * this method returns an array of relevant neighbouring coordinates depending on the direction of the ant
     * @return an array of coordinates of all relevant neighbouring fields
     */
    private Coordinate[] getRelevantFieldCoordinates() {
        Coordinate[] relevantCoordinate = new Coordinate[5];

        //objects/instances of type Coordinate
        switch (this.direction) {
            case NORTH:
                relevantCoordinate[0] = checkCoordinate(new Coordinate(this.position.getPosX() - 1, this.position.getPosY(), 0));
                relevantCoordinate[1] = checkCoordinate(new Coordinate(this.position.getPosX() - 1, this.position.getPosY() - 1, 0));
                relevantCoordinate[2] = checkCoordinate(new Coordinate(this.position.getPosX(), this.position.getPosY() - 1, 0));
                relevantCoordinate[3] = checkCoordinate(new Coordinate(this.position.getPosX() + 1, this.position.getPosY() - 1, 0));
                relevantCoordinate[4] = checkCoordinate(new Coordinate(this.position.getPosX() + 1, this.position.getPosY(), 0));
                break;

            case NORTHEAST:
                relevantCoordinate[0] = checkCoordinate(new Coordinate(this.position.getPosX() - 1, this.position.getPosY() - 1, 0));
                relevantCoordinate[1] = checkCoordinate(new Coordinate(this.position.getPosX(), this.position.getPosY() - 1, 0));
                relevantCoordinate[2] = checkCoordinate(new Coordinate(this.position.getPosX() + 1, this.position.getPosY() - 1, 0));
                relevantCoordinate[3] = checkCoordinate(new Coordinate(this.position.getPosX() + 1, this.position.getPosY(), 0));
                relevantCoordinate[4] = checkCoordinate(new Coordinate(this.position.getPosX() + 1, this.position.getPosY() + 1, 0));
                break;

            case EAST:
                relevantCoordinate[0] = checkCoordinate(new Coordinate(this.position.getPosX(), this.position.getPosY() - 1, 0));
                relevantCoordinate[1] = checkCoordinate(new Coordinate(this.position.getPosX() + 1, this.position.getPosY() - 1, 0));
                relevantCoordinate[2] = checkCoordinate(new Coordinate(this.position.getPosX() + 1, this.position.getPosY(), 0));
                relevantCoordinate[3] = checkCoordinate(new Coordinate(this.position.getPosX() + 1, this.position.getPosY() + 1, 0));
                relevantCoordinate[4] = checkCoordinate(new Coordinate(this.position.getPosX(), this.position.getPosY() + 1, 0));
                break;

            case SOUTHEAST:
                relevantCoordinate[0] = checkCoordinate(new Coordinate(this.position.getPosX() + 1, this.position.getPosY() - 1, 0));
                relevantCoordinate[1] = checkCoordinate(new Coordinate(this.position.getPosX() + 1, this.position.getPosY(), 0));
                relevantCoordinate[2] = checkCoordinate(new Coordinate(this.position.getPosX() + 1, this.position.getPosY() + 1, 0));
                relevantCoordinate[3] = checkCoordinate(new Coordinate(this.position.getPosX(), this.position.getPosY() + 1, 0));
                relevantCoordinate[4] = checkCoordinate(new Coordinate(this.position.getPosX() - 1, this.position.getPosY() + 1, 0));
                break;

            case SOUTH:
                relevantCoordinate[0] = checkCoordinate(new Coordinate(this.position.getPosX() + 1, this.position.getPosY(), 0));
                relevantCoordinate[1] = checkCoordinate(new Coordinate(this.position.getPosX() + 1, this.position.getPosY() + 1, 0));
                relevantCoordinate[2] = checkCoordinate(new Coordinate(this.position.getPosX(), this.position.getPosY() + 1, 0));
                relevantCoordinate[3] = checkCoordinate(new Coordinate(this.position.getPosX() - 1, this.position.getPosY() + 1, 0));
                relevantCoordinate[4] = checkCoordinate(new Coordinate(this.position.getPosX() - 1, this.position.getPosY(), 0));
                break;

            case SOUTHWEST:
                relevantCoordinate[0] = checkCoordinate(new Coordinate(this.position.getPosX() + 1, this.position.getPosY() + 1, 0));
                relevantCoordinate[1] = checkCoordinate(new Coordinate(this.position.getPosX(), this.position.getPosY() + 1, 0));
                relevantCoordinate[2] = checkCoordinate(new Coordinate(this.position.getPosX() - 1, this.position.getPosY() + 1, 0));
                relevantCoordinate[3] = checkCoordinate(new Coordinate(this.position.getPosX() - 1, this.position.getPosY(), 0));
                relevantCoordinate[4] = checkCoordinate(new Coordinate(this.position.getPosX() - 1, this.position.getPosY() - 1, 0));
                break;

            case WEST:
                relevantCoordinate[0] = checkCoordinate(new Coordinate(this.position.getPosX(), this.position.getPosY() + 1, 0));
                relevantCoordinate[1] = checkCoordinate(new Coordinate(this.position.getPosX() - 1, this.position.getPosY() + 1, 0));
                relevantCoordinate[2] = checkCoordinate(new Coordinate(this.position.getPosX() - 1, this.position.getPosY(), 0));
                relevantCoordinate[3] = checkCoordinate(new Coordinate(this.position.getPosX() - 1, this.position.getPosY() - 1, 0));
                relevantCoordinate[4] = checkCoordinate(new Coordinate(this.position.getPosX(), this.position.getPosY() - 1, 0));
                break;

            case NORTHWEST:
                relevantCoordinate[0] = checkCoordinate(new Coordinate(this.position.getPosX() - 1, this.position.getPosY() + 1, 0));
                relevantCoordinate[1] = checkCoordinate(new Coordinate(this.position.getPosX() - 1, this.position.getPosY(), 0));
                relevantCoordinate[2] = checkCoordinate(new Coordinate(this.position.getPosX() - 1, this.position.getPosY() - 1, 0));
                relevantCoordinate[3] = checkCoordinate(new Coordinate(this.position.getPosX(), this.position.getPosY() - 1, 0));
                relevantCoordinate[4] = checkCoordinate(new Coordinate(this.position.getPosX() + 1, this.position.getPosY() - 1, 0));
                break;
        }
        return relevantCoordinate;
    }


    //Class method
    /**
     * this method checks if the given coordinate is in bound of the world. If not it will be transformed.
     * i.e. if the index is smaller than 0 it will start again from the highest index.
     * @param coordinate the coordinate which has to be checked for being out of bound
     * @return the (new) coordinate which is in bound
     */
    private Coordinate checkCoordinate(Coordinate coordinate) {
        return Coordinate.checkCoordinate(coordinate, this.world.getFields().length, this.world.getFields()[0].length, world.getFields());
    }


    //Class method
    /**
     * @param neighbourCoordinates coordinates of all relevant neighbouring fields
     * @param state the desired FieldState
     * @return the coordinate of a random neighbouring field with the given FieldState;
     *         null when none of the fields have this state
     *
     */
    private Coordinate getRandomNeighbourFieldOfState(Coordinate[] neighbourCoordinates, FieldState state) {
        List<Coordinate> relevantCoordinates = new ArrayList<>();

        for (Coordinate neighbour : neighbourCoordinates) {
            Field neighbourField = this.getFieldFromCoordinate(neighbour);

            if (neighbourField.getFieldState() == state) {
                relevantCoordinates.add(neighbour);
            }
        }

        if (relevantCoordinates.isEmpty()) return null;
        return relevantCoordinates.get((int) Math.floor(Math.random() * relevantCoordinates.size()));
    }


    //Class method
    /**
     * @param neighbourCoordinates all neighbour coordinates the ant can move to
     * @return the coordinate where the Home of the ant is or null when it isn't in the list
     */
    private Coordinate getHomefield (Coordinate[] neighbourCoordinates) {
        for(Coordinate coord : neighbourCoordinates) {
            if (this.getFieldFromCoordinate(coord).getFieldState() == FieldState.HOME &&
                    this.getFieldFromCoordinate(coord).getBuilding().id == this.antColony.id) {
                return coord;
            }
        }
        return null;
    }


    //Class method
    /**
     * this method returns a coordinate from the given coordinates by using a weighted random behaviour.
     * The base chance is the proportions between the scentValues+1. When the field has a strong scent, the chance will
     * be multiplied with Ant.strongScentMultiplier. When the field lies within the direction of the ant, the chance
     * will be multiplied with Ant.directionMultiplier.
     * when the field is often used it will also increase the chance of being taken
     * @param neighbourCoordinates coordinates of all relevant neighbouring fields
     * @return coordinate of the field where the ant wants to move next
     */
    private Coordinate chooseRandomCoordinateWithScentWeight(Coordinate[] neighbourCoordinates) {
        Coordinate directionCoordinate = this.getNextCoordinateFromCurrentDirection();
        int scentSum = 0;
        if (neighbourCoordinates.length==0) return null;
        int minScent = this.getEffectiveScent(neighbourCoordinates[0]);
        for (int i = 1; i < neighbourCoordinates.length; i++) {
            minScent=Math.min(minScent,this.getEffectiveScent(neighbourCoordinates[i]));
        }

        for (Coordinate neighbourCoordinate : neighbourCoordinates) {
            int scent = this.getEffectiveScent(neighbourCoordinate);
            if (minScent <= 0)
                scent = (scent - minScent) + 1; // to set the lowest scent to atleast 1 and scale all other fields accordingly
            scent += this.usedFields.getOrDefault(neighbourCoordinate,0);
            if (neighbourCoordinate.equals(directionCoordinate)) scent = scent * Ant.directionMultiplier;
            scentSum += scent >= Ant.scentThreshhold ? scent * Ant.strongScentMultiplier : scent;
        }

        int randomNumber = (int) Math.floor(Math.random() * scentSum);
        scentSum = 0;

        for (Coordinate neighbourCoordinate : neighbourCoordinates) {
            int scent = this.getEffectiveScent(neighbourCoordinate);
            if (minScent <= 0)
                scent = (scent - minScent) + 1; // to set the lowest scent to atleast 1 and scale all other fields accordingly
            scent += this.usedFields.getOrDefault(neighbourCoordinate,0);
            if (neighbourCoordinate.equals(directionCoordinate)) scent = scent * Ant.directionMultiplier;
            scentSum += scent >= Ant.scentThreshhold ? scent * strongScentMultiplier : scent;
            if (scentSum > randomNumber) return neighbourCoordinate;
        }

        return null;
    }


    //Class method
    /**
     * this method returns the coordinates from the neighbouring field in the ants' direction.
     * @return coordinate where the ant would go if it follows the current direction
     */
    private Coordinate getNextCoordinateFromCurrentDirection() {
        return switch (this.direction) {
            case NORTH -> checkCoordinate(new Coordinate(this.position.getPosX(), this.position.getPosY() - 1, 0));
            case NORTHEAST -> checkCoordinate(new Coordinate(this.position.getPosX() + 1, this.position.getPosY() - 1, 0));
            case EAST -> checkCoordinate(new Coordinate(this.position.getPosX() + 1, this.position.getPosY(), 0));
            case SOUTHEAST -> checkCoordinate(new Coordinate(this.position.getPosX() + 1, this.position.getPosY() + 1, 0));
            case SOUTH -> checkCoordinate(new Coordinate(this.position.getPosX(), this.position.getPosY() + 1, 0));
            case SOUTHWEST -> checkCoordinate(new Coordinate(this.position.getPosX() - 1, this.position.getPosY() + 1, 0));
            case WEST -> checkCoordinate(new Coordinate(this.position.getPosX() - 1, this.position.getPosY(), 0));
            case NORTHWEST -> checkCoordinate(new Coordinate(this.position.getPosX() - 1, this.position.getPosY() - 1, 0));
        };
    }


    //Class method
    /**
     * this method turns the ant around to the opposite direction
     */
    private void turnAround() {
        this.direction= Direction.getOppositeDirection(this.direction);
    }


    //Class method
    /**
     * @param targetPosition the Coordinate the ant wants to reach
     * @return the next coordinate the ant has to move next in order to reach the target position
     */
    private Coordinate getNextFieldToPosition(Coordinate targetPosition) {
        int x = Math.max(Math.min(targetPosition.getPosX() - this.position.getPosX(),1),-1) + this.position.getPosX();
        int y = Math.max(Math.min(targetPosition.getPosY() - this.position.getPosY(),1),-1) + this.position.getPosY();
        return new Coordinate(x,y,this.world.getFields()[x][y].getHeight());
    }
}



