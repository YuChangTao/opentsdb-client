package com.bme.opentsdb.client.exception;

import com.bme.opentsdb.client.bean.response.ErrorResponse;
import lombok.Data;

/**
 * @Description:
 * @Author: jinyao
 * @CreateDate: 2019/2/22 下午7:40
 * @Version: 1.0
 */
@Data
public class HttpException extends RuntimeException {

    private ErrorResponse errorResponse;

    public HttpException(ErrorResponse errorResponse) {
        super(errorResponse.toString());
        this.errorResponse = errorResponse;
    }

}
