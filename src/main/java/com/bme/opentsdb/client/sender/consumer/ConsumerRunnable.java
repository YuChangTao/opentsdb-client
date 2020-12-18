package com.bme.opentsdb.client.sender.consumer;

import com.bme.opentsdb.client.tsdb.OpenTSDBConfig;
import com.bme.opentsdb.client.bean.request.Api;
import com.bme.opentsdb.client.bean.request.Point;
import com.bme.opentsdb.client.common.Json;
import com.bme.opentsdb.client.http.HttpClient;
import com.bme.opentsdb.client.http.callback.BatchPutHttpResponseCallback;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * 消费者线程具体的消费逻辑
 *
 * @author yutyi
 * @date 2020/12/17
 */
@Slf4j
public class ConsumerRunnable implements Runnable {

    private final HttpClient httpClient;

    private final OpenTSDBConfig config;

    /**
     * 消费队列
     */
    private final BlockingQueue<Point> queue;

    private final CountDownLatch countDownLatch;

    private BatchPutHttpResponseCallback.BatchPutCallBack callBack;

    /**
     * 批处理大小
     */
    private int batchSize;

    /***
     * 每次提交等待的时间间隔，单位ms
     */
    private int batchPutTimeLimit;

    public ConsumerRunnable(HttpClient httpClient, OpenTSDBConfig config, BlockingQueue<Point> queue, CountDownLatch countDownLatch) {
        this.httpClient = httpClient;
        this.config = config;
        this.queue = queue;
        this.countDownLatch = countDownLatch;
        this.callBack = this.config.getBatchPutCallBack();
        this.batchSize = this.config.getBatchPutSize();
        this.batchPutTimeLimit = this.config.getBatchPutTimeLimit();
    }

    @Override
    public void run() {
        try {
            log.debug("thread:{} has started take point from queue", Thread.currentThread().getName());

            boolean readyClose = false;
            //从队列中获取数据等待最大时长
            int waitTimeLimit = batchPutTimeLimit / 3;

            Point waitPoint = null;

            while (!readyClose) {
                long t0 = System.currentTimeMillis();
                //批处理数据点集合
                List<Point> pointList = new ArrayList<>(batchSize);
                if (waitPoint != null) {
                    pointList.add(waitPoint);
                    waitPoint = null;
                }

                for (int i = pointList.size(); i < batchSize; i++) {
                    try {

                        Point point = queue.poll(waitTimeLimit, TimeUnit.MILLISECONDS);
                        if (point != null) {
                            pointList.add(point);
                        }

                        //避免无限循环poll
                        long t1 = System.currentTimeMillis();
                        if (t1 - t0 > batchPutTimeLimit) {
                            break;
                        }
                    } catch (InterruptedException e) {
                        //结束线程
                        readyClose = true;
                        log.info("The thread {} is interrupted", Thread.currentThread().getName());
                        break;
                    }
                }

                //当未获取到队列中数据时，阻塞直到获取到队列的头部元素
                if (pointList.size() == 0 && !readyClose) {
                    try {
                        waitPoint = queue.take();
                    } catch (InterruptedException e) {
                        //结束线程
                        readyClose = true;
                        log.info("The thread {} is interrupted", Thread.currentThread().getName());
                    }
                    continue;
                }

                if (pointList.size() == 0) {
                    continue;
                }
                sendHttp(pointList);
            }
        } finally {
            this.countDownLatch.countDown();
        }
    }

    /**
     * 发送Http请求写入数据
     *
     * @param points 数据点集合
     */
    private void sendHttp(List<Point> points) {
        try {
            if (callBack != null) {
                // 批处理写入数据发生部分错误时回调业务接口
                this.httpClient.post(
                        Api.PUT_DETAIL.getPath(),
                        Json.writeValueAsString(points),
                        new BatchPutHttpResponseCallback(callBack, points));

            } else {
                this.httpClient.post(
                        Api.PUT.getPath(),
                        Json.writeValueAsString(points),
                        new BatchPutHttpResponseCallback()
                );
            }
        } catch (JsonProcessingException e) {
            log.error("batch http request cause error,detail:{}", e);
        }

    }
}
