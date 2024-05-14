package org.example.server;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.File;

public class ServerLogger{
    private static final String LINE_DELIMITER = " & "; // special delimiter for multiline strings

    public static void log(String logFilePath, int index, long startTimestamp, long endTimestamp, long latency, String batch, String batchOutput) {
        try {
            File file = new File(logFilePath);
            boolean isNewFile = !file.exists();

            try (PrintWriter writer = new PrintWriter(new FileWriter(logFilePath, !isNewFile))) {
                // If it's a new file, add headers
                if (isNewFile) {
                    writer.println("Index, StartTimestamp, EndTimestamp, Latency, Batch, BatchOutput");
                }

                // Replace newline characters with the delimiter
                batch = batch.replace("\n", LINE_DELIMITER);
                batchOutput = batchOutput.replace("\n", LINE_DELIMITER);

                writer.println(index + ", " + startTimestamp + ", " + endTimestamp + ", " + latency + ", " + batch + ", " + batchOutput);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}