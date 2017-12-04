package com.Jaycekon.demo.model;

import org.apache.commons.httpclient.Cookie;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.httpclient.cookie.CookiePolicy;
import org.apache.commons.httpclient.params.HttpClientParams;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 在需要换ip的时候，不建议重新new一个该对象，建议使用以下方法
 *
 * @author kafka
 */
public class HttpRequestData implements Serializable {


    public static HttpRequestData creatHttpRequestData() {
        HttpRequestData httpRequestData = new HttpRequestData();
        httpRequestData.httpClient().getParams().setCookiePolicy(CookiePolicy.BROWSER_COMPATIBILITY);
        httpRequestData.httpClient().getParams().setSoTimeout(20000);
        httpRequestData.httpClient().getParams().setConnectionManagerTimeout(20000);
        httpRequestData.httpClient().getHttpConnectionManager().getParams().setSoTimeout(20000);
        httpRequestData.httpClient().getHttpConnectionManager().getParams().setConnectionTimeout(20000);
        return httpRequestData;
    }

    public static HttpRequestData creatHttpRequestData(boolean useProxy, String proxyIp, int proxyPort) {
        HttpRequestData httpRequestData = new HttpRequestData(useProxy, proxyIp, proxyPort);
        httpRequestData.httpClient().getParams().setCookiePolicy(CookiePolicy.BROWSER_COMPATIBILITY);
        httpRequestData.httpClient().getParams().setSoTimeout(20000);
        httpRequestData.httpClient().getParams().setConnectionManagerTimeout(20000);
        httpRequestData.httpClient().getHttpConnectionManager().getParams().setSoTimeout(20000);
        httpRequestData.httpClient().getHttpConnectionManager().getParams().setConnectionTimeout(20000);
        return httpRequestData;
    }


    private static final long serialVersionUID = -3942064259653141609L;

    private transient Logger logger = LoggerFactory.getLogger(HttpRequestData.class);

    private Map<String, String> headers;
    private transient HttpClient httpClient;

    private Map<String, String> responseHeaders;

    // 是否使用代理
    private boolean useProxy;

    private String proxyIp;
    private int proxyPort;

    /**
     * 默认使用代理
     */
    private HttpRequestData() {
        this(false);
    }

    private HttpRequestData(boolean useProxy) {
        this.useProxy = useProxy;
    }

    private HttpRequestData(boolean useProxy, String proxyIp, int proxyPort) {
        this.useProxy = useProxy;
        this.proxyIp = proxyIp;
        this.proxyPort = proxyPort;
    }

    public Map<String, String> getHeaders() {
        if (null == headers)
            headers = new HashMap<>();
        return headers;
    }

    public void setHeaders(String name, String value) {
        headers = getHeaders();
        headers.put(name, value);
    }

    public void removeHeader(String name) {
        getHeaders().remove(name);
    }

    public void clearHeaders() {
        getHeaders().clear();
    }

    /**
     * 获取httpClient
     * <p>
     * 如果需要并发使用httpClient，务必记得使用MultiThreadedHttpConnectionManager
     * 如果需要设置cookie，调用方法 initHttpClientCookie
     */
    public HttpClient httpClient(boolean useProxy, String proxyIp,
                                 int proxyPort) {
        if (this.getHttpClient() != null) {
            return this.getHttpClient();
        }

        this.setHttpClient(new HttpClient(new HttpClientParams(),
                new MultiThreadedHttpConnectionManager()));// 连接在releaseConnection后总是被关闭
        // 设置代理IP
        if (useProxy && !StringUtils.isEmpty(proxyIp)) {
            getHttpClient().getHostConfiguration().setProxy(proxyIp, proxyPort);
        }
        try {
            getHttpClient().getParams().setCookiePolicy(
                    CookiePolicy.BROWSER_COMPATIBILITY);
            getHttpClient().getParams().setSoTimeout(20000);// 20秒超时response
            getHttpClient().getParams().setConnectionManagerTimeout(20000);// 20秒超时connect
            getHttpClient().getHttpConnectionManager().getParams()
                    .setSoTimeout(20000);
            getHttpClient().getHttpConnectionManager().getParams()
                    .setConnectionTimeout(20000);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return this.getHttpClient();
    }

    public HttpClient httpClient() {
        return httpClient(this.useProxy, this.proxyIp, this.proxyPort);
    }

    /**
     * 添加请求所需的cookies
     *
     * @param domain  域
     * @param cookies 值
     */
    public void initHttpClientCookie(String domain, Map<String, String> cookies) {
        if (null == cookies || cookies.isEmpty()) {
            return;
        }
        for (Map.Entry<String, String> entry : cookies.entrySet()) {
            httpClient().getState().addCookie(
                    new Cookie(domain, entry.getKey(), entry.getValue(), "/",
                            null, false));
        }
    }

    /**
     * 添加请求所需的cookies
     *
     * @param domain  域
     * @param cookies 值
     */
    public void initHttpClientCookieNew(String domain, String cookies) {
        if (StringUtils.isBlank(cookies)) {
            return;
        }
        for (String x : cookies.split(";")) {
            String[] xx = x.trim().split("=");
            if (xx.length > 1) {
                String val = StringUtils.join(Arrays
                        .asList(xx).subList(1, xx.length), "=");
                httpClient().getState().addCookie(
                        new Cookie(domain, xx[0], val, "/", null, false));
            }
        }
    }

    /**
     * 添加请求所需的cookies
     *
     * @param domain  域
     * @param cookies 值
     */
    public void initHttpClientCookie(String domain, String cookies) {
        if (StringUtils.isBlank(cookies)) {
            return;
        }
        for (String x : cookies.split(";")) {
            String[] xx = x.trim().split("=");
            String val = StringUtils.join(Arrays
                    .asList(xx).subList(1, xx.length), "=");
            httpClient().getState().addCookie(
                    new Cookie(domain, xx[0], val, "/", null, false));
        }
    }

    /**
     * 添加请求所需的cookies
     *
     * @param domain 域
     * @param name   cookie名称
     * @param value  值
     */
    public void initHttpClientCookie(String domain, String name, String value) {
        httpClient().getState().addCookie(
                new Cookie(domain, name, value, "/", null, false));
    }

    public String getCookieValue(String name) {
        Cookie[] cookies = httpClient().getState().getCookies();
        for (int i = 0; i < cookies.length; i++) {
            if (cookies[i].getName().equals(name)) {
                return cookies[i].getValue();
            }

        }
        return "";
    }

    public void printCookie() {
        logger.error("=====================cookie=====================");
        for (Cookie e : httpClient().getState().getCookies()) {
            logger.error(e.getDomain() + ":\t" + e.getName() + "=" + e.getValue());
        }
        logger.error("=====================cookie end=====================");
    }

    public HttpClient getHttpClient() {
        return httpClient;
    }

    public void setHttpClient(HttpClient httpClient) {
        this.httpClient = httpClient;
    }

    public Map<String, String> getResponseHeaders() {
        if (responseHeaders == null) {
            responseHeaders = new LinkedHashMap<>();
        }
        return responseHeaders;
    }

    public void setResponseHeaders(Map<String, String> responseHeaders) {
        this.responseHeaders = responseHeaders;
    }
}
