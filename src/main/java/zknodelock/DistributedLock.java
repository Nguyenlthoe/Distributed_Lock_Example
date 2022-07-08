package zknodelock;

import java.io.IOException;

import org.apache.zookeeper.AddWatchMode;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.ZooKeeper;

public class DistributedLock implements Runnable, Watcher {
    private final String hostPort = "localhost:2181,localhost:2182,localhost:2183";
    private final static String nodeLockPath = "/DistributedLock";
    private ZooKeeper zkNode;
    private static int count = 0;
    private int number;
    private boolean done = true;
    public DistributedLock() {
        // TODO Auto-generated constructor stub
        count++;
        number = count;
        try {
            zkNode = new ZooKeeper(hostPort, 2000, this);
            try {
//                if (zkNode.exists(nodeLockPath, this) == null) {
                    // Tạo nút trong hàng đợi thực thi.
                    zkNode.create(nodeLockPath + "/queue" + number, "".getBytes(),
                    ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL);
                    // Thêm watcher vào znode đằng trước nó.
                    zkNode.addWatch(nodeLockPath + "/queue" + (number - 1), AddWatchMode.PERSISTENT);
//                }
            } catch (KeeperException | InterruptedException e) {
                e.printStackTrace();
            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        System.out.println("Thread " + number + "start");
    }
    
    public void run() {
        if(number == 1) {
            try {
                System.out.println("*************************");
                System.out.println("Thread " + number + " is holding lock");
                System.out.println("*************************");
                Thread.sleep(2000);
                zkNode.delete(nodeLockPath + "/queue" + number, -1);
                System.out.println("Thread " + number + " shutdown");
                zkNode.close();
            } catch (InterruptedException | KeeperException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            this.done = false;
        }
        // TODO Auto-generated method stubs
//        try {
            synchronized (this) {
                while (this.done) {
//                    wait();
                }
            }
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//            Thread.currentThread().interrupt();
//        }
    }

    public void process(WatchedEvent event) {
        System.out.println("Event received: " + event.toString());
        System.out.println("Event: " + event.getPath());
//        Sự kiện kích hoạt khi nút đằng trước nó bị xóa.
        if(event.getType() == Event.EventType.NodeDeleted) {
            try {
                System.out.println("*************************");
                System.out.println("Thread " + number + " is holding lock");
                System.out.println("*************************");
                Thread.sleep(10000);
                zkNode.delete(nodeLockPath + "/queue" + number, -1);
                System.out.println("Thread " + number + " shutdown");
                zkNode.close();
                this.done = false;
            } catch (Exception e) {
//                System.out.println("Error: " + e.getMessage());
                try {
                    zkNode.addWatch(nodeLockPath, AddWatchMode.PERSISTENT_RECURSIVE);
                } catch (KeeperException | InterruptedException e1) {
                    // TODO Auto-generated catch block
//                    e1.printStackTrace();
                }
            }
        }
    }

}
