package com.bme.opentsdb.client.config;

import com.bme.opentsdb.client.tsdb.OpenTSDBClient;
import com.bme.opentsdb.client.tsdb.OpenTSDBClientFactory;
import com.bme.opentsdb.client.tsdb.OpenTSDBConfig;
import lombok.Data;
import org.apache.http.nio.reactor.IOReactorException;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Objects;

/**
 * OpenTSDB客户端注册
 *
 * @author yut
 * @date 2021/1/7 17:13
 */
@Configuration
@Data
@ConfigurationProperties("opentsdb.config")
public class OpenTSDBAutoConfiguration {

    private String host;
    private Integer port;

    @Bean
    @ConditionalOnMissingBean(name = {"openTSDBClient"})
    public OpenTSDBClient openTSDBClient() throws IOReactorException {
        Objects.requireNonNull(host, "host");
        Objects.requireNonNull(port, "port");
        OpenTSDBConfig config = OpenTSDBConfig.address(host, port).config();
        return OpenTSDBClientFactory.build(config);
    }

}
