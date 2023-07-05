package HSYStack;

import StackCommons.ConcurrentStack;
import StackCommons.Node;
import TreiberStack.*;
import WoudenbergStack.WBRangePolicy;

import java.util.EmptyStackException;
import java.util.concurrent.TimeoutException;

public class HSYEliminationBackoffStack<T> extends TLockFreeStack<T> implements ConcurrentStack<T> {
    HSYEliminationArray<T> eliminationArray;
    private final int capacity = 8;
    static WBRangePolicy policy;   // TODO check whether Range Policy is the same for HSY and WB stacks
    private int stackCounter=0;
    private int stackFailCounter =0;
    private int collisionCounter = 0;
    private int collisionFailCounter =0;
    public HSYEliminationBackoffStack() {
        eliminationArray = new HSYEliminationArray<>(capacity - 1);
        policy = new WBRangePolicy(capacity -1);
//    {
//            protected synchronized WBRangePolicy initialValue() {
//                return new WBRangePolicy(capacity - 1);
//            }
//        };
    }

    @Override
    public void push(T value) {
        WBRangePolicy rangePolicy = policy;
        Node<T> node = new Node<>(value);
        while (true) {
            if (tryPush(node)) {
                stackCounter++;
                return;
            }
            else try {
                stackFailCounter++;
                T otherValue = eliminationArray.visit(value, rangePolicy.getRange());
                if (otherValue == null) {
                    collisionCounter++;
                    rangePolicy.recordEliminationSuccess();
                    return;
                }
            } catch (TimeoutException ex) {
                collisionFailCounter++;
                rangePolicy.recordEliminationTimeout();
            }
        }
    }

    @Override
    public T pop() throws EmptyStackException {
        WBRangePolicy rangePolicy = policy;
        while (true) {
            Node<T> returnNode = tryPop();
            if (returnNode != null) {
                stackCounter++;
                return returnNode.value;
            }
            else try {
                stackFailCounter++;
                T otherValue = eliminationArray.visit(null, rangePolicy.getRange());
                if (otherValue != null) {
                    collisionCounter++;
                    rangePolicy.recordEliminationSuccess();
                    return otherValue;
                }
            } catch (TimeoutException ex) {
                collisionFailCounter++;
                rangePolicy.recordEliminationTimeout();
            }
        }
    }
    public int[] getCounters() {
        int[] array = {stackCounter, stackFailCounter, collisionCounter, collisionFailCounter};
        stackCounter = 0;
        collisionCounter =0;
        stackFailCounter = 0;
        collisionFailCounter =0;
        return array;
    }

}


