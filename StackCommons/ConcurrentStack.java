package StackCommons;

public interface ConcurrentStack<T> {

    public void push (T value) throws InterruptedException;

    public T pop() throws InterruptedException;

}
