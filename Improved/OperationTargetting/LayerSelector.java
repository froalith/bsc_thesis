package Improved.OperationTargetting;

import java.util.Random;

public class LayerSelector {

    private final int MIN = 20;
    private final int MAX = 100;
    private int preference = 70;
    private final Random random;

    public LayerSelector() {
        random = new Random();
    }

    public boolean select() {
        int value = random.nextInt(MAX);

        if (value < preference) {
            return true;    // choose the stack
        }
        else {
            return false;   // choose the elimination-combining layer
        }
    }

    public void collisionFailed() {
        preference = Math.min(MAX, preference + 5);
    }

    public void stackFailed() {
        preference = Math.max(MIN, preference - 20);
    }

}
