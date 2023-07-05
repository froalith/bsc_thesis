package WoudenbergStack;

public class WBRangePolicy {
    int eliminationArrayWidth, range, successCounter, timeoutCounter;

    public WBRangePolicy(int eliminationArrayWidth) {
        this.eliminationArrayWidth = eliminationArrayWidth;
        range = eliminationArrayWidth;
        successCounter = 0;
        timeoutCounter = 0;
    }

    public void recordEliminationSuccess() {
        successCounter++;
        if (successCounter > 5) {
            successCounter = 0;
            if (range < eliminationArrayWidth - 1) {
                range++;
            }
        }
    }

    public void recordEliminationTimeout() {
        timeoutCounter++;
        if (timeoutCounter > 10) {
            timeoutCounter = 0;
            if (range > 1) {
                range--;
            }
        }
    }

    public int getRange() {
        return range;
    }
}
