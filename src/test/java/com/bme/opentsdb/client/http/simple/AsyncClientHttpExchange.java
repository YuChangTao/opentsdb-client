package com.bme.opentsdb.client.http.simple;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.apache.http.impl.nio.client.HttpAsyncClients;

import java.util.concurrent.Future;

/**
 * This example demonstrates a basic asynchronous HTTP request / response exchange. Response content is buffered in memory for simplicity.
 *
 * @Description 本示例演示了基本的异步HTTP请求/响应交换。为了简化，将响应内容缓冲在内存中。
 * @author yutyi
 * @date 2020/12/16
 */
public class AsyncClientHttpExchange {
    public static void main(final String[] args) throws Exception {
        CloseableHttpAsyncClient httpclient = HttpAsyncClients.createDefault();
        try {
            httpclient.start();
            HttpGet request = new HttpGet("http://httpbin.org/get");
            Future<HttpResponse> future = httpclient.execute(request, null);
            System.out.println(future.isDone());
            long l = System.currentTimeMillis();
            //阻塞一段时间
            HttpResponse response = future.get();
            System.out.println("等待时长："+(System.currentTimeMillis() - l));
            System.out.println("Response: " + response.getStatusLine());
            System.out.println("Shutting down");
        } finally {
            httpclient.close();
        }
        System.out.println("Done");
    }
}
