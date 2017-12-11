package com.jaycekon.util;

import com.jaycekon.model.HttpRequestData;
import org.apache.commons.httpclient.*;
import org.apache.commons.httpclient.methods.*;
import org.apache.commons.httpclient.methods.multipart.MultipartRequestEntity;
import org.apache.commons.httpclient.methods.multipart.Part;
import org.apache.commons.httpclient.methods.multipart.StringPart;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.apache.commons.httpclient.protocol.Protocol;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Stream;

public final class FetchUtils {

    private static final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/61.0.3163.91 Safari/537.36";
    public static final int DEFAULT_TIMEOUT = 20000;
    private static String locations = "Location";
    public static final String USER_AGENT_KEY = "User-Agent";
    private static String setCookie = "Set-COOKIE";

    private static Logger logger = LoggerFactory.getLogger(FetchUtils.class);
    private static int size = 1 << 23;

    static {
        Protocol ignoreSecureHttps = new Protocol("https", new IgnoreSecureProtocolSocketFactory(), 443);
        Protocol.registerProtocol("https", ignoreSecureHttps);
    }

    private FetchUtils() {
    }

    /**
     * 处理url带的特殊字符
     *
     * @param url
     * @return
     */
    private static String replaceUrl(String url) {
        return url.replaceAll("&amp;", "&");
    }

    /**
     * 初始化httpMethod
     */
    private static void initHttpMethod(HttpRequestData data, HttpMethod method) throws URIException {
        if (!data.getHeaders().containsKey(USER_AGENT_KEY) || data.getHeaders().get(USER_AGENT_KEY) == null) {
            data.setHeaders(USER_AGENT_KEY, USER_AGENT);
        }
        // 添加请求头参数
        for (Entry<String, String> entry : data.getHeaders().entrySet()) {
            method.addRequestHeader(entry.getKey(), entry.getValue());
        }
        method.getParams().setVersion(HttpVersion.HTTP_1_1);
        method.getParams().setBooleanParameter(HttpMethodParams.SINGLE_COOKIE_HEADER, true);
    }

    /**
     * http get 字符数组
     */
    public static byte[] getBytes(HttpRequestData data, String url) throws IOException {
        GetMethod get = new GetMethod(replaceUrl(url));
        HttpClient httpClient = data.httpClient();
        try {
            initHttpMethod(data, get);
            execute(httpClient, get);
            trace(httpClient, url, null);

            return get.getResponseBody(size);
        } finally {
            get.releaseConnection();
            httpClient.getHttpConnectionManager().closeIdleConnections(0);
        }
    }


    public static String getRedirectUrlFromResponseHeader(Header[] headers) {
        if (headers != null) {
            for (Header h : headers) {
                if (locations.equals(h.getName()))
                    return h.getValue();
            }
        }
        return "";
    }


    /**
     * http get调用
     */
    public static String get(HttpRequestData data, String url) throws IOException {
        GetMethod get = new GetMethod(replaceUrl(url));
        HttpClient httpClient = data.httpClient();
        try {
            initHttpMethod(data, get);
            execute(httpClient, get);
            String html = get.getResponseBodyAsString(size);
            trace(httpClient, url, html);
            return html;
        } finally {
            get.releaseConnection();
            httpClient.getHttpConnectionManager().closeIdleConnections(0);
        }
    }


    public static String options(HttpRequestData data, String url) throws IOException {
        OptionsMethod get = new OptionsMethod(replaceUrl(url));
        HttpClient httpClient = data.httpClient();
        try {
            initHttpMethod(data, get);

            execute(httpClient, get);
            String html = get.getResponseBodyAsString(size);
            trace(httpClient, url, html);
            return html;
        } finally {
            get.releaseConnection();
            httpClient.getHttpConnectionManager().closeIdleConnections(0);
        }
    }

    /**
     * http get调用
     */
    public static String getQueryString(HttpRequestData data, String url) throws IOException {
        GetMethod get = new GetMethod(replaceUrl(url));
        HttpClient httpClient = data.httpClient();
        try {
            initHttpMethod(data, get);
            execute(httpClient, get);
            return get.getQueryString();
        } finally {
            get.releaseConnection();
            httpClient.getHttpConnectionManager().closeIdleConnections(0);
        }
    }

    /**
     * 循环get
     *
     * @param data 请求
     * @param urls 重定向地址列表
     * @return html页面，如果异常则返回""
     * @throws IOException
     */
    public static String recurseGet(HttpRequestData data, String[] urls) throws IOException {
        for (String url : urls) {
            GetMethod get = new GetMethod(replaceUrl(url));
            HttpClient httpClient = data.httpClient();
            try {
                initHttpMethod(data, get);
                execute(httpClient, get);
                String html = get.getResponseBodyAsString(size);
                trace(httpClient, url, html);
                if (get.getStatusCode() == 200 || (get.getStatusCode() == 304 && get.getResponseHeader(locations) == null))
                    return html;
            } finally {
                get.releaseConnection();
                httpClient.getHttpConnectionManager().closeIdleConnections(0);
            }
        }
        return "";
    }

    /**
     * 重定向get
     *
     * @param data 请求
     * @param url  重定向地址
     * @return html页面，如果异常则返回""
     * @throws IOException
     */
    public static String redirectGet(HttpRequestData data, String url) throws IOException {
        while (true) {
            GetMethod get = new GetMethod(replaceUrl(url));
            HttpClient httpClient = data.httpClient();
            String redirectUrl = url;
            try {
                initHttpMethod(data, get);
                execute(httpClient, get);
                Header setCookieHeader = get.getResponseHeader(setCookie);
                if (setCookieHeader != null) {
                    data.httpClient().getState().addCookies(getRespCookies(setCookieHeader.getValue()));
                }
                if (get.getStatusCode() == 302) {
                    redirectUrl = get.getResponseHeader(locations).getValue();
                } else if (get.getStatusCode() == 200) {
                    String html = get.getResponseBodyAsString(size);
                    trace(httpClient, redirectUrl, html);
                    return html;
                }
                return "";
            } finally {
                get.releaseConnection();
                httpClient.getHttpConnectionManager().closeIdleConnections(0);
            }
        }
    }

    /**
     * http post调用
     */
    public static String post(HttpRequestData data, String url, Map<String, String> params, boolean setCookie) throws IOException {
        return post(data, url, params, DEFAULT_TIMEOUT, setCookie);
    }

    /**
     * http post调用
     */
    public static String post(HttpRequestData data, String url, Map<String, String> params) throws IOException {
        return post(data, url, params, DEFAULT_TIMEOUT, false);
    }

    /**
     * http post调用
     */
    public static String post(HttpRequestData data, String url, Map<String, String> params, int timeout) throws IOException {
        return post(data, url, params, timeout, false);
    }

    /**
     * http post调用
     */
    public static String post(HttpRequestData data, String url, Map<String, String> params, int timeout, boolean setResponseHeader) throws IOException {
        PostMethod post = new PostMethod(replaceUrl(url));
        HttpClient httpClient = data.httpClient();
        try {
            initHttpMethod(data, post);
            post.getParams().setSoTimeout(timeout);// 5秒超时response
            for (Entry<String, String> entry : params.entrySet()) {
                post.addParameter(entry.getKey(), entry.getValue() == null ? "" : entry.getValue());
            }
            execute(httpClient, post);
            String html = post.getResponseBodyAsString(size);

            if (setResponseHeader) {
                setResponseHeader(post, data);
            }
            trace(httpClient, url, html);

            return html;
        } finally {
            post.releaseConnection();
            httpClient.getHttpConnectionManager().closeIdleConnections(0);
        }
    }

    public static String postMultipart(HttpRequestData data, String url, Map<String, String> params) throws IOException {
        PostMethod post = new PostMethod(replaceUrl(url));
        HttpClient httpClient = data.httpClient();
        try {
            initHttpMethod(data, post);
            post.getParams().setSoTimeout(DEFAULT_TIMEOUT);// 5秒超时response

            List<Part> parts = new ArrayList<>();
            for (Entry<String, String> entry : params.entrySet()) {
                parts.add(new StringPart(entry.getKey(), entry.getValue() == null ? "" : entry.getValue()));
            }
            Part[] pars = new Part[4];
            parts.toArray(pars);

            RequestEntity requestEntity = new MultipartRequestEntity(pars, post.getParams());
            post.setRequestEntity(requestEntity);

            execute(httpClient, post);
            String html = post.getResponseBodyAsString(size);

            trace(httpClient, url, html);

            return html;
        } finally {
            post.releaseConnection();
            httpClient.getHttpConnectionManager().closeIdleConnections(0);
        }
    }

    private static void setResponseHeader(PostMethod post, HttpRequestData data) {
        try {
            for (Header header : post.getResponseHeaders()) {
                if (!"Set-Cookie".equalsIgnoreCase(header.getName())) {
                    data.getResponseHeaders().put(header.getName(), header.getValue());
                }
            }

        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }


    public static String postRedirect(HttpRequestData data, String url, Map<String, String> params) throws IOException {
        PostMethod post = new PostMethod(replaceUrl(url));
        HttpClient httpClient = data.httpClient();
        try {
            initHttpMethod(data, post);
            for (Entry<String, String> entry : params.entrySet()) {
                post.addParameter(entry.getKey(), entry.getValue() == null ? "" : entry.getValue());
            }
            execute(httpClient, post);
            Header location = post.getResponseHeader("Location");
            String html = post.getResponseBodyAsString(size);
            if (location != null) { // redirect
                html = get(data, location.getValue());
            }

            trace(httpClient, url, html);

            return html;
        } finally {
            post.releaseConnection();
            httpClient.getHttpConnectionManager().closeIdleConnections(0);
        }
    }

    /**
     * http post调用,返回map
     */
    public static Map<String, String> postAndLocation(HttpRequestData data, String url, Map<String, String> params) throws IOException {
        Map<String, String> map = new HashMap<>();
        PostMethod post = new PostMethod(replaceUrl(url));
        HttpClient httpClient = data.httpClient();
        try {
            initHttpMethod(data, post);
            for (Entry<String, String> entry : params.entrySet()) {
                post.addParameter(entry.getKey(), entry.getValue());
            }
            execute(httpClient, post);
            String html = post.getResponseBodyAsString(size);

            trace(httpClient, url, html);

            String location = "";
            if (post.getStatusCode() == 302 && post.getResponseHeader(locations) != null) {
                location = post.getResponseHeader(locations).getValue();
            }
            map.put("location", location);
            map.put("html", html);
            return map;
        } finally {
            post.releaseConnection();
            httpClient.getHttpConnectionManager().closeIdleConnections(0);
        }
    }

    public static String postAndRedirect(HttpRequestData data, String url, Map<String, String> params) throws IOException {
        PostMethod post = new PostMethod(replaceUrl(url));
        HttpClient httpClient = data.httpClient();
        String redirectUrl = url;
        try {
            initHttpMethod(data, post);
            for (Entry<String, String> entry : params.entrySet()) {
                post.addParameter(entry.getKey(), entry.getValue());
            }
            execute(httpClient, post);
            Header setCookieHeader = post.getResponseHeader(setCookie);
            if (setCookieHeader != null) {
                data.httpClient().getState().addCookies(getRespCookies(setCookieHeader.getValue()));
            }
            String html = post.getResponseBodyAsString(size);
            if (post.getStatusCode() == 302 && post.getResponseHeader(locations) != null) {
                redirectUrl = post.getResponseHeader(locations).getValue();
                html = redirectGet(data, redirectUrl);
            }

            trace(httpClient, url, html);
            return html;
        } finally {
            post.releaseConnection();
            httpClient.getHttpConnectionManager().closeIdleConnections(0);
        }
    }

    /**
     * http post调用
     */
    public static String post(HttpRequestData data, String url, Map<String, String> params, String encode) throws IOException {
        PostMethod post = new PostMethod(replaceUrl(url));
        HttpClient httpClient = data.httpClient();
        try {
            initHttpMethod(data, post);
            post.getParams().setParameter(HttpMethodParams.HTTP_CONTENT_CHARSET, encode);
            for (Entry<String, String> entry : params.entrySet()) {
                post.addParameter(entry.getKey(), entry.getValue());
            }
            execute(httpClient, post);
            String html = post.getResponseBodyAsString(size);

            Header setCookieHeader = post.getResponseHeader(setCookie);
            if (setCookieHeader != null) {
                data.httpClient().getState().addCookies(getRespCookies(setCookieHeader.getValue()));
            }

            trace(httpClient, url, html);

            return html;
        } finally {
            post.releaseConnection();
            httpClient.getHttpConnectionManager().closeIdleConnections(0);
        }
    }


    /**
     * 增加对gzip返回格式的处理。
     */
    public static String postText(HttpRequestData data, String url, String text, String contentType, String encoding) throws IOException {
        PostMethod post = new PostMethod(replaceUrl(url));
        HttpClient httpClient = data.httpClient();
        try {
            initHttpMethod(data, post);
            RequestEntity entity = new StringRequestEntity(text, contentType, encoding);
            post.setRequestEntity(entity);
            execute(httpClient, post);
            String html = Stream.of(post.getResponseHeaders())
                    .filter(header -> header.toString().contains("gzip"))
                    .count() > 0 ? GZIPUtils.uncompressToString(post.getResponseBody()) : post.getResponseBodyAsString(size);
            trace(httpClient, url, html);
            return html;
        } finally {
            post.releaseConnection();
            httpClient.getHttpConnectionManager().closeIdleConnections(0);
        }
    }

    /**
     * @param data
     * @param url
     * @param text
     * @param contentType
     * @param encoding
     * @return
     * @throws IOException put方法
     */
    public static String putText(HttpRequestData data, String url, String text, String contentType, String encoding) throws IOException {
        PutMethod put = new PutMethod(replaceUrl(url));
        HttpClient httpClient = data.httpClient();
        try {
            initHttpMethod(data, put);
            RequestEntity entity = new StringRequestEntity(text, contentType, encoding);
            put.setRequestEntity(entity);
            execute(httpClient, put);
            String html = Stream.of(put.getResponseHeaders())
                    .filter(header -> header.toString().contains("gzip"))
                    .count() > 0 ? GZIPUtils.uncompressToString(put.getResponseBody()) : put.getResponseBodyAsString(size);
            trace(httpClient, url, html);
            return html;
        } finally {
            put.releaseConnection();
            httpClient.getHttpConnectionManager().closeIdleConnections(0);
        }
    }

    public static String postText(HttpRequestData data, String url, String text) throws IOException {
        return postText(data, url, text, "application/json", "utf-8");
    }

    public static InputStream getPostBytes(HttpRequestData data, String url, Map<String, String> params) throws IOException {
        PostMethod post = new PostMethod(replaceUrl(url));
        HttpClient httpClient = data.httpClient();
        try {
            initHttpMethod(data, post);
            for (Entry<String, String> entry : params.entrySet()) {
                post.addParameter(entry.getKey(), entry.getValue());
            }
            execute(httpClient, post);
            return post.getResponseBodyAsStream();
        } finally {
            post.releaseConnection();
            httpClient.getHttpConnectionManager().closeIdleConnections(0);
        }
    }

    public static byte[] getPostByte(HttpRequestData data, String url, Map<String, String> params) throws IOException {
        PostMethod post = new PostMethod(replaceUrl(url));
        HttpClient httpClient = data.httpClient();
        try {
            initHttpMethod(data, post);
            for (Entry<String, String> entry : params.entrySet()) {
                post.addParameter(entry.getKey(), entry.getValue());
            }
            execute(httpClient, post);
            return post.getResponseBody();
        } finally {
            post.releaseConnection();
            httpClient.getHttpConnectionManager().closeIdleConnections(0);
        }
    }

    /**
     * 日志
     */
    private static void trace(HttpClient httpClient, String url, String html) {
        if (logger.isDebugEnabled()) {
            logger.info("==========printing http request information===" + url + "===========================================");
            for (Cookie e : httpClient.getState().getCookies()) {
                logger.info(e.getName() + "===" + e.getValue());
            }
            if (!StringUtils.isEmpty(html)) {
                logger.info(html);
            } else {
                logger.info("html is empty");
            }
            logger.info("==========printing http request information===" + url + "===========================================");
        }
    }

    private static void execute(HttpClient client, HttpMethodBase method) throws IOException {
        long start = System.currentTimeMillis();
        method.getParams().setSoTimeout(client.getParams().getSoTimeout());
        client.executeMethod(method);
//		setRespCookies(method, client); //这个方法设置 cookie 有 bug
        long cost = System.currentTimeMillis() - start;
        if (cost > 2000 && logger.isWarnEnabled()) {
            logger.warn("http request : " + method.getURI().toString() + " cost " + (System.currentTimeMillis() - start) + " milliseconds with result : " + method.getStatusCode()
                    + " " + method.getStatusText());
        }

    }

    private static void setRespCookies(HttpMethodBase post, HttpClient httpClient) {
        Header setCookieHeader = post.getResponseHeader(setCookie);
        if (setCookieHeader != null) {
            httpClient.getState().addCookies(getRespCookies(setCookieHeader.getValue()));
        }

    }

    private static Cookie[] getRespCookies(String respCookie) {
        Cookie[] cookies = null;
        List<Cookie> list = getRespCookieList(respCookie);
        if (list != null && !list.isEmpty()) {
            cookies = new Cookie[list.size()];
            list.toArray(cookies);
        }
        return cookies;
    }

    private static List<Cookie> getRespCookieList(String respCookie) {
        List<Cookie> setCookies = new ArrayList<>();
        if (StringUtils.isNotBlank(respCookie)) {
            String[] perCookies = respCookie.split(", ");
            for (String perCookie : perCookies) {
                parseCookie(perCookie, setCookies);
            }
        }
        return setCookies;
    }

    private static void parseCookie(String perCookie, List<Cookie> setCookies) {
        String[] infos = perCookie.split("; ");
        String name = "";
        String value = "";
        String path = "";
        String domain = "";
        for (String info : infos) {
            String[] ss = info.split("=");
            String s1 = ss[0];
            String s2 = "";
            if (ss.length > 1) {
                s2 = ss[1];
            }
            String lower = s1.toLowerCase();
            if ("path".equals(lower)) {
                path = s2;
            } else if ("domain".equals(lower)) {
                domain = s2;
            } else {
                name = s1;
                value = s2;
            }
        }
        setCookies.add(new Cookie(domain, name, value, path, null, false));
    }


    public static String postXml(HttpRequestData data, String url, String xml, String contentType, String charSet) throws IOException {
        PostMethod post = new PostMethod(url);
        HttpClient httpClient = data.httpClient();
        try {
            FetchUtils.initHttpMethod(data, post);
            StringRequestEntity requestEntity = new StringRequestEntity(xml, contentType, charSet);
            post.setRequestEntity(requestEntity);
            FetchUtils.execute(httpClient, post);
            return post.getResponseBodyAsString(1 << 23);
        } finally {
            post.releaseConnection();
            httpClient.getHttpConnectionManager().closeIdleConnections(0);
        }
    }

}
