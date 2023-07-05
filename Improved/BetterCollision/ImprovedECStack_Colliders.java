package Improved.BetterCollision;

import ECStack.EliminationCombiningStack;
import ECStack.MultiOp;
import ECStack.Status;
import StackCommons.ConcurrentStack;

public class ImprovedECStack_Colliders<T> extends EliminationCombiningStack<T> implements ConcurrentStack<T> {

    public Collider<T>[] colliders;

    public ImprovedECStack_Colliders(int nThreads) {
        super(nThreads);
        colliders = (Collider<T>[]) new Collider[nThreads];

        for (int i = 0; i < colliders.length; i++) {
            colliders[i] = new Collider<>(this);
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
        int index = rand.nextInt(colliders.length);
        return colliders[index].collide(mOp);
    }
}
