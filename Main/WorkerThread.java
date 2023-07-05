package Main;

import StackCommons.ConcurrentStack;

public class WorkerThread<T> extends Thread {
    /** Thread number. */
    private final int id;
    /** The data structure to be used. */
    private final ConcurrentStack<T> Stack;
    /** The items to be added by this thread. */
    private final int[] OperationsToPerform;
    private final T[] values;

    public WorkerThread(int id, ConcurrentStack<T> Stack, int[]OperationsToPerform, T[]values) {
        this.id = id;
        this.Stack = Stack;
        this.OperationsToPerform = OperationsToPerform;
        this.values = values;
    }


    @Override
    public void run() {
        for (int i : OperationsToPerform) {
            try {
                if (OperationsToPerform[i] == 1) {
                    Stack.pop();
                } else {
                    Stack.push(values[i]);
                }
            }catch (InterruptedException ie){ie.printStackTrace();}
        }

    }
}
