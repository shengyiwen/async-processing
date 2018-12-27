package com.billion.async.processing.support;

import com.billion.async.processing.AbstractAsyncHookRegister;
import com.billion.async.processing.AsyncHookRegisterState;
import com.billion.async.processing.zookeeper.IDataListener;
import com.billion.async.processing.zookeeper.ZookeeperClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicLong;


/**
 * 使用zk做异步回调
 *
 * @author asheng
 * @since 2018/12/25
 */
public class ZkAsyncHookRegister extends AbstractAsyncHookRegister {

    private static final Logger LOGGER = LoggerFactory.getLogger(ZkAsyncHookRegister.class);

    /** zk服务实例 */
    private final ZookeeperClient client;

    /** 锁服务 */
    private final Object lock = new Object();

    /**
     * 使用此构造方法，不能调用注册方法{@link this#registerHook(String)}
     * 否则会抛出NullPointException
     * 此方法是为了完成触发完成动作的，即{@link this#trigger(String)}
     */
    public ZkAsyncHookRegister(ZookeeperClient client) {
        this(client, 0L, null, null);
    }

    public ZkAsyncHookRegister(ZookeeperClient client, long timeout, Thread successThread, Thread timeoutThread) {
        super(timeout, successThread, timeoutThread);
        if (client == null) {
            throw new NullPointerException("zookeeper client is null.");
        }
        this.client = client;
    }

    /**
     * 注册一个服务到zk
     * 1.如果超时时间 <= 0 的话，此任务将会一直等待，直到被回调
     * 2.如果超时时间 > 0 的话
     * <1>. 如果剩余时间 <= 0了，那么取消任务，将任务置于失败，回调超时处理
     * <2>. 如果超时时间 > 0，那么当前线程会wait剩余时间，在此过程中，如果被回调成功结果，将会notify此线程，此线程结束
     * <3>. 如果超时时间 < 0，那么当前线程会wait剩余时间，在此过程中，如果没有被回调，将会回调超时，而且将此状态置为超时
     *
     *
     * @param uniqueId  唯一标识
     */
    @Override
    public void registerHook(final String uniqueId) {
        if (onSuccess() == null) {
            throw new RuntimeException("successThread is null.");
        }

        if (timeout > 0 && onTimeout() == null) {
            throw new RuntimeException("timeoutThread is null.");
        }

        client.create(uniqueId, false);
        if (timeout <= 0) {
            client.subscribe(uniqueId, new IDataListener() {
                @Override
                public void onSubscribe(int state) {
                    if (state == IDataListener.NODE_DELETED && getCurrentState().isInit()) {
                        complete(uniqueId);
                    }
                }
            });
        } else {
            client.subscribe(uniqueId, new IDataListener() {
                @Override
                public void onSubscribe(int state) {
                    synchronized (lock) {
                        if (state == IDataListener.NODE_DELETED && getCurrentState().isInit()) {
                            complete(uniqueId);
                            lock.notify();
                        }
                    }
                }
            });

            final AtomicLong waitTime = new AtomicLong(timeout - (System.currentTimeMillis() - createTime));
            if (waitTime.get() > 0) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        if (waitTime.get() > 0) {
                            for (; ;) {
                                try {
                                    synchronized (lock) {
                                        lock.wait(waitTime.get());
                                    }
                                } catch (InterruptedException e) {
                                    LOGGER.error("register hook error.", e);
                                }

                                if (!getCurrentState().isInit()) {
                                    break;
                                }

                                waitTime.set(timeout - (System.currentTimeMillis() - createTime));
                                if (waitTime.get() <= 0) {
                                    if (getCurrentState().isInit()) {
                                        cancel(uniqueId);
                                    }
                                    break;
                                }
                            }
                        }
                    }
                }).start();
            } else {
                cancel(uniqueId);
            }
        }
    }

    @Override
    public void trigger(String uniqueId) {
        if (client.exist(uniqueId)) {
            client.delete(uniqueId);
        }
    }

    /**
     * 成功被回调
     * 1.修改状态为完成
     * 2.调用成功线程
     * 3.如果节点存在，删除节点
     *
     * @param uniqueId 唯一标识
     */
    private void complete(String uniqueId) {
        synchronized (lock) {
            setCurrentState(AsyncHookRegisterState.COMPLETED);
            onSuccess().start();
            if (client.exist(uniqueId)) {
                client.delete(uniqueId);
            }
        }
    }

    /**
     * 超时被回调
     * 1.状态置为取消
     * 2.调用超时线程
     * 3.如果节点存在，删除节点
     *
     * @param uniqueId 唯一标识
     */
    private void cancel(String uniqueId) {
        synchronized (lock) {
            setCurrentState(AsyncHookRegisterState.CANCELED);
            onTimeout().start();
            if (this.client.exist(uniqueId)) {
                this.client.delete(uniqueId);
            }
        }
    }

}
