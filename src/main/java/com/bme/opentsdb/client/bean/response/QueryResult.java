package com.bme.opentsdb.client.bean.response;

import lombok.Data;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Query响应结果
 *
 * @author yutyi
 * @date 2020/12/18
 */
@Data
public class QueryResult {

    private String metric;

    private Map<String, String> tags;

    private List<String> aggregateTags;

    private LinkedHashMap<Long, Number> dps;

    //todo
}
