import java.util.Timer;
import java.util.TimerTask;

/*
    This Class is an extension of the java-Class TimerTask. It saves the variables timer1, timer2 and object in order to cancel both
    timer1 and timer2, if a task cannot be run.
 */


//Modul/Class MyTimerTask
//Abstraction: subtype, real world
//extends Class TimerTask, therefore is a subtype
//gets used in Class Test, therefore is a subtype
//uses references of Timer and Simulation, therefore operates on a higher level of abstraction
public class MyTimerTask extends TimerTask {
    private Timer timer1;
    private Timer timer2;
    private Simulation object;


    //Class method
    /**
     * this constructor sets the class' timer1 and timer2 as well as the class' object
     * @param timer1 the timer for object1
     * @param timer2 the timer for object2
     * @param object1 the Simulation object for which the timer shall be set for
     */
    public MyTimerTask(Timer timer1, Timer timer2, Simulation object1) {
        this.timer1 = timer1;
        this.timer2 = timer2;
        this.object = object1;
    }


    //Class method
    /**
     * this method evokes the objects' run()-method. If it returns false, timer1 and timer2 will get canceled.
     */
    public void run() {
        //Class method
        if(!object.run()){
            timer1.cancel();
            timer2.cancel();
        }
    }
}
