package com.bme.opentsdb.client.http.callback;

import com.bme.opentsdb.client.bean.request.Query;
import com.bme.opentsdb.client.bean.response.QueryResult;
import com.bme.opentsdb.client.common.Json;
import com.bme.opentsdb.client.common.util.ResponseUtil;
import com.bme.opentsdb.client.exception.HttpException;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpResponse;
import org.apache.http.concurrent.FutureCallback;

import java.io.IOException;
import java.util.List;

/**
 * 异步查询回调
 *
 * @author yutyi
 * @date 2020/12/17
 */
@Slf4j
public class QueryHttpResponseCallback implements FutureCallback<HttpResponse> {

    private final QueryCallback callback;

    private final Query query;

    public QueryHttpResponseCallback(QueryCallback callback, Query query) {
        this.callback = callback;
        this.query = query;
    }

    @Override
    public void completed(HttpResponse response) {
        try {
            List<QueryResult> results = Json.readValue(ResponseUtil.getContent(response), List.class, QueryResult.class);
            log.debug("请求成功");
            this.callback.response(query, results);
        } catch (IOException e) {
            e.printStackTrace();
            this.callback.failed(query, e);
        } catch (HttpException e) {
            log.error("请求失败，query:{},error:{}", query, e.getMessage());
            e.printStackTrace();
            this.callback.responseError(query, e);
        }
    }

    @Override
    public void failed(Exception e) {
        log.error("请求失败，query:{},error:{}", query, e.getMessage());
        this.callback.failed(query, e);
    }

    @Override
    public void cancelled() {

    }

    /***
     * 定义查询callback，需要用户自己实现逻辑
     */
    public interface QueryCallback {

        /***
         * 在请求完成并且response code成功时回调
         * @param query 查询对象
         * @param queryResults 查询结果
         */
        void response(Query query, List<QueryResult> queryResults);

        /***
         * 在response code失败时回调
         * @param query 查询对象
         * @param e 异常
         */
        void responseError(Query query, HttpException e);

        /***
         * 在发生错误是回调，如果http成功complete，但response code大于400，也会调用这个方法
         * @param query 查询对象
         * @param e 异常
         */
        void failed(Query query, Exception e);

    }

}
