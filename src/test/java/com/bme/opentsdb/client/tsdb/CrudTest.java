package com.bme.opentsdb.client.tsdb;

import com.bme.opentsdb.client.ApplicationTests;
import com.bme.opentsdb.client.bean.request.Point;
import com.bme.opentsdb.client.bean.request.Query;
import com.bme.opentsdb.client.bean.request.SubQuery;
import com.bme.opentsdb.client.bean.response.DetailResult;
import com.bme.opentsdb.client.bean.response.QueryResult;
import com.bme.opentsdb.client.exception.HttpException;
import com.bme.opentsdb.client.http.callback.BatchPutHttpResponseCallback;
import com.bme.opentsdb.client.http.callback.QueryHttpResponseCallback;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.nio.reactor.IOReactorException;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * OpenTSDB 测试案例
 *
 * @author yutyi
 * @date 2020/12/18
 */
@Slf4j
//@RunWith(SpringRunner.class)
//@SpringBootTest
public class CrudTest extends ApplicationTests {

    OpenTSDBClient client;

    @Autowired
    OpenTSDBClientFactory openTSDBClientFactory;

    /**
     * 初始化连接
     *
     * @throws IOReactorException
     */
    @Before
    public void config() throws IOReactorException {

        OpenTSDBConfig.address()
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
        OpenTSDBConfig config = OpenTSDBConfig.address().config();
        client = openTSDBClientFactory.build(config);
    }

    /**
     * 单点同步查询
     */
    @Test
    public void singleSyncQuery() throws InterruptedException, ExecutionException, IOException {
        log.info("当前线程:{}", Thread.currentThread().getName());
        //查询参数
        Query query = Query.begin("1d-ago")
                .sub(SubQuery.metric("test")
                        .aggregator(SubQuery.Aggregator.NONE)
                        .build())
                .build();
        //同步查询返回结果
        List<QueryResult> resultList = client.query(query);
        log.info("result:{}", resultList);
    }

    /**
     * 单点异步查询
     */
    @Test
    public void singleAsyncQuery() throws IOException {
        log.info("当前线程:{}", Thread.currentThread().getName());
        //查询参数
        Query query = Query.begin("1d-ago")
                .sub(SubQuery.metric("test")
                        .aggregator(SubQuery.Aggregator.NONE)
                        .build())
                .build();
        //相同代码可封装成一个QueryCallback实现类
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
    }


    /**
     * 异步写入数据
     * @throws IOException
     */
    public void asyncPut() throws IOException {
        Point point = Point.metric("test")
                           .tag("test", "hello")
                           .value(System.currentTimeMillis(), 1.1)
                           .build();
        //异步写入数据,将数据放入队列消费
        client.put(point);

        //请求执行完优雅关闭http客户端
        client.gracefulClose();
    }

    /**
     * 同步写入数据
     * @throws IOException
     */
    public void syncPut() throws IOException, ExecutionException, InterruptedException {
        Point point = Point.metric("test")
                .tag("test", "hello")
                .value(System.currentTimeMillis(), 1.1)
                .build();
        //同步写入数据
        client.putSync(point);

        //请求执行完优雅关闭http客户端
        client.gracefulClose();
    }







}
