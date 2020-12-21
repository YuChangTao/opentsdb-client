package com.bme.opentsdb.client.bean.request;

/**
 * api地址
 * 详见<a>http://opentsdb.net/docs/build/html/api_http/index.html#api-endpoints</a>
 *  /s
 *  /api/aggregators
 *  /api/annotation
 *  /api/config
 *  /api/dropcaches
 *  /api/put
 *  /api/rollup
 *  /api/histogram
 *  /api/query
 *  /api/search
 *  /api/serializers
 *  /api/stats
 *  /api/suggest
 *  /api/tree
 *  /api/uid
 *  /api/version
 *
 * @Description
 * @author yutyi
 * @date 2020/12/18
 */
public enum Api {

    /***
     * 查询API端点
     */
    QUERY("/api/query"),
    LASTQUEERY("/api/query/last"),

    /**
     * 插入API端点
     */
    PUT("/api/put"),
    PUT_DETAIL("/api/put?details=true"),

    /**
     * 查询metric、tagk、tagv列表
     */
    SUGGEST("/api/suggest"),

    S("/api/s"),
    AGGREGATORS("/api/aggregators"),
    CONFIG("/api/config"),
    DROPCACHES("/api/dropcaches"),
    ROOLUP("/api/rollup"),
    HISTOGRAM("/api/histogram"),
    SEARCH("/api/search"),
    SERIALIZERS("/api/serializers"),
    STATS("/api/stats"),
    TREE("/api/tree"),
    UID("/api/uid"),
    VERSION("/api/version");

    private String path;

    Api(String path) {
        this.path = path;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }
}



