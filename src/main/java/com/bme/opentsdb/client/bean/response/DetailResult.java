package com.bme.opentsdb.client.bean.response;

import com.bme.opentsdb.client.bean.request.Point;
import lombok.Data;

import java.util.List;

/**
 * put操作响应的详细结果
 * 详见 <a>http://opentsdb.net/docs/build/html/api_http/put.html</a>
 *
 * @author yutyi
 * @date 2020/12/18
 */
@Data
public class DetailResult {

    private List<ErrorPoint> errors;

    private int failed;

    private int success;

    /**
     * put错误的Point
     */
    @Data
    public static class ErrorPoint{

        private Point datapoint;

        private String error;

    }

}
