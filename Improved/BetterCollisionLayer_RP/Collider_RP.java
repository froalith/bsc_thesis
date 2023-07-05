package Improved.BetterCollisionLayer_RP;

import ECStack.MultiOp;

import java.util.concurrent.atomic.AtomicStampedReference;

public class Collider_RP<T> {
    AtomicStampedReference<MultiOp<T>> operation;
    ImprovedECStack_Colliders_RP<T> stack;

    public static final int FREE = 0, WAITING_ACTIVE = 1, FULL = 2;

    public Collider_RP(ImprovedECStack_Colliders_RP<T> stack) {
        operation = new AtomicStampedReference<>(null, FREE);
        this.stack = stack;
    }

    public boolean collide(MultiOp<T> mOp) {
        MultiOp<T> other;
        int[] stampHolder = {-1};
        if (operation.getStamp() == FULL) {
            stack.rangePolicy.get().increaseRange();
            return stack.collide(mOp);     // If collider is full find another one
        }

        // If the collider is empty, try to become the passive collider
        if (operation.compareAndSet(null, mOp, FREE, WAITING_ACTIVE)) {
            other = operation.get(stampHolder);
            if (stampHolder[0] == FULL) {
                operation.set(null, FREE); // cleanup before leaving
                return stack.passiveCollide(mOp, other); // do the collision
            }

            try {Thread.sleep(0, 1000);} catch (InterruptedException ex) {}

            if (operation.compareAndSet(mOp, null, WAITING_ACTIVE, FREE)) {
                stack.rangePolicy.get().decreaseRange();
                return false;
            }
            other = operation.getReference();
            operation.set(null, FREE);
            return stack.passiveCollide(mOp, other);
        }

        // Otherwise, try to become the active collider
        other = operation.getReference();
        if (operation.compareAndSet(other, mOp, WAITING_ACTIVE, FULL)) {
            return stack.activeCollide(mOp, other);
        }
        return false;

    }


}
