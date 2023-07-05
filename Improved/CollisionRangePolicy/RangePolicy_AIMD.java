package Improved.CollisionRangePolicy;

import java.util.Random;

public class RangePolicy_AIMD {

    private final int FAIL_THRESH = 10;
    private final int SUCCESS_THRESH = 10;
    private final int MIN_RANGE;
    private final int MAX_RANGE;
    private int range;
    private int failCount;
    private int successCount;
    private final Random random;


    public RangePolicy_AIMD(int minRange, int maxRange) {
        MIN_RANGE = minRange;
        range = minRange;
        MAX_RANGE = maxRange;

        failCount = 0;
        successCount = 0;
        random = new Random();
    }

    public int randomIndex() {
        return random.nextInt(range);
    }

    public void registerFailure() {
        failCount++;

        if (failCount >= FAIL_THRESH) {
            range = Math.min(range + 1, MAX_RANGE);
            failCount = 0;
        }
    }

    public void registerSuccess() {
        successCount++;

        if (successCount >= SUCCESS_THRESH) {
            range = Math.max(range / 2, MIN_RANGE);
            successCount = 0;
        }
    }

}
