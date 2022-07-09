package todo;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import zknodelock.DistributedLock;

public class Main {

    public static void main(String[] args) {
        ExecutorService executor = Executors.newFixedThreadPool(3);
        DistributedLock[] newDL = new DistributedLock[5];
        for(int i = 0; i < 4; i++) {
            newDL[i] = new DistributedLock();
            executor.execute(newDL[i]);
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        for(int i = 0; i < 4; i++) {
            newDL[i].printNodeWatcher();
        }
        executor.shutdown();
        Thread.currentThread().interrupt();
    }
}
