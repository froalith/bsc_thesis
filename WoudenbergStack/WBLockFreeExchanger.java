package WoudenbergStack;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicStampedReference;

public class WBLockFreeExchanger<T> {
    static final int EMPTY = 0, WAITINGPOPPER = 1, WAITINGPUSHER = 2, BUSY = 3;

    AtomicStampedReference<T> slot = new AtomicStampedReference<>(null, 0);

    public T exchange(T myItem, long timeout, TimeUnit unit) throws TimeoutException {
        int status = (myItem == null) ? WAITINGPOPPER : WAITINGPUSHER;
        long nanos = unit.toNanos(timeout);
        long timeBound = System.nanoTime() + nanos;
        int[] stampHolder = {EMPTY};

        while (true) {
            if (System.nanoTime() > timeBound) {
                throw new TimeoutException();
            }

            T yrItem = slot.get(stampHolder);
            int stamp = stampHolder[0];

            switch (stamp) {
                case EMPTY:
                    if (slot.compareAndSet(yrItem, myItem, EMPTY, status)) {
                        while (System.nanoTime() < timeBound) {
                            yrItem = slot.get(stampHolder);
                            if (stampHolder[0] == BUSY) {
                                slot.set(null, EMPTY);
                                return yrItem;
                            }
                        }
                    }
                    if (slot.compareAndSet(myItem, null, status, EMPTY)) {
                        throw new TimeoutException();
                    }
                    else {
                        yrItem = slot.get(stampHolder);
                        slot.set(null, EMPTY);
                        return yrItem;
                    }
                    // break;
                case WAITINGPOPPER:
                    if (status != WAITINGPOPPER && slot.compareAndSet(yrItem, myItem, WAITINGPOPPER, BUSY)) {
                        return yrItem;
                    }
                    break;
                case WAITINGPUSHER:
                    if (status != WAITINGPUSHER && slot.compareAndSet(yrItem, myItem, WAITINGPUSHER, BUSY)) {
                        return yrItem;
                    }
                    break;
                case BUSY:
                    break;
            }
        }
    }


}
