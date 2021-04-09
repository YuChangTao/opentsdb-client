package com.bme.opentsdb.client.config;

import com.bme.opentsdb.client.tsdb.OpenTSDBClient;
import com.bme.opentsdb.client.tsdb.OpenTSDBClientFactory;
import com.bme.opentsdb.client.tsdb.OpenTSDBConfig;
import org.apache.http.nio.reactor.IOReactorException;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.util.Assert;

import java.util.Objects;

/**
 * OpenTSDB客户端注册
 *
 * @author yut
 * @date 2021/1/7 17:13
 */
@Configuration
@Import(OpenTSDBConfig.class)
@ConditionalOnBean(OpenTSDBClientMarkerConfiguration.Marker.class)
public class OpenTSDBAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(name = {"openTSDBClient"})
    public OpenTSDBClient openTSDBClient(OpenTSDBConfig config) throws IOReactorException {
        Objects.requireNonNull(config.getHost(), "host required");
        Assert.isTrue(config.getPort() > 0, "port must be greater than 0");
        return OpenTSDBClientFactory.build(config);
    }

}
