package zknodelock;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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
    private String nodeEphemeralPath;
    private int number;
    private boolean done = true;
    private boolean start = false;
    private String nodeWatcher;

    public DistributedLock() {
        // TODO Auto-generated constructor stub
        count++;
        number = count;
        try {
            zkNode = new ZooKeeper(hostPort, 2000, this);
            try {
//                if (zkNode.exists(nodeLockPath, this) == null) {
                // Tạo nút trong hàng đợi thực thi.
                nodeEphemeralPath = zkNode.create(nodeLockPath + "/queue", "".getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE,
                        CreateMode.EPHEMERAL_SEQUENTIAL);
                // Lấy danh sách các khóa
                List<String> listPathChildren = zkNode.getChildren(nodeLockPath, false);
                Collections.sort(listPathChildren);
                for (int i = 0; i < listPathChildren.size(); i++) {
                    String comparePath = nodeLockPath + "/" + listPathChildren.get(i);
                    if (comparePath.equals(nodeEphemeralPath)) {
                        if (i == 0) {
                            start = true;
                            nodeWatcher = "/none";
                        } else {
                            // Thêm watcher vào znode đằng trước nó.
                            nodeWatcher = nodeLockPath + "/" + listPathChildren.get(i - 1);
                            zkNode.addWatch(nodeWatcher, AddWatchMode.PERSISTENT);
                            // Kiểm tra lại xem node Watcher còn tồn tại trong hàng đợi không, nếu không nút
                            // này được chạy luôn.
                            listPathChildren = zkNode.getChildren(nodeLockPath, false);
                            String[] nodePaths = nodeWatcher.split("/");
                            if (!listPathChildren.contains(nodePaths[nodePaths.length - 1])) {
                                start = true;
                            }
                        }
                        break;
                    }
                }
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
        if (start == true) { // LUồng được chạy ngay do không cần phải đợi.
            try {
                System.out.println("*************************");
                System.out.println("Thread Program1 " + number + " is holding lock");
                System.out.println("*************************");
                Thread.sleep(10000);
                zkNode.delete(nodeEphemeralPath, -1);
//                zkNode.removeAllWatches(nodeWatcher, WatcherType.Any, true);
                System.out.println("Thread Program1 " + number + " shutdown");
                zkNode.close();
                zkNode = null;
            } catch (InterruptedException | KeeperException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            this.done = false;
        }
        // TODO Auto-generated method stubs
        try {
            synchronized (this) {
                while (this.done) {
                    wait();
                }
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
            Thread.currentThread().interrupt();
        }
    }

    public void process(WatchedEvent event) {
        System.out.println("Event received: " + event.toString());
        System.out.println("Event: " + event.getPath());
        ArrayList<String> a = new ArrayList<>();
//        Sự kiện kích hoạt khi nút tạo ngay trước nó bị xóa.
        if (event.getType() == Event.EventType.NodeDeleted) {
            try {
                System.out.println("*************************");
                System.out.println("Thread Program1 " + number + " is holding lock");
                System.out.println("*************************");
                Thread.sleep(10000);
                zkNode.delete(nodeEphemeralPath, -1);// Xóa nút, kích hoạt watcher của nút tiếp theo.
//                zkNode.removeAllWatches(nodeWatcher, WatcherType.Any, true);
                System.out.println("Thread Program1 " + number + " shutdown");
                zkNode.close();
                zkNode = null;
                this.done = false;
            } catch (Exception e) {
                System.out.println("Error: " + e.getMessage());
                try {
                    zkNode.addWatch(nodeLockPath, AddWatchMode.PERSISTENT);
                } catch (KeeperException | InterruptedException e1) {
                    // TODO Auto-generated catch block
//                    e1.printStackTrace();
                }
            }
        }
    }

    public void printNodeWatcher() {
        System.out.println(nodeWatcher);
    }

}
