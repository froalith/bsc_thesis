package Main;

import StackCommons.ConcurrentStack;

public class Result {
    static final int billion = 1000000000;
    static final double million = 1000000;
    protected double totalTime; // in nanoseconds
    protected int operations;
    protected double MOS;
    protected String name;
//    protected ConcurrentStack<Integer> concurrentStack;

    public Result(double startTime, double endTime, int opCount ) {
        totalTime = endTime - startTime;
        operations = opCount;
        MOS = ((1/(totalTime/billion) * operations)/million);
//        /(operations/million);
    }
    public double getMOS() {
        return MOS;
    }
    public void resultPrint(){
        System.out.println(name + ", op:  " + operations + ", totaltime in ns:  " + totalTime + ", time in s " + totalTime/billion + ", MOS:   " + MOS + "\n");
    }

}
