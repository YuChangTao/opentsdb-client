package com.bme.opentsdb.client.sender.consumer;

import com.bme.opentsdb.client.tsdb.OpenTSDBConfig;
import com.bme.opentsdb.client.bean.request.Point;
import com.bme.opentsdb.client.http.HttpClient;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.*;

/**
 * 消费者
 *
 * @author yutyi
 * @date 2020/12/17
 */
@Slf4j
public class ConsumerImpl implements Consumer {

    private final HttpClient httpClient;
    /**
     * 消息队列
     */
    private final BlockingQueue<Point> queue;

    /**
     * 消费者线程池
     */
    private final ThreadPoolExecutor threadPool;

    /**
     * 开启消费线程数
     */
    private final int threadCount;

    private final OpenTSDBConfig config;

    private final CountDownLatch countDownLatch;


    public ConsumerImpl(BlockingQueue<Point> queue, HttpClient httpClient, OpenTSDBConfig config) {
        this.queue = queue;
        this.httpClient = httpClient;
        this.config = config;
        this.threadCount = config.getPutConsumerThreadCount();

        //初始化线程池,线程池大小固定
        final int[] i = new int[1];
        LinkedBlockingQueue<Runnable> threadQueue = new LinkedBlockingQueue<>(threadCount);
        this.threadPool = new ThreadPoolExecutor(threadCount, threadCount, 60, TimeUnit.SECONDS, threadQueue,
                runnable -> new Thread(runnable, "batch-put-thread-" + ++i[0]));
        this.countDownLatch = new CountDownLatch(threadCount);

        log.debug("the consumer has started");
    }

    @Override
    public void start() {
        //启动消费线程
        for (int i = 0; i < threadCount; i++) {
            threadPool.execute(new ConsumerRunnable(httpClient, config, queue, countDownLatch));
        }
    }

    @Override
    public void gracefulStop() {
        this.stop(false);
    }

    @Override
    public void forceStop() {
        this.stop(true);
    }

    /***
     * 关闭线程池
     * @param force 是否强制关闭
     */
    private void stop(boolean force) {
        if (threadPool != null) {
            if (force) {
                // 强制退出不等待，截断消费者线程。
                threadPool.shutdownNow();
            } else {
                // 截断消费者线程
                while (!threadPool.isShutdown() || !threadPool.isTerminated()) {
                    threadPool.shutdownNow();
                }

                // 等待所有消费者线程结束
                try {
                    countDownLatch.await();
                } catch (InterruptedException e) {
                    log.error("An error occurred waiting for the consumer thread to close", e);
                }
            }
        }
    }
}
