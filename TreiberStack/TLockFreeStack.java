package TreiberStack;

import StackCommons.ConcurrentStack;
import StackCommons.Node;

import java.util.EmptyStackException;
import java.util.concurrent.atomic.AtomicReference;

public class TLockFreeStack<T> implements ConcurrentStack<T> {

    AtomicReference<Node<T>> top = new AtomicReference<>(null);
    static final int MIN_DELAY = 1;
    static final int MAX_DELAY = 2048;
    Backoff backoff = new Backoff(MIN_DELAY, MAX_DELAY);

    protected boolean tryPush(Node<T> node) {
        Node<T> oldTop = top.get();
        node.next = oldTop;
        return top.compareAndSet(oldTop, node);
    }

    @Override
    public void push(T value) throws InterruptedException {
        Node<T> node = new Node<T>(value);
        while (true) {
            if (tryPush(node)) {
                return;
            }
            else {
                backoff.backoff();
            }
        }
    }

    protected Node<T> tryPop() throws EmptyStackException {
        Node<T> oldTop = top.get();
        if (oldTop == null) {
            throw new EmptyStackException();
        }

        Node<T> newTop = oldTop.next;
        if (top.compareAndSet(oldTop, newTop)) {
            return oldTop;
        }
        else {
            return null;
        }
    }

    @Override
    public T pop() throws EmptyStackException, InterruptedException {
        while(true) {
            Node<T> returnNode = tryPop();
            if (returnNode != null) {
                return returnNode.value;
            }
            else {
                backoff.backoff();
            }
        }
    }
}
