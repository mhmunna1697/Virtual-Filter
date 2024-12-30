import java.util.Random;


public class VirtualFilter {
    private final boolean[] bitmap;    // Real part of the bitmap
    private final int realSize;        // Size of the real part
    private final int totalSize;       // Total size of the bitmap (real + virtual)
    private int zeroCount;             // Number of zeros in the real part
    private final double samplingProbability; // Target sampling probability
    private final Random random;       // Random number generator


    public VirtualFilter(int realSize, int totalSize, double samplingProbability) {
        this.realSize = realSize;
        this.totalSize = totalSize;
        this.samplingProbability = samplingProbability;
        this.bitmap = new boolean[realSize];
        this.zeroCount = realSize; // Initially, all bits in the real part are zeros
        this.random = new Random();
    }

    
    /**
     * Process an item through the Virtual Filter.
     * @param item The data item to be sampled.
     * @return True if the item is sampled, false otherwise.
     */
    public boolean processItem(String item) {
        int hashValue = hash(item);

        // Step 1: Check if the item falls in the virtual part
        if (hashValue >= realSize) {
            return false; // Item is ignored
        }

        // Step 2: Check for duplicates in the real part
        if (bitmap[hashValue]) {
            return false; // Duplicate detected
        } else {
            bitmap[hashValue] = true; // Mark the bit as used
            zeroCount--;             // Decrease the count of zeros
        }

        // Step 3: Sample with adjusted probability
        double adjustedProbability = (realSize * samplingProbability) / zeroCount;
        if (random.nextDouble() < adjustedProbability) {
            return true; // Item is sampled
        }

        return false; // Item is not sampled
    }


    
    /**
     * Simple hash function.
     * @param item The data item to hash.
     * @return Hash value mod totalSize.
     */
    private int hash(String item) {
        return Math.abs(item.hashCode()) % totalSize;
    }

    /**
     * Reset the filter for a new sampling period.
     */
    public void reset() {
        for (int i = 0; i < realSize; i++) {
            bitmap[i] = false;
        }
        zeroCount = realSize;
    }

    public static void main(String[] args) {
        // Example usage
        int realSize = 1000;          // Real part size
        int totalSize = 1500;         // Total bitmap size
        double samplingProbability = 0.1; // Target sampling probability

        VirtualFilter vf = new VirtualFilter(realSize, totalSize, samplingProbability);

        // Simulate processing items
        String[] items = {"item1", "item2", "item3", "item1", "item4", "item5"};
        for (String item : items) {
            if (vf.processItem(item)) {
                System.out.println(item + " is sampled.");
            } else {
                System.out.println(item + " is not sampled.");
            }
        }
    }
}
