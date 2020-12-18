package com.bme.opentsdb.client.sender.consumer;

/**
 * 发布订阅者模式——订阅者
 *
 * @author yutyi
 * @date 2020/12/17
 */
public interface Consumer {


    /***
     * 开始消费，启动线程池中的消费线程
     */
    void start();

    /***
     * 停止消费，会等待线程池中的任务完成
     */
    void gracefulStop();

    /***
     * 强制停止
     */
    void forceStop();

}
