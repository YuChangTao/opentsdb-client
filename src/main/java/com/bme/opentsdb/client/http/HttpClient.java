package com.bme.opentsdb.client.http;

import com.bme.opentsdb.client.tsdb.OpenTSDBConfig;
import com.bme.opentsdb.client.http.callback.GracefulCloseFutureCallBack;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.concurrent.FutureCallback;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Http客户端
 *
 * @author yutyi
 * @date 2020/12/16
 */
@Slf4j
@Data
public class HttpClient {

    private String host;
    private int port;

    /**
     * Http异步请求客户端
     */
    private final CloseableHttpAsyncClient client;

    /**
     * 空闲连接清理服务
     */
    private ScheduledExecutorService connectionGcService;

    /**
     * 未完成任务数 for graceful close.默认0
     */
    private final AtomicInteger unCompletedTaskNum;

    /**
     * @param config 连接配置
     * @param client 异步HTTP客户端
     * @param connectionGcService 连接GC服务
     */
    public HttpClient(OpenTSDBConfig config, CloseableHttpAsyncClient client, ScheduledExecutorService connectionGcService) {
        this.host = config.getHost();
        this.port = config.getPort();
        this.client = client;
        this.connectionGcService = connectionGcService;
        this.unCompletedTaskNum = new AtomicInteger(0);
    }

    /**
     * post请求
     * @param path 请求路径
     * @param json json格式参数
     * @return
     */
    public Future<HttpResponse> post(String path,String json) {
        return this.post(path,json,null);
    }

    /***
     * post请求
     * @param path 请求路径
     * @param json json格式参数
     * @param httpCallback 异步请求回调，如果不为null则需要等待结果并且GracefulClose
     * @return
     */
    public Future<HttpResponse> post(String path, String json, FutureCallback<HttpResponse> httpCallback) {
        log.debug("发送post请求，路径:{}，请求内容:{}", path, json);

        //构建HttpPost
        HttpPost httpPost = new HttpPost(getUrl(path));
        if (StringUtils.isNoneBlank(json)) {
            httpPost.addHeader("Content-Type", "application/json");
            httpPost.setEntity(generateStringEntity(json));
        }

        FutureCallback<HttpResponse> responseCallback = null;
        if (httpCallback != null) {
            log.debug("等待完成的任务数:{}", unCompletedTaskNum.incrementAndGet());
            responseCallback = new GracefulCloseFutureCallBack(unCompletedTaskNum, httpCallback);
        }
        return client.execute(httpPost, responseCallback);
    }

    /**
     * 获取请求url
     * @param path
     * @return
     */
    private String getUrl(String path) {
        return host + ":" + port + path;
    }

    /**
     * 构建请求体
     * @param json 请求参数json字符串
     * @return
     */
    private StringEntity generateStringEntity(String json) {
        StringEntity stringEntity = new StringEntity(json, Charset.forName("UTF-8"));
        return stringEntity;
    }


    /**
     * 开启请求线程
     */
    public void start() {
        this.client.start();
    }

    /**
     * 优雅关闭
     * @throws IOException
     */
    public void gracefulClose() throws IOException {
        this.close(false);
    }

    /**
     * 强制关闭
     * @throws IOException
     */
    public void forceClose() throws IOException {
        this.close(true);
    }

    private void close(boolean force) throws IOException {
        //等待关闭
        if (!force) {
            while (this.client.isRunning()) {
                //优雅关闭
                int remainingTasks = this.unCompletedTaskNum.get();
                if (remainingTasks == 0) {
                    break;
                } else {
                    try {
                        // 轮询检查优雅关闭
                        Thread.sleep(50);
                    } catch (InterruptedException e) {
                        log.warn("The thread {} is Interrupted", Thread.currentThread()
                                .getName());
                    }
                }

            }
        }
        //关闭GC服务
        connectionGcService.shutdownNow();
        //关闭客户端
        this.client.close();
    }
}
