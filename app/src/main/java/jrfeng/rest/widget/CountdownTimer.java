package jrfeng.rest.widget;

public interface CountdownTimer {
    void startCountdown(int seconds, OnTimeoutListener listener);

    boolean isCountdownRunning();

    void cancelCountdown();

    /**
     * 倒计时超时事件监听器。到倒计时完成时，会回调该接口的 {@link OnTimeoutListener#timeout()} 方法。
     */
    interface OnTimeoutListener {
        /**
         * 倒计时完成事件的回调方法。
         */
        void timeout();
    }
}
