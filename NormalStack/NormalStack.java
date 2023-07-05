package NormalStack;

import StackCommons.ConcurrentStack;
import StackCommons.Node;

import java.util.EmptyStackException;
import java.util.Vector;

public class NormalStack<T> extends Vector<T> implements ConcurrentStack<T> {

    @Override
    public synchronized T pop() throws InterruptedException {
        T       obj;
        int     len = size();

        obj = peek();
        removeElementAt(len - 1);

        return obj;
    }

    @Override
    public synchronized void push(T item) throws InterruptedException {
        addElement(item);
    }
    public T peek() {
        int     len = size();

        if (len == 0)
            throw new EmptyStackException();
        return elementAt(len - 1);
    }
}
