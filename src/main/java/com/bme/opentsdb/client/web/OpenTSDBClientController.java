package com.bme.opentsdb.client.web;

import com.bme.opentsdb.client.bean.request.Point;
import com.bme.opentsdb.client.bean.request.Query;
import com.bme.opentsdb.client.bean.request.SubQuery;
import com.bme.opentsdb.client.bean.response.QueryResult;
import com.bme.opentsdb.client.tsdb.OpenTSDBClient;
import com.bme.opentsdb.client.tsdb.OpenTSDBClientFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * OpenTSDB客户端web测试
 *
 * @author yutyi
 * @date 2020/12/21
 */
@RestController
@RequestMapping("/api")
public class OpenTSDBClientController {

    @Autowired
    OpenTSDBClientFactory openTSDBClientFactory;
    private OpenTSDBClient client;

    @GetMapping("/query")
    public List<QueryResult> query(String metric) throws IOException, InterruptedException, ExecutionException {
        if (client == null || !client.getHttpClient().getClient().isRunning()) {
            client = openTSDBClientFactory.build();
        }
        Query query = Query.begin("1d-ago")
                .sub(SubQuery.metric(metric)
                        .aggregator(SubQuery.Aggregator.NONE)
                        .build())
                .build();
        List<QueryResult> resultList = client.query(query);
//        client.gracefulClose();
        return resultList;
    }

    @GetMapping("/put")
    public void put(String metric, String tagk, String tagv) throws IOException {
        if (client == null || !client.getHttpClient().getClient().isRunning()) {
            client = openTSDBClientFactory.build();
        }
        Point point = Point.metric(metric)
                .tag(tagk, tagv)
                .value(System.currentTimeMillis(), 1.1)
                .build();
        //异步写入数据,将数据放入队列消费
        client.put(point);

        //请求执行完优雅关闭http客户端
//        client.gracefulClose();
    }
}
