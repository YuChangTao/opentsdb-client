package com.bme.opentsdb.client.http.ssl;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.apache.http.impl.nio.client.HttpAsyncClients;
import org.apache.http.nio.conn.ssl.SSLIOSessionStrategy;
import org.apache.http.ssl.SSLContexts;

import javax.net.ssl.SSLContext;
import java.util.concurrent.Future;

/**
 * This example demonstrates how to create secure connections with a custom SSL
 * context.
 *
 * @@Description 此示例演示如何使用自定义SSL上下文创建安全连接。
 * @author yutyi
 */
public class AsyncClientCustomSSL {

    public final static void main(String[] args) throws Exception {
        // Trust standard CAs and all self-signed certs
        SSLContext sslcontext = SSLContexts.custom()
                .loadTrustMaterial(new TrustSelfSignedStrategy())
                .build();
        // Allow TLSv1 protocol only
        SSLIOSessionStrategy sslSessionStrategy = new SSLIOSessionStrategy(
                sslcontext,
                new String[]{"TLSv1"},
                null,
                SSLIOSessionStrategy.getDefaultHostnameVerifier());
        CloseableHttpAsyncClient httpclient = HttpAsyncClients.custom()
                .setSSLStrategy(sslSessionStrategy)
                .build();
        try {
            httpclient.start();
            HttpGet httpget = new HttpGet("https://httpbin.org/");
            Future<HttpResponse> future = httpclient.execute(httpget, null);
            HttpResponse response = future.get();
            System.out.println("Response: " + response.getStatusLine());
            System.out.println("Shutting down");
        } finally {
            httpclient.close();
        }
        System.out.println("Done");
    }

}
