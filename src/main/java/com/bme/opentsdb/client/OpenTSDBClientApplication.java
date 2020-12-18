package com.bme.opentsdb.client;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * OpenTSDB客户端启动类
 *
 * @author yutyi
 * @date 2020/12/18
 */
@SpringBootApplication
public class OpenTSDBClientApplication {

    public static void main(String[] args) {
        SpringApplication.run(OpenTSDBClientApplication.class, args);
    }
}
