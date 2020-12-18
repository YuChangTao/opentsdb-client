package com.bme.opentsdb.client.bean.request;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 查询一批时间序列的最新的数据值
 * 详见 <a>http://opentsdb.net/docs/build/html/api_http/query/last.html</a>
 * @author yutyi
 * @date 2020/12/18
 */
@Data
public class LastPointQuery {

    /**
     * 时间序列集合
     */
    private List<LastPointSubQuery> queries;

    private boolean resolveNames;

    /**
     * 过去几个小时要搜索数据。如果设置为 0，则使用时间序列的元数据计数器的时间戳。
     */
    private int backScan;

    /**
     * Builder模式
     */
    public static class Builder {

        private List<LastPointSubQuery> queries = new ArrayList<>();

        private boolean resolveNames = true;

        private int backScan;

        public LastPointQuery build() {
            LastPointQuery query = new LastPointQuery();
            query.queries = this.queries;
            query.resolveNames = this.resolveNames;
            query.backScan = this.backScan;
            return query;
        }

        public Builder(LastPointSubQuery query) {
            this.queries.add(query);
        }

        public Builder(List<LastPointSubQuery> queries) {
            this.queries = queries;
        }

        public Builder resolveNames(boolean resolveNames) {
            this.resolveNames = resolveNames;
            return this;
        }

        public Builder backScan(int backScan) {
            this.backScan = backScan;
            return this;
        }

    }

    public static Builder sub(LastPointSubQuery query) {
        return new Builder(query);
    }

    public static Builder sub(List<LastPointSubQuery> queries) {
        return new Builder(queries);
    }

}
