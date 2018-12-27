package com.billion.async.processing;

import com.billion.async.processing.support.ZkAsyncHookRegister;
import com.billion.async.processing.zookeeper.ZookeeperClient;
import com.billion.async.processing.zookeeper.zkclient.ZkclientZookeeperClient;
import org.junit.Before;
import org.junit.Test;

/**
 * @author asheng
 * @since 2018/12/27
 */
public class AsyncHookRegisterTest {


    private AsyncHookRegister register;

    @Before
    public void setUp() {
        ZookeeperClient zkClient = new ZkclientZookeeperClient("localhost:2181");
        register = new ZkAsyncHookRegister(zkClient, 5000L, new Thread(new Runnable() {
            @Override
            public void run() {
                System.out.println(" ============= i'm success !!! =================== ");
                synchronized (AsyncHookRegisterTest.class) {
                    AsyncHookRegisterTest.class.notify();
                }
            }
        }), new Thread(new Runnable() {
            @Override
            public void run() {
                System.out.println(" ============ i'm timeout !!! =================== ");
            }
        }));
    }

    @Test
    public void registerTimeout() {
        register.registerHook("123456");
    }

    @Test
    public void registerSuccess() throws InterruptedException {
        register.registerHook("123456");
        Thread.sleep(4500L);
        register.trigger("123456");
        synchronized (AsyncHookRegisterTest.class) {
            AsyncHookRegisterTest.class.wait();
        }
        //System.out.println("hello game over");
    }

}
