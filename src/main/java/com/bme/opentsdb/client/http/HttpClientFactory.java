package com.bme.opentsdb.client.http;

import com.bme.opentsdb.client.tsdb.OpenTSDBConfig;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.concurrent.BasicThreadFactory;
import org.apache.http.HeaderElement;
import org.apache.http.HeaderElementIterator;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.ConnectionKeepAliveStrategy;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.apache.http.impl.nio.client.HttpAsyncClientBuilder;
import org.apache.http.impl.nio.client.HttpAsyncClients;
import org.apache.http.impl.nio.conn.PoolingNHttpClientConnectionManager;
import org.apache.http.impl.nio.reactor.DefaultConnectingIOReactor;
import org.apache.http.impl.nio.reactor.IOReactorConfig;
import org.apache.http.message.BasicHeaderElementIterator;
import org.apache.http.nio.conn.NoopIOSessionStrategy;
import org.apache.http.nio.conn.SchemeIOSessionStrategy;
import org.apache.http.nio.conn.ssl.SSLIOSessionStrategy;
import org.apache.http.nio.reactor.ConnectingIOReactor;
import org.apache.http.nio.reactor.IOReactorException;
import org.apache.http.protocol.HTTP;

import java.util.Objects;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * HttpClient工厂类
 *
 * @author yutyi
 * @date 2020/12/16
 */
@Slf4j
public class HttpClientFactory {

    private static final AtomicInteger NUM = new AtomicInteger();

    private static HttpClient httpClient;

    /**
     * 获取HttpClient
     * <p>
     * 连接池获取参考 {@link com.bme.opentsdb.client.http.AsyncClientConfiguration}
     *
     * @param config OpenTSDB连接配置
     * @return
     * @throws IOReactorException
     */
    public static HttpClient createHttpClient(OpenTSDBConfig config) throws IOReactorException {
        if (httpClient == null) {
            synchronized (HttpClientFactory.class) {
                //判断config是否为null
                Objects.requireNonNull(config);

                Registry<SchemeIOSessionStrategy> registry = RegistryBuilder.<SchemeIOSessionStrategy>create()
                        .register("http", NoopIOSessionStrategy.INSTANCE)
                        .register("https", SSLIOSessionStrategy.getDefaultStrategy())
                        .build();
                //初始化IO线程数
                ConnectingIOReactor ioReactor = initIOReactorConfig();
                //创建连接池
                PoolingNHttpClientConnectionManager manager = new PoolingNHttpClientConnectionManager(ioReactor, registry);

                //初始化请求配置
                RequestConfig requestConfig = initRequestConfig(config);
                //从连接池中获取CloseableHttpAsyncClient
                CloseableHttpAsyncClient httpAsyncClient = createPoolingHttpClient(requestConfig, manager, config);

                httpClient = new HttpClient(config, httpAsyncClient, initFixedCycleCloseConnection(manager));
                httpClient.start();
                return httpClient;
            }
        }
        return httpClient;
    }


    /***
     * 创建CPU核数的IO线程
     *
     * 参考{@link com.bme.opentsdb.client.http.AsyncClientConfiguration}
     * @return
     * @throws IOReactorException
     */
    private static ConnectingIOReactor initIOReactorConfig() throws IOReactorException {
        // Create I/O reactor configuration
        IOReactorConfig ioReactorConfig = IOReactorConfig.custom()
                //IO线程总数，默认为运行时可用线程数
                .setIoThreadCount(Runtime.getRuntime().availableProcessors())
                //连接超时
                .setConnectTimeout(30000)
                //读取数据超时
                .setSoTimeout(30000)
                .build();

        // Create a custom I/O reactort
        ConnectingIOReactor ioReactor = new DefaultConnectingIOReactor(ioReactorConfig);
        return ioReactor;
    }

    /***
     * 设置超时时间
     * @param config OpenTSDB配置参数
     * @return
     */
    private static RequestConfig initRequestConfig(OpenTSDBConfig config) {
        // Create global request configuration
        return RequestConfig.custom()
                // ConnectTimeout:连接超时.连接建立时间，三次握手完成时间.
                .setConnectTimeout(config.getHttpConnectTimeout() * 1000)
                // SocketTimeout:Socket请求超时.数据传输过程中数据包之间间隔的最大时间.
                .setSocketTimeout(config.getHttpConnectTimeout() * 1000)
                // ConnectionRequestTimeout:httpclient使用连接池来管理连接，这个时间就是从连接池获取连接的超时时间
                .setConnectionRequestTimeout(config.getHttpConnectTimeout() * 1000)
                .build();
    }

    /***
     * 创建CloseableHttpAsyncClient
     *
     * 参考{@link com.bme.opentsdb.client.http.AsyncClientConfiguration}
     * @param config 查询对象
     * @param manager 连接池管理
     * @param openTSDBConfig
     * @return
     */
    private static CloseableHttpAsyncClient createPoolingHttpClient(RequestConfig config,
                                                                    PoolingNHttpClientConnectionManager manager,
                                                                    OpenTSDBConfig openTSDBConfig) {
        //设置最大连接数和每个路由的默认最大连接数
        manager.setMaxTotal(100);
        manager.setDefaultMaxPerRoute(100);

        //创建HttpAsyncClientBuilder
        HttpAsyncClientBuilder httpAsyncClientBuilder = HttpAsyncClients.custom()
                .setConnectionManager(manager)
                .setDefaultRequestConfig(config);
        // 如果不是只读，则设置为长连接
        if (!openTSDBConfig.isReadonly()) {
            httpAsyncClientBuilder.setKeepAliveStrategy(myStrategy());
        }

        //从连接池创建CloseableHttpAsyncClient
        return httpAsyncClientBuilder.build();
    }

    /**
     * 长连接策略
     *
     * @return
     */
    private static ConnectionKeepAliveStrategy myStrategy() {
        return (response, context) -> {
            HeaderElementIterator it = new BasicHeaderElementIterator
                    (response.headerIterator(HTTP.CONN_KEEP_ALIVE));
            while (it.hasNext()) {
                HeaderElement he = it.nextElement();
                String param = he.getName();
                String value = he.getValue();
                if (value != null && param.equalsIgnoreCase
                        ("timeout")) {
                    return Long.parseLong(value) * 1000;
                }
            }
            //如果没有约定，则默认定义时长为60s
            return 60 * 1000;
        };
    }

    /**
     * 定时任务线程池定时回收连接
     * 参考 {@link com.bme.opentsdb.client.http.AsyncClientEvictExpiredConnections}
     *
     * @param manager
     * @return
     */
    private static ScheduledExecutorService initFixedCycleCloseConnection(final PoolingNHttpClientConnectionManager manager) {
        // 通过工厂方法创建线程
        ScheduledThreadPoolExecutor connectionGcService = new ScheduledThreadPoolExecutor(1,
                new BasicThreadFactory.Builder().namingPattern("Fixed-Cycle-Close-Connection-" + NUM.incrementAndGet()).daemon(true).build());

        /*ScheduledExecutorService connectionGcService = Executors.newSingleThreadScheduledExecutor(
                (r) -> {
                    Thread t = new Thread(r, "Fixed-Cycle-Close-Connection-" + NUM.incrementAndGet());
                    t.setDaemon(true);
                    return t;
                }
        );*/

        // 定时关闭所有空闲链接
        connectionGcService.scheduleAtFixedRate(() -> {
            try {
                log.debug("Close idle connections, fixed cycle operation");
                //关闭过期连接
                manager.closeExpiredConnections();
                // 关闭30秒内不活动的链接
                manager.closeIdleConnections(30, TimeUnit.SECONDS);
            } catch (Exception ex) {
                log.error("Close idle connections cause error，detail：{}", ex);
            }
        }, 30, 30, TimeUnit.SECONDS);
        return connectionGcService;

    }

}
