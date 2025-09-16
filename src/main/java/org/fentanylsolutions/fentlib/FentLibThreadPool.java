package org.fentanylsolutions.fentlib;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

public class FentLibThreadPool {

    /**
     * Create a single-threaded executor with a custom thread name.
     * Can be reused for other purposes.
     */
    public static ExecutorService createSingleThreadPool(String name) {
        return Executors.newSingleThreadExecutor(r -> {
            Thread t = new Thread(r, name);
            t.setDaemon(true);
            return t;
        });
    }

    /**
     * Optional: create a fixed-size pool with custom names
     */
    public static ExecutorService createFixedThreadPool(int size, String baseName) {
        return Executors.newFixedThreadPool(size, new ThreadFactory() {

            private int counter = 0;

            @Override
            public Thread newThread(Runnable r) {
                Thread t = new Thread(r, baseName + "-" + counter++);
                t.setDaemon(true);
                return t;
            }
        });
    }
}
