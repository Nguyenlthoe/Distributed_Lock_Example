package todo;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import zknodelock.DistributedLock2;

public class Main2 {

    public static void main(String[] args) {
        ExecutorService executor = Executors.newFixedThreadPool(3);
        for(int i = 0; i < 4; i++) {
            DistributedLock2 newDL = new DistributedLock2();
            executor.execute(newDL);
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        executor.shutdown();
        Thread.currentThread().interrupt();
    }
}
