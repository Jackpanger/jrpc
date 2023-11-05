package jack;

import com.jackpang.netty.CustomWatcher;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.ZooKeeper;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

/**
 * description: ZookeeperTest
 * date: 11/3/23 4:44 PM
 * author: jinhao_pang
 * version: 1.0
 */
public class ZookeeperTest {
    ZooKeeper zooKeeper;

    @Before
    public void createZk() throws IOException {

    }

    @Test
    public void createNode() {
        String path = "/test";
        byte[] data = "hello".getBytes();
        String result = null;
        try {
            result = zooKeeper.create(path, data, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
        } catch (KeeperException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            try {
                if (zooKeeper != null)
                    zooKeeper.close();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        System.out.println(result);

    }

    @Test
    public void deleteNode() {
        String path = "/test";
        String result = null;
        try {
            zooKeeper.delete(path, -1);
        } catch (KeeperException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            try {
                if (zooKeeper != null)
                    zooKeeper.close();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        System.out.println(result);

    }
}
