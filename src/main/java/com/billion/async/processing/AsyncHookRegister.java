package com.billion.async.processing;

/**
 * 异步回调注册中心
 *
 * @author asheng
 * @since 2018/12/24
 */
public interface AsyncHookRegister {

    /**
     * 注册异步回调，如果成功处理将会调用onCompletion的方法
     * 如果超时将会调用onTimeout的方法
     * 如果timeout <=0 的话，不会超时
     *
     * @param uniqueId 唯一标识
     */
    void registerHook(String uniqueId);

    /**
     * 超时时间
     *
     * @return 超时时间
     */
    long timeout();

    /**
     * 成功回调的线程
     *
     * @return {@link Thread}
     */
    Thread onSuccess();

    /**
     * 超时回调的线程
     *
     * @return {@link Thread}
     */
    Thread onTimeout();
}
