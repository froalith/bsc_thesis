package Improved.adaptivewait2;

import ECStack.EliminationCombiningStack;
import ECStack.MultiOp;
import ECStack.ThreadID;
import Improved.AdaptiveCollisionWait.AdaptiveWaiter;
import StackCommons.ConcurrentStack;

public class Adaptivewait2Stack<T> extends EliminationCombiningStack<T> implements ConcurrentStack<T> {
    private ThreadLocal<AdaptiveWaiter> adaptiveWaiter = new ThreadLocal<AdaptiveWaiter>() {
        protected synchronized AdaptiveWaiter initialValue() {
            return new AdaptiveWaiter(100, 1000);
        }
    };

    public Adaptivewait2Stack(int nThreads) {
        super(nThreads);
    }

    @Override
    public boolean collide(MultiOp<T> mOp) {
        location[ThreadID.get()].set(mOp);    // location[id] = mOp
        int index = rand.nextInt(collision.length);  // choose a random index in the collision array
        int him = collision[index].get();     // get the id of the thread at that index

        while (!collision[index].compareAndSet(him, ThreadID.get())) {
            him = collision[index].get();
        }

        if (him != EMPTY) {
            MultiOp<T> oInfo = location[him].get();
            if (oInfo != null && oInfo.id != ThreadID.get() && oInfo.id == him) {
                if (location[ThreadID.get()].compareAndSet(mOp, null)) {
                    return activeCollide(mOp, oInfo);
                }
                else {
                    return passiveCollide(mOp);
                }
            }
        }

        adaptiveWaiter.get().awaitCollider();

        if (!location[ThreadID.get()].compareAndSet(mOp, null)) {
            adaptiveWaiter.get().decreaseWait(); //TODO was increase
            return passiveCollide(mOp);
        }
        adaptiveWaiter.get().increaseWait();
        return false;
    }
}
