package com.bme.opentsdb.client.bean.request;

import lombok.Data;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * 一个或多个查询的列表，用于确定要为其获取最后一个数据点的时间序列
 * @author yutyi
 * @date 2020/12/18
 */
@Data
public class LastPointSubQuery {

    private String metric;

    private Map<String, String> tags;

    /**
     * Builder模式
     */
    public static class Builder {

        private String metric;

        private Map<String, String> tags = new HashMap<>();

        public LastPointSubQuery build() {
            LastPointSubQuery query = new LastPointSubQuery();
            query.metric = this.metric;
            query.tags = this.tags;
            return query;
        }

        public Builder(String metric) {
            this.metric = metric;
        }

        public Builder tag(String tagk, String tagv) {
            if (StringUtils.isNoneBlank(tagk) && StringUtils.isNoneBlank(tagv)) {
                this.tags.put(tagk, tagv);
            }
            return this;
        }

        public Builder tag(Map<String, String> tags) {
            if (!MapUtils.isEmpty(tags)) {
                this.tags.putAll(tags);
            }
            return this;
        }

    }

    public static Builder metric(String metric) {
        return new Builder(metric);
    }

}
