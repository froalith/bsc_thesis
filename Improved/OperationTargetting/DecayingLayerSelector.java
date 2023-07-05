package Improved.OperationTargetting;

import javax.swing.plaf.TableHeaderUI;
import java.util.Random;
import java.util.SplittableRandom;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

public class DecayingLayerSelector {
    private double stackPreference;
    private int stackFailCounter;
    private int stackFailCountertoo;
    private double collisionPreference;
    private int collisionFailCounter;
    private int collisonFailcountertoo;
    private final int THRESH;
    SplittableRandom splittableRandom;
    private final double DECAY_RATE; // must be between 1 and 0



    public DecayingLayerSelector(double decayRate) {
        splittableRandom = new SplittableRandom();
        stackFailCountertoo = 0;
        collisonFailcountertoo = 0;
        stackPreference = 1000; // should be 100?
        stackFailCounter = 0;
        collisionPreference = 1;
        collisionFailCounter = 0;
        THRESH = 10; // TODO was 10
        DECAY_RATE = decayRate;
    }

    public boolean select() {
//        stackMutex.lock();
        double range = stackPreference + collisionPreference;
//        stackMutex.unlock();
//        System.out.println("range " + range + " stackpref " + stackPreference + " collpref " +collisionPreference );
        double value = splittableRandom.nextDouble() * range;
//        System.out.println("value " + value + "stackpref " + stackPreference + " collpref " + collisionPreference);
        return value < stackPreference;
    }

    public void collisionFailed() {
//        System.out.println("collision failed " + collisonFailcountertoo + " pref " + collisionPreference );
//        System.out.println("c");

        try {
            Thread.sleep(0, 100);
        }catch (InterruptedException ie){}

        collisionFailCounter++;

        collisonFailcountertoo++;
        if (collisionFailCounter >= THRESH) {
            collisionFailCounter = 0;
//            stackMutex.lock();
            stackPreference = stackPreference * ((1 - DECAY_RATE) + 4); //TODO: maybe +100 was +1
            collisionPreference = Math.max(0.00000001, collisionPreference / (15 - DECAY_RATE)); // TODO: was 1, decayrate * 1
//            stackMutex.unlock();
        }
    }

   public void stackFailed() {
//        System.out.println("stack failed " + stackFailCountertoo + " pref " + stackPreference);
//       System.out.println("s");
       try {
           Thread.sleep(0, 100);
       }catch (InterruptedException ie){}

       stackFailCounter++;
        stackFailCountertoo++;
        if (stackFailCounter >= THRESH) {
            stackFailCounter = 0;
//            stackMutex.lock();
            collisionPreference = collisionPreference *((1 - DECAY_RATE) + (1+DECAY_RATE)); //TODO was 1
//            collisionPreference *= (DECAY_RATE +1);
            stackPreference = Math.max(0.0001, stackPreference / 3); // TODO should be using the decay rate but meh
//            stackMutex.unlock();
        }
    }
//    public void printFailures() {
//        System.out.println("stackfail " + stackFailCountertoo + "collisionfail " + collisonFailcountertoo);
//    }
//    public void clearFailures() {
//        stackFailCountertoo = 0;
//        collisonFailcountertoo = 0;
//    }
}
