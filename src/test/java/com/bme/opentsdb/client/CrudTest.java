package com.bme.opentsdb.client;

import com.bme.opentsdb.client.bean.request.*;
import com.bme.opentsdb.client.bean.response.DetailResult;
import com.bme.opentsdb.client.bean.response.LastPointQueryResult;
import com.bme.opentsdb.client.bean.response.QueryResult;
import com.bme.opentsdb.client.exception.HttpException;
import com.bme.opentsdb.client.http.callback.BatchPutHttpResponseCallback;
import com.bme.opentsdb.client.http.callback.QueryHttpResponseCallback;
import com.bme.opentsdb.client.tsdb.OpenTSDBClient;
import com.bme.opentsdb.client.tsdb.OpenTSDBClientFactory;
import com.bme.opentsdb.client.tsdb.OpenTSDBConfig;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.nio.reactor.IOReactorException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.concurrent.*;

/**
 * @Description:
 * @Author: jinyao
 * @CreateDate: 2019/2/22 下午3:53
 * @Version: 1.0
 */
@Slf4j
public class CrudTest {

    String host = "http://172.31.141.3";

    int port = 4399;

    OpenTSDBClient client;

    @Before
    public void config() throws IOReactorException {
        OpenTSDBConfig config = OpenTSDBConfig.address(host, port)
                                              .config();
        OpenTSDBConfig.address(host, port)
                      // http连接池大小，默认100
                      .httpConnectionPool(100)
                      // http请求超时时间，默认100s
                      .httpConnectTimeout(100)
                      // 异步写入数据时，每次http提交的数据条数，默认50
                      .batchPutSize(50)
                      // 异步写入数据中，内部有一个队列，默认队列大小20000
                      .batchPutBufferSize(20000)
                      // 异步写入等待时间，如果距离上一次请求超多300ms，且有数据，则直接提交
                      .batchPutTimeLimit(300)
                      // 当确认这个client只用于查询时设置，可不创建内部队列从而提高效率
                      .readonly()
                      // 每批数据提交完成后回调
                      .batchPutCallBack(new BatchPutHttpResponseCallback.BatchPutCallBack() {
                          @Override
                          public void response(List<Point> points, DetailResult result) {
                              // 在请求完成并且response code成功时回调
                          }

                          @Override
                          public void responseError(List<Point> points, DetailResult result) {
                              // 在response code失败时回调
                          }

                          @Override
                          public void failed(List<Point> points, Exception e) {
                              // 在发生错误是回调
                          }
                      })
                      .config();
//        client = OpenTSDBClientFactory.connect(config);
    }

    /***
     * 单点查询测试
     */
    @Test
    public void testQuery() throws InterruptedException, ExecutionException, IOException {
        log.info("当前线程:{}", Thread.currentThread()
                                   .getName());
        Query query = Query.begin("7d-ago")
                           .sub(SubQuery.metric("metric.test")
                                        .aggregator(SubQuery.Aggregator.NONE)
                                        .build())
                           .build();
        List<QueryResult> resultList = client.query(query);
        client.query(query, new QueryHttpResponseCallback.QueryCallback() {
            @Override
            public void response(Query query, List<QueryResult> queryResults) {
                // 在请求完成并且response code成功时回调
            }

            @Override
            public void responseError(Query query, HttpException e) {
                // 在response code失败时回调
            }

            @Override
            public void failed(Query query, Exception e) {
                // 在发生错误是回调
            }
        });
        log.info("result:{}", resultList);
    }

    /***
     * 并发查询测试
     * 测试结果大概190个线程会出现http超时
     * 之后会把http超时做成参数
     *
     * 更新：已解决这个问题，目前默认超时时间100秒，并可以通过参数改变
     */
    @Test
    public void testQueryConcurrent() {
        CountDownLatch latch = new CountDownLatch(1);
        int threadCount = 2000;
        long[] times = new long[3];
        // 利用CyclicBarrier模拟并发
        CyclicBarrier startBarrier = new CyclicBarrier(
                threadCount,
                () -> {
                    log.info("所有线程已经就位");
                    times[0] = System.currentTimeMillis();
                }
        );

        CyclicBarrier endBarrier = new CyclicBarrier(
                threadCount,
                () -> {
                    log.info("所有线程已经执行完毕");
                    times[1] = System.currentTimeMillis();
                    log.info("运行时间:{}毫秒", times[1] - times[0]);
                    latch.countDown();
                }
        );


        ExecutorService fixedThreadPool = Executors.newFixedThreadPool(
                threadCount,
                (runnable) -> {
                    Thread thread = new Thread(runnable, "thread-" + times[2]++);
                    return thread;
                }
        );
        while (threadCount -- > 0) {
            fixedThreadPool.execute(() -> {
                try {
                    startBarrier.await();
                    // 查询
                    testQuery();
                    endBarrier.await();
                } catch (Exception e) {
                    log.error("", e);
                    e.printStackTrace();
                }
            });
        }

        try {
            latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

    /**
     * 测试写入数据
     */
    @Test
    public void put() throws Exception {
        Point point = Point.metric("metric.test")
                           .tag("test", "hello")
                           .value(System.currentTimeMillis(), 1.0)
                           .build();
//        client.put(point);
        client.putSync(point);
        client.gracefulClose();
    }

    /***
     * 并发写入测试，运行前需要先清除metric为point的数据
     */
    @Test
    public void batchPut() throws Exception {
        /**
         * 删除数据
         *//*
        Query delete = Query.begin("30d-ago")
                            .delete()
                            .sub(SubQuery.metric("point")
                                         .aggregator(SubQuery.Aggregator.NONE)
                                         .build())
                            .build();
        client.query(delete);*/

        /***
         * 使用5个线程，每个线程都写入100000条数据
         */
        int threadCount = 5;
        int dataCount = 100000;
        CountDownLatch latch = new CountDownLatch(5);
        int[] ints = new int[1];
        /***
         * 方便测试，线程名称为1,2,3,4,5
         */
        ExecutorService threadPool = Executors.newFixedThreadPool(5, (r) ->
                new Thread(r, String.valueOf(++ints[0]))
        );
        long start = System.currentTimeMillis();

        /**
         * 获得前10天的时间戳，测试发现openTSDB不会写入大于当前时间戳的数据，所以时间戳要前移
         */
        long begin = start - (long) 24 * 60 * 60 * 1000 * 10;
        for (int a = 0; a < threadCount; a++) {
            threadPool.execute(() -> {
                for (int i = 1; i <= dataCount; i++) {
                    Point point = Point.metric("point")
                                       .tag("testTag", "test_" + Thread.currentThread()
                                                                       .getName())
                                       /**
                                        * 每秒一条数据
                                        */
                                       .value(begin + i * 1000, i)
                                       .build();
                    client.put(point);
                }
                latch.countDown();
            });
        }

        latch.await();
        long end = System.currentTimeMillis();
        log.debug("运行时间:{}毫秒", end - start);
        /***
         * 等待10秒，因为下面查询还需要用到client，所以这里先不关闭client，用沉睡线程的方式等待队列中所有任务完成
         */
        TimeUnit.SECONDS.sleep(10);

        /***
         * 断言依据是写入数据求和是否等于（1+100000）*100000/2
         * 用double类型，防止查询结果为科学计数法
         */
        double should = (double) (1 + dataCount) * dataCount / 2;
        Query query = Query.begin("20d-ago")
                           .sub(SubQuery.metric("point")
                                        /***
                                         * 不采用时间线聚合，结果应该会返回5条时间线
                                         */
                                        .aggregator(SubQuery.Aggregator.NONE)
                                        /***
                                         * 降采样，这里0all-sum表示把所有点合并成一个点
                                         */
                                        .downsample("0all-sum")
                                        .build())
                           .build();
        List<QueryResult> queryResults = client.query(query);
        for (QueryResult queryResult : queryResults) {
            LinkedHashMap<Long, Number> dps = queryResult.getDps();
            /**
             * 因为把所有点聚合在一个点上，所有dps只有一个值
             */
            dps.forEach((k, v) -> {
                /**
                 * 因结果数值较大，在本地测试时，已被转换成了科学计数法
                 */

                Assert.assertEquals(should, v);
            });
        }
        client.gracefulClose();
    }

    /***
     * 测试查询最新数据
     * @throws Exception
     */
    @Test
    public void testQueryLast() throws Exception {
        LastPointQuery query = LastPointQuery.sub(LastPointSubQuery.metric("metric.test")
                                                                   .tag("test", "hello")
                                                                   .build())
                                             // baskScan表示查询最多向前推进多少小时
                                             // 比如在5小时前写入过数据
                                             // 那么backScan(6)可以查出数据，但backScan(4)则不行
                                             .backScan(1000)
                                             .build();
        List<LastPointQueryResult> lastPointQueryResults = client.queryLast(query);
        log.info("查询最新数据:{}", lastPointQueryResults);
    }

    /***
     * 测试异步查询
     * @throws Exception
     */
    @Test
    public void testAsyncQuery() throws Exception {
        int[] ints = new int[1];
        QueryHttpResponseCallback.QueryCallback queryCallback = new QueryHttpResponseCallback.QueryCallback() {
            @Override
            public void response(Query query, List<QueryResult> queryResults) {
                log.debug("success,result:{}", queryResults);
            }

            @Override
            public void responseError(Query query, HttpException e) {
                log.debug("fail,error:{}", e.getMessage());
                e.printStackTrace();
                ints[0] = 1;
            }

            @Override
            public void failed(Query query, Exception e) {
                e.printStackTrace();
            }
        };
        Query query = Query.begin("20d-ago")
                           .sub(SubQuery.metric("point")
                                        .aggregator(SubQuery.Aggregator.NONE)
                                        /**
                                         * 特意写错，会触发callback中分failed方法
                                         */
                                        .downsample("0all-su")
                                        .build())
                           .build();
        client.query(query, queryCallback);
        client.gracefulClose();
        Assert.assertEquals(1, ints[0]);
    }

    /***
     * 测试写入回调
     * @throws Exception
     */
//    @Test
//    public void testPutCallback() throws Exception {
//        int[] ints = new int[2];
//        BatchPutHttpResponseCallback.BatchPutCallBack batchPutCallBack = new BatchPutHttpResponseCallback.BatchPutCallBack() {
//            @Override
//            public void response(List<Point> points, DetailResult result) {
//                log.debug("添加成功，detail:{}", result);
//                ints[0] = 1;
//            }
//
//            @Override
//            public void responseError(List<Point> points, DetailResult result) {
//                log.debug("添加失败，detail:{}", result);
//                ints[1] = 1;
//            }
//
//            @Override
//            public void failed(List<Point> points, Exception e) {
//
//            }
//        };
//        OpenTSDBConfig config = OpenTSDBConfig.address(host, port)
//                                              .batchPutCallBack(batchPutCallBack)
//                                              .config();
//        OpenTSDBClient openTSDBClient = OpenTSDBClientFactory.connect(config);
//        Point point = Point.metric("batchPutCallback")
//                           .tag("testTag", "test_1")
//                           .value(System.currentTimeMillis(), 1.0)
//                           .build();
//        openTSDBClient.put(point);
//        openTSDBClient.gracefulClose();
//        /**
//         * 测试response
//         */
//        Assert.assertEquals(1, ints[0]);
//        /***
//         * 测试error
//         */
//        //Assert.assertEquals(1, ints[1]);
//    }

}
