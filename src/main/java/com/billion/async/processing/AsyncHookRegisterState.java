package com.billion.async.processing;

/**
 * @author asheng
 * @since 2018/12/27
 */
public enum AsyncHookRegisterState {

    /** 创建状态 */
    CREATED {
        @Override
        public boolean isInit() {
            return true;
        }
    },

    /** 完成状态 */
    COMPLETED {
        @Override
        public boolean isInit() {
            return false;
        }
    },

    /** 超时取消状态 */
    CANCELED {
        @Override
        public boolean isInit() {
            return false;
        }
    };

    /**
     * 是否是初始化状态
     *
     * @return true：初始化状态
     */
    public abstract boolean isInit();

}
