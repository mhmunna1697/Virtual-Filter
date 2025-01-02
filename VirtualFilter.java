import java.io.*;
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

    public String processItem(String source, String destination) {
        String item = source + "," + destination;
        int hashValue = hash(item);

        // Step 1: Check if the item falls in the virtual part
        if (hashValue >= realSize) {
            return "Not Sampled"; // Item is ignored
        }

        // Step 2: Check for duplicates in the real part
        if (bitmap[hashValue]) {
            return "Not Sampled"; // Duplicate detected
        } else {
            bitmap[hashValue] = true; // Mark the bit as used
            zeroCount--;             // Decrease the count of zeros
        }

        // Step 3: Sample with adjusted probability
        double adjustedProbability = (realSize * samplingProbability) / zeroCount;
        if (random.nextDouble() < adjustedProbability) {
            return "Sampled";
        }

        return "Not Sampled";
    }

    private int hash(String item) {
        return Math.abs(item.hashCode()) % totalSize;
    }

    public static void main(String[] args) {
        // Virtual Filter Configuration
        int realSize = 1000000;         // Real part size
        int totalSize = 1500000;        // Total bitmap size (real + virtual)
        double samplingProbability = 0.01; // Target sampling probability
        VirtualFilter vf = new VirtualFilter(realSize, totalSize, samplingProbability);

        // Input and output file paths
        String inputFile = "/u/spa-d4/grad/mmu369/Desktop/Research/caida/o1.txt";
        String outputFile = "output.txt"; 

        try (BufferedReader br = new BufferedReader(new FileReader(inputFile));
             BufferedWriter bw = new BufferedWriter(new FileWriter(outputFile))) {

            String line;
            long processedCount = 0;

            while ((line = br.readLine()) != null) {
                // Parse source and destination IPs
                String[] parts = line.split("\t");
                if (parts.length != 2) {
                    System.err.println("Invalid line format: " + line);
                    continue;
                }
                String source = parts[0].trim();
                String destination = parts[1].trim();

                // Process the pair and get the result
                String result = vf.processItem(source, destination);

                // Write the result to the output file
                bw.write(source + "\t" + destination + "\t" + result);
                bw.newLine();

                // Print progress every 1,000,000 lines
                if (++processedCount % 1000000 == 0) {
                    System.out.println("Processed " + processedCount + " rows...");
                }
            }

            System.out.println("Processing completed. Total rows processed: " + processedCount);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
