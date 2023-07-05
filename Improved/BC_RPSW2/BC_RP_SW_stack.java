package Improved.BC_RPSW2;

import ECStack.EliminationCombiningStack;
import ECStack.MultiOp;
import ECStack.Status;
import Improved.AdaptiveCollisionWait.AdaptiveWaiter;
import Improved.CollisionRangePolicy.RangePolicy_AIAD;
import StackCommons.ConcurrentStack;

public class BC_RP_SW_stack<T> extends EliminationCombiningStack<T> implements ConcurrentStack<T> {

    public BC_RP_SW2<T>[] colliders;
    private int threadCount;

    protected ThreadLocal<RangePolicy_AIAD> rangePolicy = new ThreadLocal<RangePolicy_AIAD>() {
        protected synchronized RangePolicy_AIAD initialValue() {
            return new RangePolicy_AIAD(2, threadCount);
        }
    };

    protected ThreadLocal<AdaptiveWaiter> adaptiveWaiter = new ThreadLocal<AdaptiveWaiter>() {
        protected synchronized AdaptiveWaiter initialValue() {
            return new AdaptiveWaiter(100, 1000);
        }
    };

    public BC_RP_SW_stack(int nThreads) {
        super(nThreads);
        threadCount = nThreads;
        colliders = (BC_RP_SW2<T>[]) new BC_RP_SW2[nThreads];

        for (int i = 0; i < colliders.length; i++) {
            colliders[i] = new BC_RP_SW2<>(this);
        }
    }

    @Override
    public boolean activeCollide(MultiOp<T> aInf, MultiOp<T> pInf) {
        if (aInf.op == pInf.op) {
            combine(aInf, pInf);
            return false;
        }
        else {
            multiEliminate(aInf,pInf);
            return true;
        }
    }

    public boolean passiveCollide(MultiOp<T> pInf, MultiOp<T> aInf) {
        if (pInf.op != aInf.op) {
            if (pInf.op == MultiOp.Type.POP) {
                pInf.node = aInf.node;
            }
            return true;
        }
        else {
            while (pInf.status == Status.INIT) {
                try {Thread.sleep(0, 250);} catch (InterruptedException ex) {}   // TODO this can be improved
            }
            if (pInf.status == Status.FINISHED) {
                return true;
            }
            else {
                pInf.status = Status.INIT;
                return false;
            }
        }
    }

    @Override
    public boolean collide(MultiOp<T> mOp) {
        int index = rangePolicy.get().randomIndex();
        return colliders[index].collide(mOp);
    }
}
