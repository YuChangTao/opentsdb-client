package com.bme.opentsdb.client.web;

import com.bme.opentsdb.client.bean.request.Point;
import com.bme.opentsdb.client.bean.request.Query;
import com.bme.opentsdb.client.bean.request.SubQuery;
import com.bme.opentsdb.client.bean.response.QueryResult;
import com.bme.opentsdb.client.common.Json;
import com.bme.opentsdb.client.common.util.ResponseUtil;
import com.bme.opentsdb.client.tsdb.OpenTSDBClient;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpResponse;
import org.apache.http.concurrent.FutureCallback;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * OpenTSDB客户端web测试
 *
 * @author yutyi
 * @date 2020/12/21
 */
@RestController
@RequestMapping("/api")
@Slf4j
public class OpenTSDBClientController {

    @Resource
    private OpenTSDBClient openTSDBClient;

    @GetMapping("/query")
    public List<QueryResult> query(String metric) throws IOException, InterruptedException, ExecutionException {
        Query query = Query.begin("1d-ago")
                .sub(SubQuery.metric(metric)
                        .aggregator(SubQuery.Aggregator.NONE)
                        .build())
                .build();
        return openTSDBClient.query(query);
    }

    @GetMapping("/put")
    public void put(String metric, String tagk, String tagv) throws IOException {
        Point point = Point.metric(metric)
                .tag(tagk, tagv)
                .value(System.currentTimeMillis(), 1.1)
                .build();
        //异步写入数据,将数据放入队列消费
        openTSDBClient.put(point);
    }

    @GetMapping("/test")
    public String test() throws IOException, InterruptedException, ExecutionException, TimeoutException {
        //异步写入数据,将数据放入队列消费
        Map<String, Object> map = new HashMap<>(1);
        map.put("customerId", 1);
        Future<HttpResponse> future = openTSDBClient.getHttpClient().post("/screen/intelligent_control/getVideoConf?customerId=1", Json.writeValueAsString(map), new FutureCallback<HttpResponse>() {
            @SneakyThrows
            @Override
            public void completed(HttpResponse result) {
                log.info("test result:{}", result);
            }

            @Override
            public void failed(Exception ex) {
                log.info("test ex:{}", ex.getMessage());
            }

            @Override
            public void cancelled() {
                log.info("test cancelled");
            }
        });
        return ResponseUtil.getContent(future.get(2000, TimeUnit.SECONDS));
    }
}
