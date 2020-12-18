package com.bme.opentsdb.client.bean.response;

import lombok.Data;

import java.util.Map;

/**
 * 查询单个时间序列最新值的响应结果
 * <p>
 * 详见 <a>http://opentsdb.net/docs/build/html/api_http/query/last.html</a>
 *
 * @author yutyi
 * @date 2020/12/18
 */
@Data
public class LastPointQueryResult {

    private String metric;

    private Map<String, String> tags;

    private long timestamp;

    private Object value;

    private String tsuid;

}
