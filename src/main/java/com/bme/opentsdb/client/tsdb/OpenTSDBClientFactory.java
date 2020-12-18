package com.bme.opentsdb.client.tsdb;

import com.bme.opentsdb.client.bean.request.Point;
import com.bme.opentsdb.client.bean.response.DetailResult;
import com.bme.opentsdb.client.http.callback.BatchPutHttpResponseCallback;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.nio.reactor.IOReactorException;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;

/**
 * OpenTSDB客户端工厂类
 *
 * @author yutyi
 * @date 2020/12/17
 */
@Data
@ConfigurationProperties("opentsdb")
@Component
public class OpenTSDBClientFactory {

    private String host;
    private Integer port;

    public OpenTSDBClient build(OpenTSDBConfig config) throws IOReactorException {
        if (StringUtils.isEmpty(config.getHost())) {
            Objects.requireNonNull(host,"host");
            Objects.requireNonNull(port,"port");
            config.setHost(host);
            config.setPort(port);
        }
        return new OpenTSDBClient(config);
    }


}
