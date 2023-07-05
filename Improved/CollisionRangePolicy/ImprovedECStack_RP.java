package Improved.CollisionRangePolicy;

import ECStack.EliminationCombiningStack;
import ECStack.MultiOp;
import ECStack.ThreadID;
import StackCommons.ConcurrentStack;

public class ImprovedECStack_RP<T> extends EliminationCombiningStack<T> implements ConcurrentStack<T> {

    RangePolicy_AIAD policy = new RangePolicy_AIAD(2, collision.length); // TODO was threadlocal
//        {
//        protected synchronized RangePolicy_AIAD initialValue() {
//            return new RangePolicy_AIAD(2, collision.length);
//        }


    public ImprovedECStack_RP(int nThreads) {
        super(nThreads);
    }

    @Override
    public boolean collide(MultiOp<T> mOp) {
        location[ThreadID.get()].set(mOp);    // location[id] = mOp
        int index = policy.randomIndex();  // choose a random index in the collision array
        int him = collision[index].get();     // get the id of the thread at that index

        while (!collision[index].compareAndSet(him, ThreadID.get())) {
            him = collision[index].get();
            policy.increaseRange();
        }
        policy.decreaseRange();     // TODO is this the best place to put this?

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

        try {Thread.sleep(0, 1000);} catch (InterruptedException ex){} // FIXME OPT: Maybe an adaptive backoff instead of just a flat wait?

        if (!location[ThreadID.get()].compareAndSet(mOp, null)) {
            return passiveCollide(mOp);
        }
        return false;
    }

    public void printFailures(){
        policy.printCounters();
    }
}
