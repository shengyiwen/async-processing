package com.billion.async.processing.monitor;

import com.billion.async.processing.AbstractAsyncHookRegister;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;
import java.util.concurrent.*;

/**
 * @author asheng
 * @since 2018/12/27
 */
public class RegisterMonitor {

    private static final Logger LOGGER = LoggerFactory.getLogger(RegisterMonitor.class);

    private ConcurrentMap<String, AbstractAsyncHookRegister> registerMap = new ConcurrentHashMap<>();

    private static RegisterMonitor INSTANCE = new RegisterMonitor();

    private final ScheduledExecutorService executorService;

    public static RegisterMonitor getInstance() {
        return INSTANCE;
    }

    private RegisterMonitor() {
        executorService = Executors.newScheduledThreadPool(1);
        initTask();
    }

    private void initTask() {
        executorService.schedule(new Runnable() {
            @Override
            public void run() {
                Set<String> keySet = registerMap.keySet();
                if (!keySet.isEmpty()) {
                    for (String key : keySet) {
                        AbstractAsyncHookRegister register = registerMap.get(key);
                        if (register.getCurrentState().isFinalState()) {
                            registerMap.remove(key);
                        } else {
                            if ((System.currentTimeMillis() - register.createTime()) > register.timeout()) {
                                LOGGER.info("checked there is a register not triggered. the uniqueId is: [{}]", key);
                                //registerMap.remove(key);
                            }
                        }
                    }
                }
            }
        }, 500L, TimeUnit.MILLISECONDS);
    }

    public void register(String uniqueId, AbstractAsyncHookRegister register) {
        if (uniqueId != null && register != null
                && register.onSuccess() != null && register.onTimeout() != null) {
            registerMap.put(uniqueId, register);
        }
    }


}
