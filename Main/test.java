package Main;

import java.util.Random;
import java.util.Stack;

public class test {
    static double [] results = new double [100];
    static int counter = 0;

    public static void main(String[] args) {
        for (int i = 0; i < 100; i++) {
            Stack test = new Stack();
            Random random = new Random();
            int [] values = random.ints(5000000, 1, 100000).toArray();
            int [] operations = random.ints(5000000, 0, 2).toArray();
            for (int j = 0; j < 5000000; j++) {
                test.push(j);
            }
            benchmark(values, operations, test);
            System.out.println(counter);
        }
        double average = 0;
        for(int i = 0; i < results.length; i++) {
            average+= results[i];
        }

        System.out.println("average = " + (average/100));

    }
    public static void benchmark(int[] values, int[] operations, Stack stack) {
        double startTime = System.nanoTime();
        for (int i : operations) {
            if(operations[i] == 1) {
                stack.pop();
            }
            else {
                stack.push(values[i]);
            }
        }
        double endTime = System.nanoTime();
        results[counter] =((1/((endTime-startTime)/1000000000) * 5000000)/1000000);
        counter++;
    }
}
