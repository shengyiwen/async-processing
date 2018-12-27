package com.billion.async.processing;

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
     * {@link AsyncHookRegisterState#CREATED}   刚被创建状态
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
    protected synchronized AsyncHookRegisterState getCurrentState() {
        return this.currentState;
    }

}
