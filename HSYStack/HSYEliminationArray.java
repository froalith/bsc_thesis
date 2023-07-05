package HSYStack;

import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class HSYEliminationArray<T> {
    private static final int duration = 1;
    private HSYLockFreeExchanger<T>[] exchanger;
    private Random random;

    public HSYEliminationArray (int capacity) {
        exchanger = (HSYLockFreeExchanger<T>[]) new HSYLockFreeExchanger[capacity];

        for (int i = 0; i < capacity; i++) {
            exchanger[i] = new HSYLockFreeExchanger<T>();
        }
        random = new Random();
    }

    public T visit (T value, int range) throws TimeoutException {
        int slot = random.nextInt(range);
        return (exchanger[slot].exchange(value, duration, TimeUnit.MILLISECONDS));
    }
}
