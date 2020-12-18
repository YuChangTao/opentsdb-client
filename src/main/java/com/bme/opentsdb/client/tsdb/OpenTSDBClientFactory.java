package com.bme.opentsdb.client.tsdb;

import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.nio.reactor.IOReactorException;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

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

    public OpenTSDBClient build() throws IOReactorException {
        return this.build(null);
    }

    public OpenTSDBClient build(OpenTSDBConfig config) throws IOReactorException {
        if (config == null) {
            config = OpenTSDBConfig.address().config();
        }
        if (StringUtils.isEmpty(config.getHost())) {
            Objects.requireNonNull(host, "host");
            Objects.requireNonNull(port, "port");
            config.setHost(host);
            config.setPort(port);
        }
        return new OpenTSDBClient(config);
    }


}
