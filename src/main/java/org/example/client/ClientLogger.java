package org.example.client;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.File;

public class ClientLogger {

    public static void log(String CSV_FILE_PATH, int index, long startTimestamp, long endTimestamp, long latency, String batch, String batchOutput) {
        try {
            File file = new File(CSV_FILE_PATH);
            boolean isNewFile = !file.exists();

            try (PrintWriter writer = new PrintWriter(new FileWriter(CSV_FILE_PATH, !isNewFile))) {
                // If it's a new file, add headers
                if (isNewFile) {
                    writer.println("Index,StartTimestamp,EndTimestamp,Latency,Batch,BatchOutput");
                }
                writer.println(index + "," + startTimestamp + "," + endTimestamp + "," + latency + "," + batch + "," + batchOutput);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
