asyncclient: <a>http://hc.apache.org/httpcomponents-asyncclient-4.1.x/</a>

opentsdb: <a>http://opentsdb.net/docs/build/html/api_http/index.html</a>

使用默认OpenTSDB客户端（推荐 ）
```java
@Resource
private OpenTSDBClient client;
```




自定义OpenTSDB客户端
```java
public class CrudTest {

    OpenTSDBClient client;

    @Autowired
    OpenTSDBClientFactory openTSDBClientFactory;

    public void config() throws IOReactorException {

        //自定义连接配置
        OpenTSDBConfig config = OpenTSDBConfig.address()
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
        client = openTSDBClientFactory.build(config);
    }
}

```



第三方引入
1. 使用maven install将项目安装到本地仓库，引入依赖
```xml
        <dependency>
            <groupId>com.bme.opentsdb.client</groupId>
            <artifactId>opentsdb-client</artifactId>
            <version>1.0</version>
        </dependency>
```
2. springboot启动类添加`@EnableOpenTSDBClient`注解即可，客户端可直接注入获取
```java
@Resource
private OpenTSDBClient client;
```

