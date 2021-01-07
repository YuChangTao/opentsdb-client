package com.bme.opentsdb.client.exception;

import com.bme.opentsdb.client.bean.response.ErrorResponse;
import lombok.Data;

/**
 * Http异常
 *
 * @author yutyi
 * @date 2020/12/16
 */
@Data
public class HttpException extends RuntimeException {

    private ErrorResponse errorResponse;

    public HttpException(ErrorResponse errorResponse) {
        super(errorResponse.toString());
        this.errorResponse = errorResponse;
    }

}
