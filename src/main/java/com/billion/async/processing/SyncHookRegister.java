package com.billion.async.processing;

/**
 * @author asheng
 * @since 2019/1/2
 */
public interface SyncHookRegister extends HookRegister {

    SyncHookFunction onSuccess();

    SyncHookFunction onTimeout();

}
