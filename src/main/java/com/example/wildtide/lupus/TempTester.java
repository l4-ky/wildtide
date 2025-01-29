package com.example.wildtide.lupus;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
public class TempTester {
    public static void main(String[] args) {
        // Create a CountDownLatch initialized to 1
        CountDownLatch latch = new CountDownLatch(1);

        // Create a new thread that will perform an action
        Thread actionThread = new Thread(() -> {
            try {
                // Simulate some work with sleep
                System.out.println("Performing action...");
                Thread.sleep(10000); // Simulate a delay of 2 seconds
                System.out.println("Action performed!");
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } finally {
                // Count down the latch to signal that the action is complete
                latch.countDown();
            }
        });

        // Start the action thread
        actionThread.start();

        try {
            // Wait for the action to be performed for up to 5 seconds
            if (latch.await(5, TimeUnit.SECONDS)) {
                System.out.println("Action completed within the time limit.");
            } else {
                System.out.println("Timed out waiting for the action.");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.out.println("Waiting was interrupted.");
        }

        // Optionally, join the action thread to clean up
        try {
            actionThread.join();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}