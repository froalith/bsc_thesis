package ECStack;

import StackCommons.ConcurrentStack;
import StackCommons.Node;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.atomic.AtomicStampedReference;

public class EliminationCombiningStack<T> implements ConcurrentStack<T> {
    protected AtomicReference<Node<T>> top;
    protected AtomicInteger[] collision;
    protected AtomicReference<MultiOp<T>>[] location;

    protected int stackCounter = 0;
    protected int collisionCounter =0;
    protected int stackFailCounter = 0;
    protected int collisionFailCounter =0;
    protected final static int EMPTY = -1;
    protected final Random rand;

    public EliminationCombiningStack(int nThreads) {
        top = new AtomicReference<>(null);
        collision = new AtomicInteger[nThreads];
        location = (AtomicReference<MultiOp<T>>[]) new AtomicReference[nThreads];

        for (int i = 0; i < nThreads; i++) {
            collision[i] = new AtomicInteger(EMPTY);
            location[i] = new AtomicReference<>(null);
        }

        rand = new Random();
    }

    @Override
    public T pop() throws InterruptedException {
        MultiOp<T> mOp = new MultiOp<T>();
        while (true) {
            if (multiPop(mOp)) {
                stackCounter++;
                return mOp.node.value;
            }
            else{
                stackFailCounter++;
            }
            if (collide(mOp)) {
                collisionCounter++;
                return mOp.node.value;
            }
            else{
                collisionFailCounter++;
            }
        }
    }

    @Override
    public void push(T value) throws InterruptedException {
        MultiOp<T> mOp = new MultiOp<T>(value);
        while (true) {
            if (multiPush(mOp)) {
                stackCounter++;
                return;
            }
            else{
                stackFailCounter++;
            }
            if (collide(mOp)) {
                collisionCounter++;
                return;
            }
            else{
                collisionFailCounter++;
            }
        }
    }

    public boolean multiPop(MultiOp<T> mOp) {
        Node<T> oldTop = top.get();

        // If the stack is empty, mark all pop's as finished and return true
        if (oldTop == null) {
            do {
                mOp.node = new Node<T>(null);
                mOp.status = Status.FINISHED;
                mOp = mOp.next;
            } while (mOp != null);
            return true;
        }

        // If the stack is not empty, find all the elements that we can pop
        Node<T> newTop = oldTop.next;
        int m = 1;
        while (newTop != null && m < mOp.length) {
            newTop = newTop.next;
            m++;
        }

        // Try to set the top to the element AFTER the last one we want to pop
        if (top.compareAndSet(oldTop, newTop)) {
            mOp.node = oldTop;
            oldTop = oldTop.next;

            // Assign the values that we "claimed" to each of the pop's one by one
            while (mOp.next != null) {

                if (oldTop == null) {
                    mOp.next.node = new Node<>(null);
                }
                else {
                    mOp.next.node = oldTop;
                    oldTop = oldTop.next;
                }
                mOp.next.status = Status.FINISHED;
                mOp.next = mOp.next.next;
            }
            return true;
        }
        // Otherwise, the top has moved, and we'll try again later
        else return false;
    }

    public boolean multiPush(MultiOp<T> mOp) {
        Node<T> oldTop = top.get();     // save the original top node
        mOp.last.node.next = oldTop;    // mark the old top as the next thing after the group we want to push (concatenation basically)

        if (top.compareAndSet(oldTop, mOp.node)) {      // try to replace the top with the first cell in the MultiOp
            // Mark all the pushes as finished
            while (mOp.next != null) {
                mOp.next.status = Status.FINISHED;
                mOp.next = mOp.next.next;
            }
            return true;
        }
        // If the CAS fails, the top changed, and we have to try again
        else return false;
    }

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

        try {Thread.sleep(0, 1000);} catch (InterruptedException ex){} // TODO do something smarter here - possible improvement?

        if (!location[ThreadID.get()].compareAndSet(mOp, null)) {
            return passiveCollide(mOp);
        }
        return false;
    }

    public boolean activeCollide(MultiOp<T> aInf, MultiOp<T> pInf) {
        if (location[pInf.id].compareAndSet(pInf, aInf)) {
            if (aInf.op == pInf.op) {
                combine(aInf, pInf);
                return false;
            }
            else {
                multiEliminate(aInf,pInf);
                return true;
            }
        }
        else {
            return false;
        }
    }

    public boolean passiveCollide(MultiOp<T> pInf) {
        MultiOp<T> aInf = location[pInf.id].get();
        location[pInf.id].set(null);

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

    public void combine(MultiOp<T> aInf, MultiOp<T> pInf) {
        if (aInf.op == MultiOp.Type.PUSH) {
            aInf.last.node.next = pInf.node;
        }

        aInf.last.next = pInf;
        aInf.last = pInf.last;
        aInf.length = aInf.length + pInf.length;
    }

    public void multiEliminate(MultiOp<T> aInf, MultiOp<T> pInf) {
        MultiOp<T> aCurr = aInf;
        MultiOp<T> pCurr = pInf;

        do {
            if (aInf.op == MultiOp.Type.POP) {
                aCurr.node = pCurr.node;
            }
            else {
                pCurr.node = aCurr.node;
            }

            aCurr.status = Status.FINISHED;
            pCurr.status = Status.FINISHED;
            aInf.length = aInf.length - 1;
            pInf.length = pInf.length - 1;
            aCurr = aCurr.next;
            pCurr = pCurr.next;
          
        } while (aCurr != null && pCurr != null);

        if (aCurr != null) {
            aCurr.length = aInf.length;
            aCurr.last = aInf.last;
            aCurr.status = Status.RETRY;
        }
        else if (pCurr != null) {
            pCurr.length = pInf.length;
            pCurr.last = pInf.last;
            pCurr.status = Status.RETRY;
        }
    }

    private void checkMOP(MultiOp<T> mOp) {
        Set<MultiOp<T>> elements = new HashSet<>();
        MultiOp<T> current = mOp;
        while (current != null) {
            if (elements.contains(current)) {
                System.out.println("MOP Loop Found!");
                break;
            }
            elements.add(current);
            current = current.next;
        }
    }
    public int[] getCounters(){
        int[] array = {stackCounter, stackFailCounter, collisionCounter, collisionFailCounter};
        stackCounter = 0;
        collisionCounter =0;
        stackFailCounter = 0;
        collisionFailCounter =0;
        return  array;
    }
}
