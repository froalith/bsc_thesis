package WoudenbergStack;

import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class WBEliminationArray<T> {
    private static final int duration = 1;
    private WBLockFreeExchanger<T>[] exchanger;
    private Random random;

    public WBEliminationArray (int capacity) {
        exchanger = (WBLockFreeExchanger<T>[]) new WBLockFreeExchanger[capacity];

        for (int i = 0; i < capacity; i++) {
            exchanger[i] = new WBLockFreeExchanger<T>();
        }
        random = new Random();
    }

    public T visit (T value, int range) throws TimeoutException {
        int slot = random.nextInt(range);
        return (exchanger[slot].exchange(value, duration, TimeUnit.MILLISECONDS));
    }

}
