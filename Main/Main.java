package Main;

import ECStack.EliminationCombiningStack;
import ECStack.ThreadID;
import HSYStack.HSYEliminationBackoffStack;
import Improved.AdaptiveCollisionWait.ImprovedECStack_SmartWait;
import Improved.BC_RPSW2.BC_RP_SW_stack;
import Improved.BetterCollision.ImprovedECStack_Colliders;
import Improved.BetterCollisionLayer_RP.ImprovedECStack_Colliders_RP;
import Improved.BetterCollision_RangePolicy_SmartWait.ImprovedECStack_Colliders_RP_SW;
import Improved.CollisionRangePolicy.ImprovedECStack_RP;
import Improved.OperationTargetting.ImprovedECStack_OT;
import Improved.adaptivewait2.Adaptivewait2Stack;
import NormalStack.NormalStack;
import StackCommons.ConcurrentStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;
import java.util.Stack;

import static java.lang.Math.ceil;


public class Main {

    static int nrThreads = 4;
    static int nrItems = nrThreads* 1000000; //every thread tests 1 million items
    static int repeatCount = 5 ;
//    static ArrayList<Result> results = new ArrayList<>();
    static double[][] results = new double[10][repeatCount]; // store results in 2d array where rows are different stacks
    static int [][] collisionCounter = new int [10][repeatCount];
    static int [][] collisionFails = new int [10][repeatCount];
    static int [][] stackCounter = new int [10][repeatCount];
    static int [][] stackFails = new int [10][repeatCount];
    static Random random;
    static int [][] values = new int [nrThreads][1000000];
    static int [][] operations = new int [nrThreads][1000000];
    static int iteration = 0;



    public static void makeOperations(){
        random = new Random();
        for (int i =0; i < nrThreads; i++) {
            values[i] = random.ints(1000000, 1, 100000).toArray();
            operations[i] = random.ints(1000000, 0, 2).toArray();
        }
    }

    public static void main(String[] args){
        for (int i = 1; i <= repeatCount; i++) { //perform x amount of times and get the average
            makeOperations();
            benchmarkStacks();
            iteration++;
            System.out.println(iteration);
        }
        printResults();
//        makeOperations();
//        benchmarkStacks();
//        printResults();

    }
    public static void addCounters(int[] counters, int resultCounter) {
        stackCounter[resultCounter][iteration] = counters[0];
        stackFails[resultCounter][iteration] = counters[1];
        collisionCounter[resultCounter][iteration] = counters[2];
        collisionFails[resultCounter][iteration] = counters[3];
    }
    //initializes the different stacks
    //calls PopulateStack and adds the benchmarked results to the result array
    //this function is slightly overloaded as returning 8 different stacks is annoying and this makes it easier
    public static void benchmarkStacks() {
        EliminationCombiningStack<Integer> ec = new EliminationCombiningStack<>(nrThreads);
        HSYEliminationBackoffStack<Integer> HSYEB = new HSYEliminationBackoffStack<>();
        ImprovedECStack_SmartWait<Integer> impWAIT = new ImprovedECStack_SmartWait<>(nrThreads);
        ImprovedECStack_Colliders<Integer> impCOL = new ImprovedECStack_Colliders<>(nrThreads);
        ImprovedECStack_Colliders_RP_SW<Integer> impRPSW = new ImprovedECStack_Colliders_RP_SW<>(nrThreads);
        ImprovedECStack_Colliders_RP<Integer> impCOLRP = new ImprovedECStack_Colliders_RP<>(nrThreads);
        ImprovedECStack_RP<Integer> impRP = new ImprovedECStack_RP<>(nrThreads);
        ImprovedECStack_OT<Integer> impOT = new ImprovedECStack_OT<>(nrThreads);
        Adaptivewait2Stack<Integer> ADW = new Adaptivewait2Stack<>(nrThreads);
        NormalStack<Integer> normalStack = new NormalStack<Integer>();
        BC_RP_SW_stack<Integer> bc_rp_sw = new BC_RP_SW_stack<>(nrThreads);

        PopulateStack(ec, HSYEB, impWAIT, impCOL, impRPSW, impCOLRP, impRP, impOT, normalStack, ADW, bc_rp_sw);

        int resultCounter = 0; //well first time i actually wanted to be able to pass by reference

        addResult("ec threads: " + nrThreads, benchmark(ec), resultCounter);
        addCounters(ec.getCounters(), resultCounter);
        resultCounter++;

        addResult("HSYEB threads:"+ nrThreads, benchmark(HSYEB), resultCounter);
        addCounters(HSYEB.getCounters(), resultCounter);
        resultCounter++;

        addResult("impWait threads:"+ nrThreads, benchmark(impWAIT), resultCounter);
        addCounters(impWAIT.getCounters(), resultCounter);
        resultCounter++;

        addResult("ADW " + nrThreads, benchmark(ADW), resultCounter);
        addCounters(ADW.getCounters(), resultCounter);
        resultCounter++;

        addResult("impCOL threads: "+ nrThreads, benchmark(impCOL), resultCounter);
        addCounters(impCOL.getCounters(), resultCounter);
        resultCounter++;

        addResult("impRPSW threads: "+ nrThreads, benchmark(impRPSW), resultCounter);
        addCounters(impRPSW.getCounters(), resultCounter);
        resultCounter++;

        addResult("bc_rp_sw:" + nrThreads, benchmark(bc_rp_sw), resultCounter);
        addCounters(bc_rp_sw.getCounters(), resultCounter);
        resultCounter++;

        addResult("impCOLRP threads: "+ nrThreads, benchmark(impCOLRP), resultCounter);
        addCounters(impCOLRP.getCounters(), resultCounter);
        resultCounter++;

        addResult("impRP threads: "+ nrThreads, benchmark(impRP), resultCounter);
        addCounters(impRP.getCounters(), resultCounter);
        resultCounter++;

//        addResult("normalstack " + "1", benchmark(normalStack), resultCounter);
//        resultCounter++;
        addResult("impOT " + nrThreads, benchmark(impOT), resultCounter);
        addCounters(impOT.getCounters(), resultCounter);
    }

    //transforms the result class to a string and adds it to the results array
    public static void addResult(String stackName, Result result, int counter) {
        System.out.println("result added " + stackName);
        results[counter][iteration] = result.getMOS();
        System.out.println(results[counter][iteration]);
    }

    //prints the results
    public static void printResults() {
        for (int i = 0; i < results.length; i++) {
            double average = 0;
            double stackAverage =0;
            double stackFailAverage =0;
            double collisionAverage = 0;
            double collisionFailAverage =0;
            for (int j = 0; j < results[i].length; j++) {
                average += results[i][j];
                stackAverage += stackCounter[i][j];
                stackFailAverage += stackFails[i][j];
                collisionAverage += collisionCounter[i][j];
                collisionFailAverage += collisionFails[i][j];
            }
            System.out.println("average " + i + ": " + (average/ results[i].length) + " stacksuccesses: " + (stackAverage/results[i].length)
             + " stackfails: " + (stackFailAverage/results[i].length) + " colsuccesses: " + (collisionAverage/results[i].length) + " colfails: " + (collisionFailAverage/results[i].length));
        }
    }

    //adds numbers to every stack, also checks the percentage of push and pops
    public static void PopulateStack(EliminationCombiningStack<Integer> ec, HSYEliminationBackoffStack<Integer> HSYEB,
                                     ImprovedECStack_SmartWait<Integer> impWAIT, ImprovedECStack_Colliders<Integer> impCOL,
                                     ImprovedECStack_Colliders_RP_SW<Integer> impRPSW, ImprovedECStack_Colliders_RP<Integer> impCOLRP,
                                     ImprovedECStack_RP<Integer> impRP,ImprovedECStack_OT<Integer> impOT , NormalStack<Integer> normalStack ,
                                     Adaptivewait2Stack ADW, BC_RP_SW_stack bc_rp_sw){ // populates the initial stacks
        //add initial numbers to the stack
        for (int i = 0; i < Math.ceil(nrItems/2.2); i++) {
            try {
                ec.push(i);
                HSYEB.push(i);
                impWAIT.push(i);
                impCOL.push(i);
                impRPSW.push(i);
                impCOLRP.push(i);
                impRP.push(i);
//                normalStack.push(i);
                ADW.push(i);
                bc_rp_sw.push(i);

            }
            catch (InterruptedException ie){

            }
        }
        ArrayList<WorkerThread<Integer>> workerThreads = new ArrayList<>(); //populating layerselector kinda has to happen with multiple threads
        for (int j = 0; j< 3; j++) {
//            System.out.println("j= " + j);
            int[] toPerform = new int[nrItems/6];
            Arrays.fill(toPerform, 1);
            Integer[] copyVal = new Integer[nrItems/6];
            workerThreads.add(new WorkerThread<Integer>(j, impOT, toPerform, copyVal));
        }
        for (WorkerThread<Integer> t : workerThreads ) {
            System.out.println("thread");
            t.start();
        }
        //waiting for threads to finish
        for (WorkerThread<Integer> t : workerThreads) {
            try {
                t.join();
            } catch (InterruptedException e) {
                throw new Error(
                        "Unexpected InterruptedException. Should not happen.",
                        e);
            }
            ThreadID.reset();
        }
        try{
            impOT.pop();
            int counter = 0;
            for (int i = 0; i < operations.length; i++) { // checks if pops and pushes are roughly even
                for(int j = 0; j < operations[i].length; j++) {
                    if (operations[i][j] == 1 ) {
                        counter++;
                    }
                }
            }
            System.out.println(counter);
            }

        catch (InterruptedException ie){}
    }

    //times the performance of the different stacks
    public static Result benchmark(ConcurrentStack<Integer> Stack) {
        ArrayList<WorkerThread<Integer>> workerThreads = new ArrayList<>();
        for (int j = 0; j< nrThreads; j++) { // get operations for every thread
//            System.out.println("j= " + j);
            int []toPerform = operations[j];
            Integer []copyVal = Arrays.stream(values[j]).boxed().toArray(Integer[]::new);
            workerThreads.add(new WorkerThread<Integer>(j, Stack, toPerform, copyVal));
        }

        double startTime = System.nanoTime();
        for (WorkerThread<Integer> t : workerThreads ) {// run the threads
            t.start();
        }
        //waiting for threads to finish
        for (WorkerThread<Integer> t : workerThreads) {
            try {
                t.join();
            } catch (InterruptedException e) {
                throw new Error(
                        "Unexpected InterruptedException. Should not happen.",
                        e);
            }
        }
        double endTime = System.nanoTime();
        ThreadID.reset();
//        try {
//            int y = Stack.pop();
//            System.out.println(Arrays.asList(values).lastIndexOf(y));
//        }catch (InterruptedException e){}
        return (new Result(startTime, endTime, nrItems));

    }
}