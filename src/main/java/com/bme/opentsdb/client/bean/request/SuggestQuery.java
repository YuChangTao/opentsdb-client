package com.bme.opentsdb.client.bean.request;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

import java.util.Objects;

/**
 * 查询元素据metrics、tagk、tagv
 *
 * 详见<a>https://www.docs4dev.com/docs/zh/opentsdb/2.3/reference/api_http-suggest.html</a>
 *
 * @author yutyi
 * @author yutyi
 * @date 2020/12/18
 */
@Data
public class SuggestQuery {

    /**
     * 自动完成的数据类型。必须是以下之一： metrics， tagk 或 tagv
     */
    private Type type;

    /**
     * 匹配给定类型的字符串
     */
    private String q;

    /**
     * 建议返回的最大结果数。必须大于 0
     */
    private Integer max;

    public static enum Type {
        /***
         * 所查询的元数据类型
         */
        METRICS("metrics"),
        TAG_KEY("tagk"),
        TAG_VALUE("tagv");

        private String value;

        Type(String value) {
            this.value = value;
        }

        @JsonValue
        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }
    }

    public static Builder type(Type type) {
        return new Builder(type);
    }

    public static class Builder {

        private Type type;

        private String q;

        private Integer max;

        public Builder(Type type) {
            Objects.requireNonNull(type);
            this.type = type;
        }

        public SuggestQuery build() {
            SuggestQuery suggestQuery = new SuggestQuery();
            suggestQuery.type = this.type;

            if (StringUtils.isNoneBlank(q)) {
                suggestQuery.q = this.q;
            }
            if (max != null) {
                suggestQuery.max = this.max;
            }
            return suggestQuery;
        }

        public Builder q(String q) {
            this.q = q;
            return this;
        }

        public Builder max(Integer max) {
            this.max = max;
            return this;
        }

    }

}
