import java.util.Timer;
import java.util.TimerTask;
/**
 * HOW IS IT STRUCTURED?
 - Test:                                creates instances of World and Draw and sets a timer for the simulation to progress
     - Draw (implements Simulation):    gets instance of World and animates the simulation
     - World (implements Simulation):   creates instances of Ant, Building and Field and calls for progress each simulation step
        - Ant:                          creates instances of Coordinate and uses Enumerations AntState and Direction
            - Coordinate:               used for navigation in Ant, used for the position of fields (food, home) in World
            - Enumeration AntState:     used for behavioral changes in Ant
            - Enumeration Direction:    used for navigation in Ant
        - Building:                     a record which holds information per ant colony
        - Dijkstra:                     finds shortest path from point A to point B within the world
        - Field:                        used to represent the worlds fields
            - Enumeration FieldState:   used to mark certain fields (home, food)

 - Timer (imported in Test):            used to call for progress each simulation step
     - MyTimerTask:                     used to create tasks for Timer
 - Interface Simulation:                used to enable classes being called by MyTimerTask


 * WHAT DOES IT DO?
 This simulation simulates the behaviour of ants. By leaving scent marks, other ants can find the paths and navigate through the world.
 Ants will explore the world, search for food and carry it home.

 - 3D:
 Each field within the world has the possibility to have a different height, so it can take several simulation turns for ants to cross to another field.

 - Shortest path:
 On their way from one food point to their home, their shortest found path gets documented via HashMap. For comparison the shortest path gets calculated
 by using the dijkstra-algorithm. Both will be displayed in the animation. The feature shortestPath can be set to true/false when creating an instance of world.

 - Learning ability:
 The older an ant gets, the better they remember their already taken paths and are more likely to choose them over new fields. Each Ant stores which field they traveresd how often
 up to <antMemory> times often. Rarely traveresd fields will be forgotten over time.
 The ability to remember is the antMemory parameter which can be set to an integer between 0 and 100, whereas 0 makes them not remember a thing.

 - Founding ant colonies:
 Each ant can create a new colony, if the maximum of colonies within a world has not been reached. The possibility is the homeSpawnChance,
 the maximum of colonies is the maxHomes parameter. There's only one colony to which every ant belongs to at the beginning.
 Once a new colony has been found, it will create a new building, which hold the colonies' ID, antScent and antColor.
 A new ant home will be set within the world and new ants will spawn.
 Note: Only ants from the first colony will be used to find the shortest path.

 - Different scent marks per colony:
 With different ant colonies come different scent marks per ant colony. Therefore ants are able to distinguish between scents by
 saving the sum of scents per ant building within a HashMap of a field. Ants will prefer their own colony scent over the scent from others.

 - Recognition of their own scent mark:
 In order to find an easy way home, ants are able to spot their own scent on a field within the scent of other ants. Ants will prefer these
 fields over other fields when carrying food but ignore there own scent when searching for food. This functionality comes hand in hand with different scent marks per colony because
 there we already saving the scent per ant on the field.
 - (not implemented) Vanishing food sources
 - (not implemented) Rain


 * WHO DID WHAT?
 David:
 - learning ability
 - recognition of their own scent mark
 - founding ant colonies (class Building, class Ant, class Field, class World)
 - different scent marks per colony (class Ant, class Field)
 - implementation of procedural and functional paradigms (class Building, class Field)
 - documentation, comments and testing / debugging

 Nati:
 - 3D
 - shortest path
 - founding of ant colonies (class Draw, class World)
 - different scent marks per colony (class Draw)
 - implementation of procedural paradigms (class Dijkstra)
 - documentation & comments and testing / debugging
 */


//Module/Class Test
//Abstraction: real World
//uses instances of World and Draw, therefore operates on a higher level of abstraction
//is a subtype of Object (-> every class is a subtype of Object)
public class Test {

    //Module method of module Test
    public static void main(String[] args) {
        int sideLength1 = 5;
        int sideLength2 = 20;
        int sideLength3 = 250;

        int ants1 = 3;
        int ants2 = 25;
        int ants3 = 100;

        int food1 = 3;
        int food2 = 6;
        int food3 = 10;

        int delay1 = 0;
        int delay2 = 30;
        int period = 200;

        //TEST #1
        //Object world#: instance of type World.
        //Object draw#: instance of type Draw.
        World world1 = new World(sideLength1, ants1, food1, 5, 30, 0.98, (int) Math.ceil(sideLength1 / 4.0), 5, true, 1, 0,0);
        Draw draw1 = new Draw(world1, sideLength1, "Simulation1");
        //this method calls the .run()-method of a Runnable instance to repositions the ants
        setTimer(draw1, delay2, period, world1, delay1, period);

         //TEST #2
        World world2 = new World(sideLength2, ants2, food2, 12, 24, 0.97, (int) Math.ceil(sideLength2 / 6.0), 3, true,2, 0.02,10);
        Draw draw2 = new Draw(world2, sideLength2, "Simulation2");
        setTimer(draw2, delay2, period, world2, delay1, period);

       //TEST #3
        World world3 = new World(sideLength3, ants3, food3, 5, 30, 0.96, (int) Math.ceil(sideLength3 / 10.0), 6, true,3, 0.003,30);
        Draw draw3 = new Draw(world3, sideLength3, "Simulation3");
        setTimer(draw3, delay2, period, world3, delay1, period);
    }

    //Module method of module Test
    /**
     * this method sets two timers and evokes the run()-method for each Simulation object after a set delay and period
     * @param object1 that will be triggered by the timer
     * @param delay1 starting delay in miliseconds before the timer starts counting
     * @param period1 milliseconds a period takes
     * @param object2 that will be triggered by the timer
     * @param delay2 starting delay in miliseconds before the timer starts counting
     * @param period2 miliseconds a period takes
     */
    public static void setTimer(Simulation object1, int delay1, int period1, Simulation object2, int delay2, int period2) {
        //object timer: instance of type Timer
        Timer timer1 = new Timer();
        Timer timer2 = new Timer();

        //object task: instance of type MyTimerTask
        TimerTask task1 = new MyTimerTask(timer1, timer2, object1);
        TimerTask task2 = new MyTimerTask(timer2, timer1, object2);

        //Class method
        task1.run();
        task2.run();

        //Class method
        timer1.scheduleAtFixedRate(task1, delay1, period1);
        timer2.scheduleAtFixedRate(task2, delay2, period2);
    }
}