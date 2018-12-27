package com.billion.async.processing;

import com.billion.async.processing.monitor.RegisterMonitor;

/**
 * @author asheng
 * @since 2018/12/27
 */
public abstract class AbstractAsyncHookRegister implements AsyncHookRegister {

    /** 超时时间 */
    protected final long timeout;

    /** 创建时间，用于做超时判断 */
    protected final long createTime;

    /** 成功回调线程 */
    private final Thread successThread;

    /** 超时回调线程 */
    private final Thread timeoutThread;

    /**
     * 当前注册回调的状态
     *
     * {@link AsyncHookRegisterState#CREATED}   刚被创建状态，可以注册回调任务
     * {@link AsyncHookRegisterState#READY}     准备状态，不能注册回调任务
     * {@link AsyncHookRegisterState#COMPLETED} 成功线程被回调的状态
     * {@link AsyncHookRegisterState#CANCELED}  超时线程被回调的状态
     */
    private AsyncHookRegisterState currentState;

    /**
     * 使用此构造方法，不能调用注册方法{@link this#registerHook(String)}
     * 否则会抛出NullPointException
     * 此方法是为了完成触发完成动作的，即{@link this#trigger(String)}
     */
    public AbstractAsyncHookRegister() {
        this(0L, null, null);
    }

    public AbstractAsyncHookRegister(long timeout, Thread successThread, Thread timeoutThread) {
        this.timeout = timeout;
        this.successThread = successThread;
        this.timeoutThread = timeoutThread;
        this.currentState = AsyncHookRegisterState.CREATED;
        this.createTime = System.currentTimeMillis();
    }

    @Override
    public Thread onSuccess() {
        return this.successThread;
    }

    @Override
    public Thread onTimeout() {
        return this.timeoutThread;
    }

    @Override
    public long timeout() {
        return this.timeout;
    }

    public long createTime() {
        return this.createTime;
    }

    @Override
    public void registerHook(String uniqueId) {
        if (onSuccess() == null) {
            throw new RuntimeException("successThread is null.");
        }

        if (timeout > 0 && onTimeout() == null) {
            throw new RuntimeException("timeoutThread is null.");
        }

        if (!getCurrentState().isInit()) {
            throw new RuntimeException("the register only can be used once");
        }

        setCurrentState(AsyncHookRegisterState.READY);
        RegisterMonitor.getInstance().register(uniqueId, this);
        register(uniqueId);
    }

    protected abstract void register(String uniqueId);

    /**
     * 修改当前状态，线程安全
     *
     * @param state 即将修改的状态
     */
    protected synchronized void setCurrentState(AsyncHookRegisterState state) {
        this.currentState = state;
    }

    /**
     * 获取当前系统状态，线程安全
     *
     * @return 当前系统状态
     */
    public synchronized AsyncHookRegisterState getCurrentState() {
        return this.currentState;
    }

}
