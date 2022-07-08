package todo;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import zknodelock.DistributedLock;

public class Main {

    public static void main(String[] args) {
        ExecutorService executor = Executors.newFixedThreadPool(3);
        for(int i = 0; i < 4; i++) {
            DistributedLock newDL = new DistributedLock();
            executor.execute(newDL);
        }
        executor.shutdown();
        Thread.currentThread().interrupt();
    }
}
