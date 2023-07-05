package Improved.OperationTargetting;

import ECStack.EliminationCombiningStack;
import ECStack.MultiOp;
import ECStack.Status;
import ECStack.ThreadID;
import StackCommons.ConcurrentStack;
import StackCommons.Node;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

public class ImprovedECStack_OT<T> extends EliminationCombiningStack<T> implements ConcurrentStack<T> {
//    volatile boolean useStack;
    DecayingLayerSelector layerSelector = new DecayingLayerSelector(0.6); //TODO was a threadlocal, why? decayrate was 0.33
//{
//        protected synchronized DecayingLayerSelector initialValue() {
//            return new DecayingLayerSelector(0.33);
//        }
//    };
    public int stackcounter =0;
    public int stackFailCounter =0;
    public int collisioncounter =0;
    public int collisionFailCounter = 0;
    public ImprovedECStack_OT(int nThreads) {
        super(nThreads);

    }

    @Override
  public T pop() throws InterruptedException {
//        System.out.println("pop");
        MultiOp<T> mOp = new MultiOp<T>();
        while (true) {
            boolean useStack = layerSelector.select();
//            System.out.println("usestack " + useStack);
            if (useStack) {
                stackcounter++;
                if (multiPop(mOp)) {
                    return mOp.node.value;
                }
                else {
                    stackFailCounter++;
                    layerSelector.stackFailed();
                }
            }
            else {
                collisioncounter++;
                if (collide(mOp)) {
                    return mOp.node.value;
                }
                else {
                    collisionFailCounter++;
                    layerSelector.collisionFailed();
                }
            }
        }
    }

    @Override
    public void push(T value) throws InterruptedException {
        MultiOp<T> mOp = new MultiOp<T>(value);
        while (true) {
            boolean useStack = layerSelector.select();
            if (useStack) {
                stackcounter++;
                if (multiPush(mOp)) {
                    return;
                }
                else layerSelector.stackFailed();
            }
            else {
                collisioncounter++;
                if (collide(mOp)) {
                    return;
                }
                else layerSelector.collisionFailed();
            }
        }
    }

    @Override
    public int[] getCounters(){
        int[] array = {stackcounter, stackFailCounter, collisioncounter, collisionFailCounter};
        stackcounter = 0;
        collisioncounter =0;
        stackFailCounter = 0;
        collisionFailCounter =0;
        return array;
    }

//    public void clearFailures(){
//        layerSelector.clearFailures();
//    }
}
