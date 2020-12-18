package com.bme.opentsdb.client.sender.producer;

import com.bme.opentsdb.client.bean.request.Point;

/**
 * 发布订阅者模式——发布者
 *
 * @author yutyi
 * @date 2020/12/17
 */
public interface Producer {

    /***
     * 写入队列
     * @param point 数据点
     */
    void send(Point point);

    /***
     * 关闭写入
     */
    void forbiddenSend();

}
