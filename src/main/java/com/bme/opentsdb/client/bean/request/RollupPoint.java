package com.bme.opentsdb.client.bean.request;

import lombok.Data;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Data
public class RollupPoint {
    private String metric;

    private Map<String, String> tags = new HashMap<>();

    private Number value;

    private long timestamp;

    private String interval;

    private String aggregator;

    private String groupByAggregator;

    public static RollupPoint.MetricBuilder metric(String metric) {
        return new RollupPoint.MetricBuilder(metric);
    }


    /**
     * Builder模式
     */
    public static class MetricBuilder {

        private String metric;

        private Map<String, String> tags = new HashMap<>();

        private Number value;

        private long timestamp;

        private String interval;

        private String aggregator;

        private String groupByAggregator;

        public MetricBuilder(String metric) {
            if (StringUtils.isBlank(metric)) {
                throw new IllegalArgumentException("The metric can't be empty");
            }
            this.metric = metric;
        }

        public RollupPoint.MetricBuilder value(long timestamp, Number value) {
            if (timestamp == 0) {
                throw new IllegalArgumentException("timestamp must gt 0");
            }
            Objects.requireNonNull(value);
            this.timestamp = timestamp;
            this.value = value;
            return this;
        }

        public RollupPoint.MetricBuilder tag(final String tagName, final String value) {
            if (StringUtils.isNoneBlank(tagName) && StringUtils.isNoneBlank(value)) {
                tags.put(tagName, value);
            }
            return this;
        }

        public RollupPoint.MetricBuilder tag(final Map<String, String> tags) {
            if (!MapUtils.isEmpty(tags)) {
                this.tags.putAll(tags);
            }
            return this;
        }

        public RollupPoint.MetricBuilder interval(final String interval) {
            if (StringUtils.isNoneBlank(interval)) {
                this.interval = interval;
            }
            return this;
        }

        public RollupPoint.MetricBuilder aggregator(final String aggregator) {
            if (StringUtils.isNoneBlank(aggregator)) {
                this.aggregator = aggregator;
            }
            return this;
        }

        public RollupPoint.MetricBuilder groupByAggregator(final String groupByAggregator) {
            if (StringUtils.isNoneBlank(groupByAggregator)) {
                this.groupByAggregator = groupByAggregator;
            }
            return this;
        }

        public RollupPoint build() {
            RollupPoint point = new RollupPoint();
            point.metric = this.metric;

            if (MapUtils.isEmpty(tags)) {
                throw new IllegalArgumentException("tags can't be empty");
            }

            if ((StringUtils.isNotEmpty(interval) && StringUtils.isNotEmpty(aggregator)) && StringUtils.isEmpty(groupByAggregator)) {
                point.interval = this.interval;
                point.aggregator = this.aggregator;
            } else if ((StringUtils.isEmpty(interval) || StringUtils.isEmpty(aggregator)) && StringUtils.isNotEmpty(groupByAggregator)) {
                point.groupByAggregator = this.groupByAggregator;
            } else if (StringUtils.isNotEmpty(interval) && StringUtils.isNotEmpty(aggregator) && StringUtils.isNotEmpty(groupByAggregator)) {
                point.interval = this.interval;
                point.aggregator = this.aggregator;
                point.groupByAggregator = this.groupByAggregator;
            } else {
                throw new IllegalArgumentException("interval and aggregator can't be empty or groupByAggregator can't be empty");
            }
            point.tags = this.tags;

            point.timestamp = this.timestamp;
            point.value = this.value;
            return point;
        }

    }
}
