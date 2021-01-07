package com.bme.opentsdb.client.tsdb;

import lombok.Data;
import org.apache.http.nio.reactor.IOReactorException;

import java.util.Objects;

/**
 * OpenTSDB客户端工厂类
 *
 * @author yutyi
 * @date 2020/12/17
 */
@Data
public class OpenTSDBClientFactory {

    public static OpenTSDBClient build(OpenTSDBConfig config) throws IOReactorException {
        Objects.requireNonNull(config, "config");
        return new OpenTSDBClient(config);
    }

}
