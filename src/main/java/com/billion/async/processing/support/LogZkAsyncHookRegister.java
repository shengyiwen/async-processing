package com.billion.async.processing.support;

import com.billion.async.processing.zookeeper.ZookeeperClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author asheng
 * @since 2018/12/27
 */
public class LogZkAsyncHookRegister extends ZkAsyncHookRegister {

    private static final Logger LOGGER = LoggerFactory.getLogger(LogZkAsyncHookRegister.class);

    public LogZkAsyncHookRegister(ZookeeperClient client) {
        this(client, 0L);
    }

    public LogZkAsyncHookRegister(ZookeeperClient client, long timeout) {
        super(client, timeout, new Thread(new Runnable() {
            @Override
            public void run() {
                LOGGER.info("trigger on success");
            }
        }), new Thread(new Runnable() {
            @Override
            public void run() {
                LOGGER.info("trigger on timeout");
            }
        }));
    }
}
