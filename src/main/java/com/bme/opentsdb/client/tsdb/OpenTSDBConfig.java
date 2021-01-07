package com.bme.opentsdb.client.tsdb;

import com.bme.opentsdb.client.http.callback.BatchPutHttpResponseCallback;
import lombok.Data;

/**
 * OpenTSDB连接配置
 *
 * @author yutyi
 * @date 2020/12/16
 */
@Data
public class OpenTSDBConfig {

    private String host;

    private int port;

    private int httpConnectionPool;

    private int httpConnectTimeout;

    private int putConsumerThreadCount;

    private int batchPutSize;

    private int batchPutBufferSize;

    private int batchPutTimeLimit;

    private boolean readonly;

    private BatchPutHttpResponseCallback.BatchPutCallBack batchPutCallBack;

    public static class Builder {

        private String host;

        private int port;

        /**
         * 每个Host分配的连接数
         */
        private int httpConnectionPool = 100;

        /**
         * http连接超时时间，单位：秒
         */
        private int httpConnectTimeout = 100;

        /***
         * 生产着消费者模式中，消费者线程
         */
        private int putConsumerThreadCount = 2;

        /***
         * 生产着消费者模式中，缓冲池的大小（消息队列大小）
         */
        private int batchPutBufferSize = 20000;

        /**
         * 每个http请求提交的数据大小（批处理大小）
         */
        private int batchPutSize = 50;

        /***
         * 每次提交等待的最大时间限制，单位ms
         */
        private int batchPutTimeLimit = 300;

        /***
         * 如果确定不写入数据，可以把这个属性设置为true，将不会开启写数据用的队列和线程池
         */
        private boolean readonly = false;

        /***
         * 对这个client实例的批量写入设置一个回调接口
         */
        private BatchPutHttpResponseCallback.BatchPutCallBack batchPutCallBack;

        public Builder(String host, int port) {
            this.host = host;
            this.port = port;
        }

        public OpenTSDBConfig config() {
            OpenTSDBConfig config = new OpenTSDBConfig();

            config.host = this.host;
            config.port = this.port;
            config.httpConnectTimeout = this.httpConnectTimeout;
            config.httpConnectionPool = this.httpConnectionPool;
            config.putConsumerThreadCount = this.putConsumerThreadCount;
            config.batchPutSize = this.batchPutSize;
            config.batchPutBufferSize = this.batchPutBufferSize;
            config.batchPutTimeLimit = this.batchPutTimeLimit;
            config.readonly = this.readonly;
            config.batchPutCallBack = this.batchPutCallBack;

            return config;
        }

        public Builder httpConnectionPool(int connectionPool) {
            if (connectionPool < 1) {
                throw new IllegalArgumentException("The ConnectionPool can't be less then 1");
            }
            httpConnectionPool = connectionPool;
            return this;
        }

        public Builder httpConnectTimeout(int httpConnectTimeout) {
            if (httpConnectTimeout <= 0) {
                throw new IllegalArgumentException("The connectTimtout can't be less then 0");
            }
            this.httpConnectTimeout = httpConnectTimeout;
            return this;
        }

        public Builder putConsumerThreadCount(int putConsumerThreadCount) {
            if (putConsumerThreadCount < 1) {
                throw new IllegalArgumentException("The threadCount can't be less then 1");
            }
            this.putConsumerThreadCount = putConsumerThreadCount;
            return this;
        }

        public Builder batchPutSize(int batchPutSize) {
            if (batchPutSize < 1) {
                throw new IllegalArgumentException("The size can't be less then 1");
            }
            this.batchPutSize = batchPutSize;
            return this;
        }

        public Builder batchPutBufferSize(int batchPutBufferSize) {
            if (batchPutBufferSize < 1) {
                throw new IllegalArgumentException("The size can't be less then 1");
            }
            this.batchPutBufferSize = batchPutBufferSize;
            return this;
        }

        public Builder batchPutTimeLimit(int batchPutTimeLimit) {
            if (batchPutTimeLimit < 1) {
                throw new IllegalArgumentException("The time limit can't be less then 1");
            }
            this.batchPutTimeLimit = batchPutTimeLimit;
            return this;
        }

        public Builder readonly() {
            this.readonly = true;
            return this;
        }

        public Builder batchPutCallBack(BatchPutHttpResponseCallback.BatchPutCallBack batchPutCallBack) {
            this.batchPutCallBack = batchPutCallBack;
            return this;
        }

    }

    public static Builder address(String host, int port) {
        return new Builder(host, port);
    }

}
