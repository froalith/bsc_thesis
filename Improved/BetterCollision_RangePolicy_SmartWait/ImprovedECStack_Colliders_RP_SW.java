package Improved.BetterCollision_RangePolicy_SmartWait;

import ECStack.EliminationCombiningStack;
import ECStack.MultiOp;
import ECStack.Status;
import Improved.AdaptiveCollisionWait.AdaptiveWaiter;
import Improved.CollisionRangePolicy.RangePolicy_AIAD;
import StackCommons.ConcurrentStack;

public class ImprovedECStack_Colliders_RP_SW<T> extends EliminationCombiningStack<T> implements ConcurrentStack<T> {

    public Collider_RP_SW<T>[] colliders;
    private int threadCount;

    protected ThreadLocal<RangePolicy_AIAD> rangePolicy = new ThreadLocal<RangePolicy_AIAD>() {
        protected synchronized RangePolicy_AIAD initialValue() {
            return new RangePolicy_AIAD(2, threadCount);
        }
    };

    protected ThreadLocal<AdaptiveWaiter> adaptiveWaiter = new ThreadLocal<AdaptiveWaiter>() {
        protected synchronized AdaptiveWaiter initialValue() {
            return new AdaptiveWaiter(200, 20000);
        }
    };

    public ImprovedECStack_Colliders_RP_SW(int nThreads) {
        super(nThreads);
        threadCount = nThreads;
        colliders = (Collider_RP_SW<T>[]) new Collider_RP_SW[nThreads];

        for (int i = 0; i < colliders.length; i++) {
            colliders[i] = new Collider_RP_SW<>(this);
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
