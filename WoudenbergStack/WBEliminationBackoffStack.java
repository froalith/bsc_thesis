package WoudenbergStack;

import StackCommons.Node;
import TreiberStack.TLockFreeStack;

import java.util.EmptyStackException;
import java.util.concurrent.TimeoutException;

public class WBEliminationBackoffStack <T> extends TLockFreeStack<T> {
    static final int capacity = 8;
    WBEliminationArray<T> eliminationArray = new WBEliminationArray<>(capacity - 1);

    static ThreadLocal<WBRangePolicy> policy = new ThreadLocal<WBRangePolicy>() {
        protected synchronized WBRangePolicy initialValue() {
            return new WBRangePolicy(capacity - 1);
        }
    };

    public void push(T value) {
        WBRangePolicy rangePolicy = policy.get();
        Node<T> node = new Node<>(value);

        while (true) {
            if (tryPush(node)) {
                break;
            } else try {
                eliminationArray.visit(value, rangePolicy.getRange());
                rangePolicy.recordEliminationSuccess();
                break;
            } catch (TimeoutException ex) {
                rangePolicy.recordEliminationTimeout();
            }
        }
    }

    @Override
    public T pop() throws EmptyStackException {
        WBRangePolicy rangePolicy = policy.get();
        while (true) {
            Node<T> returnNode = tryPop();
            if (returnNode != null) {
                return returnNode.value;
            }
            else try {
                T otherValue = eliminationArray.visit(null, rangePolicy.getRange());
                rangePolicy.recordEliminationSuccess();
                return otherValue;
            } catch (TimeoutException ex) {
                rangePolicy.recordEliminationTimeout();
            }
        }
    }
}
