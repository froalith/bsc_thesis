package ECStack;

import StackCommons.Node;

import java.util.concurrent.locks.Condition;

public class MultiOp<T> {
    public int id;
    public Type op;
    public int length;
    public ECStack.Status status;
    Condition statusChanged;
    public Node<T> node;
    public MultiOp<T> next;
    public MultiOp<T> last;

    public MultiOp() {
        id = ThreadID.get();
        length = 1;
        op = Type.POP;
        status = Status.INIT; // Is this right?
        node = new Node<T>(null);
        next = null;
        last = this;
    }

    public MultiOp(T value) {
        this();
        node = new Node<T>(value);
        op = Type.PUSH;
    }

    public enum Type {
        PUSH,
        POP
    }
}
