package com.bme.opentsdb.client.tsdb;

import com.bme.opentsdb.client.bean.request.SuggestQuery;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * @author yutyi
 * @date 2020/12/21
 */
@Slf4j
public class ApiTest extends CrudTest {


    /**
     * 查询已存在的metric
     *
     * @throws InterruptedException
     * @throws ExecutionException
     * @throws IOException
     */
    @Test
    public void querySuggest() throws InterruptedException, ExecutionException, IOException {
        SuggestQuery suggestQuery = SuggestQuery.type(SuggestQuery.Type.METRICS).build();
        List<String> list = client.querySuggest(suggestQuery);
        log.info("suggest query result:{}", list);
    }

    /**
     * 列出时间序列查询中使用的已实现聚合函数的名称
     */
    @Test
    public void aggregators() throws InterruptedException, ExecutionException, IOException {
        List<String> list = client.queryAggregators();
        log.info("aggregators query result:{}", list);
    }


}
