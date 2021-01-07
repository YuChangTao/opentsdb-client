package com.bme.opentsdb.client.common.util;

import com.bme.opentsdb.client.bean.response.ErrorResponse;
import com.bme.opentsdb.client.common.Json;
import com.bme.opentsdb.client.exception.HttpException;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.nio.charset.Charset;

/**
 * Http响应工具类
 *
 * @author yutyi
 * @date 2020/12/16
 */
public class ResponseUtil {

    private static final int CODE200 = 200;
    private static final int CODE204 = 204;
    private static final int CODE301 = 301;

    private static final int CODE400 = 400;
    private static final int CODE404 = 404;
    private static final int CODE405 = 405;
    private static final int CODE406 = 406;
    private static final int CODE408 = 408;
    private static final int CODE413 = 413;
    private static final int CODE500 = 500;
    private static final int CODE501 = 501;
    private static final int CODE503 = 503;

    /***
     * 解析响应的内容
     * @param response 响应内容
     * @return
     * @throws IOException
     */
    public static String getContent(HttpResponse response) throws IOException {
        if (checkGT400(response)) {
            throw new HttpException(convert(response));
        } else {
            return getContentString(response);
        }
    }

    private static String getContentString(HttpResponse response) throws IOException {
        HttpEntity entity = response.getEntity();
        if (entity != null) {
            return EntityUtils.toString(entity, Charset.defaultCharset());
        }
        return null;
    }

    /***
     * 判断响应码的是否为400以上，如果是，则表示出错了
     * @param response  查询对象
     * @return
     */
    private static boolean checkGT400(HttpResponse response) {
        StatusLine statusLine = response.getStatusLine();
        int statusCode = statusLine.getStatusCode();
        if (statusCode >= CODE400) {
            return true;
        }
        return false;
    }

    /***
     * 将响应内容转换成errorResponse
     * @param response  查询对象
     * @return
     */
    private static ErrorResponse convert(HttpResponse response) throws IOException {
        return Json.readValue(getContentString(response), ErrorResponse.class);
    }


}
