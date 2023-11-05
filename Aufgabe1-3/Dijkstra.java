import java.util.ArrayList;
import java.util.PriorityQueue;
import java.util.List;

/**
    STYLE:
    This class is written in a procedural style as all its methods are implemented as static.
    This enables them to be used outside of this class, if the methods are public. This also includes that there's no
    need for an instance - instead it can be evoked by using the class name itself (i.e. Dijkstra.shortestPath()).
    There are no intern states, everything works via given parameters in methods and therefore returns the same output
    for the same input.
*/


//Module/Class Dijkstra
//Abstraction: Simulation
//uses references of following Classes: Field, Coordinate. Therefore operates on a higher level of abstraction
//gets used in classes World and Draw, therefore operates on a lower level of abstraction
public class Dijkstra {

    //Module method
    /**
     * @param food the start Coordinate
     * @param home the goal Coordinate
     * @param fields double array which holds all fields of the world
     * @return an integer which represents the length of the shortest path
     */
    public static int shortestPath(Coordinate food, Coordinate home, Field[][] fields) {
        int sideLength = fields.length;
        int[][] distances = new int[sideLength][sideLength];
        for(int i = 0; i < sideLength; i++) {
            for(int j = 0; j < sideLength; j++) {
                distances[i][j] = Integer.MAX_VALUE;
            }
        }

        distances[food.getPosX()][food.getPosY()] = 0;

        PriorityQueue<Coordinate> priority = new PriorityQueue<>();
        priority.add(new Coordinate(food.getPosX(), food.getPosY(), 0));

        while(!priority.isEmpty()) {
            Coordinate look = priority.poll();
            int distance = look.getPosZ();

            if(look.equals(home)) {
                return distance;
            }

            if(distance > distances[look.getPosX()][look.getPosY()]) {
                continue;
            }

            List<Coordinate> neighbors = getNeighbors(look.getPosX(), look.getPosY(), fields);

            for(Coordinate neighbor : neighbors) {
                int newX = neighbor.getPosX();
                int newY = neighbor.getPosY();
                int cost = neighbor.getPosZ();
                int newDistance = distance + cost;

                if (newDistance < distances[newX][newY]) {
                    distances[newX][newY] = newDistance;
                    priority.add(new Coordinate(newX, newY, newDistance));
                }
            }
        }

        return -1;
    }


    //Module method
    /**
     * @param x position of the current field
     * @param y position of the current field
     * @param fields double array which holds all the fields of this world
     * @return a list of neighboring fields
     */
    private static List<Coordinate> getNeighbors(int x, int y, Field[][] fields) {
        int sideLength = fields.length;
        List <Coordinate> neighbors = new ArrayList<>();

        //up
        if(y > 0) {
            neighbors.add(new Coordinate(x, y - 1, calculateCost(fields[y][x].getHeight(), fields[y - 1][x].getHeight())));
            //up-left
            if(x > 0) {
                neighbors.add(new Coordinate(x - 1, y - 1, calculateCost(fields[y][x].getHeight(), fields[y - 1][x - 1].getHeight())));
            } else {
                neighbors.add(new Coordinate(sideLength - 1, y - 1, calculateCost(fields[y][x].getHeight(), fields[y - 1][sideLength - 1].getHeight())));
            }
            //up-right
            if(x < sideLength - 1) {
                neighbors.add(new Coordinate(x + 1, y - 1, calculateCost(fields[y][x].getHeight(), fields[y - 1][x + 1].getHeight())));
            } else {
                neighbors.add(new Coordinate(0, y - 1, calculateCost(fields[y][x].getHeight(), fields[y - 1][0].getHeight())));
            }
        } else {
            neighbors.add(new Coordinate(x, sideLength - 1, calculateCost(fields[y][x].getHeight(), fields[sideLength - 1][x].getHeight())));
            //up-left
            if (x > 0) {
                neighbors.add(new Coordinate(x - 1, sideLength - 1, calculateCost(fields[y][x].getHeight(), fields[sideLength - 1][x - 1].getHeight())));
            } else {
                neighbors.add(new Coordinate(sideLength - 1, sideLength - 1, calculateCost(fields[y][x].getHeight(), fields[sideLength - 1][sideLength - 1].getHeight())));
            }
            //up-right
            if(x < sideLength -1){
                neighbors.add(new Coordinate(x + 1, sideLength - 1, calculateCost(fields[y][x].getHeight(), fields[sideLength - 1][x + 1].getHeight())));
            } else {
                neighbors.add(new Coordinate(0, sideLength - 1, calculateCost(fields[y][x].getHeight(), fields[sideLength - 1][0].getHeight())));
            }
        }

        //left
        if(x > 0) {
            neighbors.add(new Coordinate(x - 1, y, calculateCost(fields[y][x].getHeight(), fields[y][x - 1].getHeight())));
            //down-left
            if(y < sideLength - 1) {
                neighbors.add(new Coordinate(x - 1, y + 1, calculateCost(fields[y][x].getHeight(), fields[y + 1][x - 1].getHeight())));
            } else {
                neighbors.add(new Coordinate(x - 1, 0, calculateCost(fields[y][x].getHeight(), fields[0][x - 1].getHeight())));
            }
        } else {
            neighbors.add(new Coordinate(sideLength - 1, y, calculateCost(fields[y][x].getHeight(), fields[y][sideLength - 1].getHeight())));
            //down-left
            if(y < sideLength - 1) {
                neighbors.add(new Coordinate(sideLength - 1, y + 1, calculateCost(fields[y][x].getHeight(), fields[y + 1][sideLength - 1].getHeight())));
            } else {
                neighbors.add(new Coordinate(sideLength - 1, 0, calculateCost(fields[y][x].getHeight(), fields[0][sideLength - 1].getHeight())));
            }
        }

        //right
        if(x < sideLength - 1) {
            neighbors.add(new Coordinate(x + 1, y, calculateCost(fields[y][x].getHeight(), fields[y][x + 1].getHeight())));
            //down-right
            if(y < sideLength - 1) {
                neighbors.add(new Coordinate(x + 1, y + 1, calculateCost(fields[y][x].getHeight(), fields[y + 1][x + 1].getHeight())));
            } else {
                neighbors.add(new Coordinate(x + 1, 0, calculateCost(fields[y][x].getHeight(), fields[0][x + 1].getHeight())));
            }
        } else {
            neighbors.add(new Coordinate(0, y, calculateCost(fields[y][x].getHeight(), fields[y][0].getHeight())));
            //down-right
            if(y < sideLength - 1) {
                neighbors.add(new Coordinate(0, y + 1, calculateCost(fields[y][x].getHeight(), fields[y + 1][0].getHeight())));
            } else {
                neighbors.add(new Coordinate(0, 0, calculateCost(fields[y][x].getHeight(), fields[0][0].getHeight())));
            }
        }

        //down
        if(y < sideLength - 1) {
            neighbors.add(new Coordinate(x, y + 1, calculateCost(fields[y][x].getHeight(), fields[y + 1][x].getHeight())));
        } else {
            neighbors.add(new Coordinate(x, 0, calculateCost(fields[y][x].getHeight(), fields[0][x].getHeight())));
        }

        return neighbors;
    }


    //Module method
    /**
     * @param fieldHeight1 height of the current field
     * @param fieldHeight2 height of the possible next field
     * @return an integer of the cost it would take to take this path
     */
    private static int calculateCost(int fieldHeight1, int fieldHeight2) {
        if(fieldHeight1 == fieldHeight2) {
            return 1;
        } else {
            return (int) Math.ceil(Math.sqrt(1 + Math.pow(fieldHeight1 - fieldHeight2, 2)));
        }
    }
}
