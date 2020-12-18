package com.bme.opentsdb.client.http.pipelined;

import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.nio.client.CloseableHttpPipeliningClient;
import org.apache.http.impl.nio.client.HttpAsyncClients;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Future;

/**
 * This example demonstrates a pipelinfed execution of multiple HTTP request / response exchanges
 * Response content is buffered in memory for simplicity.
 *
 * @Description 此示例演示了多个HTTP请求/响应交换的管道执行,为了简化将响应内容缓冲在内存中
 * @author yutyi
 *
 */
public class AsyncClientPipelined {

    public static void main(final String[] args) throws Exception {
        CloseableHttpPipeliningClient httpclient = HttpAsyncClients.createPipelining();
        try {
            httpclient.start();

            HttpHost targetHost = new HttpHost("httpbin.org", 80);
            HttpGet[] resquests = {
                    new HttpGet("/"),
                    new HttpGet("/ip"),
                    new HttpGet("/headers"),
                    new HttpGet("/get")
            };

            Future<List<HttpResponse>> future = httpclient.execute(targetHost,
                    Arrays.<HttpRequest>asList(resquests), null);
            List<HttpResponse> responses = future.get();
            for (HttpResponse response : responses) {
                System.out.println(response.getStatusLine());
            }
            System.out.println("Shutting down");
        } finally {
            httpclient.close();
        }
        System.out.println("Done");
    }

}
