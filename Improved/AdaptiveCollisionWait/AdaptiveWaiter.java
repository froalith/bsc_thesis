package Improved.AdaptiveCollisionWait;

public class AdaptiveWaiter {

    private final int MIN_WAIT;
    private final int MAX_WAIT;
    private int waitDuration;
    private int successCounter = 0;
    private int failCounter = 0;
    private final int THRESH = 5;

    public AdaptiveWaiter(int min, int max) {
        MIN_WAIT = min;
        MAX_WAIT = max;
        waitDuration = (min + max) / 2;
    }

    public void increaseWait() {
        successCounter++;
        if (successCounter >= THRESH) {
            successCounter = 0;
            waitDuration = Math.min(waitDuration + 100, MAX_WAIT);
        }
    }

    public void decreaseWait() {
        failCounter++;
        if (failCounter >= THRESH) {
            failCounter = 0;
            waitDuration = Math.max (waitDuration - 100, MIN_WAIT);
        }
    }

    public void awaitCollider() {
        try {
            Thread.sleep(0, waitDuration);
        } catch (InterruptedException ex) {}
    }

}
