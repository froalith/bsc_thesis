package Improved.CollisionRangePolicy;

import java.util.Random;

public class RangePolicy_AIAD {

    private final int FAIL_THRESH = 10;
    private final int SUCCESS_THRESH = 10;
    private final int MIN_RANGE;
    private final int MAX_RANGE;
    private int range;
    private int failCount;
    private int failcounttoo = 0;
    private int successcounttoo =0;
    private int successCount;
    private final Random random;


    public RangePolicy_AIAD(int minRange, int maxRange) {
        MIN_RANGE = minRange;
        range = minRange;
        MAX_RANGE = maxRange;
        try {
            if (minRange < 0) {
                throw new InterruptedException("range < 0");
            }
        }catch (InterruptedException ie){}

        failCount = 0;
        successCount = 0;
        random = new Random();
    }

    public int randomIndex() {
        return random.nextInt(range);
    }

    public void increaseRange() {
        failCount++;
        failcounttoo++;

        if (failCount >= FAIL_THRESH) {
            range = Math.min(range + 1, MAX_RANGE);
            failCount = 0;
        }
    }

    public void decreaseRange() {
        successCount++;
        successcounttoo++;

        if (successCount >= SUCCESS_THRESH) {
            range = Math.max(range - 1, MIN_RANGE);
            successCount = 0;
        }
    }
    public void printCounters(){
        System.out.println("rangepollicy fail count: " + failcounttoo + " successcount " +  successcounttoo);
        failcounttoo = 0;
        successcounttoo =0;
    }
}
