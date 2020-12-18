package com.bme.opentsdb.client.http.callback;

import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpResponse;
import org.apache.http.concurrent.FutureCallback;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * 定义一个FutureCallBack，用来对任务完成、异常、取消后进行减数
 * @author yutyi
 * @date 2020/12/17
 */
@Slf4j
public class GracefulCloseFutureCallBack implements FutureCallback<HttpResponse> {

    /**
     * 未完成任务数
     */
    private final AtomicInteger unCompletedTaskNum;

    private final FutureCallback<HttpResponse> futureCallback;

    public GracefulCloseFutureCallBack(AtomicInteger unCompletedTaskNum, FutureCallback<HttpResponse> futureCallback) {
        super();
        this.unCompletedTaskNum = unCompletedTaskNum;
        this.futureCallback = futureCallback;
    }

    @Override
    public void completed(HttpResponse result) {
        //调用业务FutureCallback#completed
        futureCallback.completed(result);
        // 任务处理完毕，再减数
        log.debug("等待完成的任务数:{}", unCompletedTaskNum.decrementAndGet());
    }

    @Override
    public void failed(Exception ex) {
        //调用业务FutureCallback#failed
        futureCallback.failed(ex);
        // 任务处理完毕，再减数
        log.debug("等待完成的任务数:{}", unCompletedTaskNum.decrementAndGet());
    }

    @Override
    public void cancelled() {
        //调用业务FutureCallback#cancelled
        futureCallback.cancelled();
        // 任务处理完毕，再减数
        log.debug("等待完成的任务数:{}", unCompletedTaskNum.decrementAndGet());
    }
}
