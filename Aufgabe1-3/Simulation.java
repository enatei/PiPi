//Modul/Interface Simulation
//gets implemented in World and Draw, therefore operates on a higher level of abstraction

//Interface Simulation
//Abstraction: subtype-relationship
//This interface enables to return a boolean if the run()-method gets evoked.
//gets implemented by classes World and Draw
public interface Simulation {
    public boolean run();
}
