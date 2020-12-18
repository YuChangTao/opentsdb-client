package com.bme.opentsdb.client.bean.response;

import lombok.Data;

import java.text.MessageFormat;

/**
 * 响应错误信息
 * 详见 <a>http://opentsdb.net/docs/build/html/api_http/index.html#response-codes</a>
 *
 * @author yutyi
 * @date 2020/12/18
 */
@Data
public class ErrorResponse {

    private Error error;

    @Override
    public String toString() {
        return MessageFormat.format(
                "调用OpenTSDB http api发生错误，响应码:{0},错误信息:{1}",
                error.getCode(),
                error.getMessage()
        );
    }

    @Data
    public static class Error {

        private int code;

        private String message;

        //todo
    }

}
