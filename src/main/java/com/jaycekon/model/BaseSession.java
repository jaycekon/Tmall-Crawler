package com.jaycekon.model;


import org.apache.commons.httpclient.HttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.*;

public class BaseSession implements Serializable {
    private static final long serialVersionUID = -8170349978725057104L;
    private static final transient Logger logger = LoggerFactory.getLogger(BaseSession.class);
    private HttpRequestData httpRequestData;
    private String sessionId;
    private String account;
    private String password;
    private String verifyCode;

    private String html;
    private String platform;
    private String token;
    private User user;

    public BaseSession() {

    }

    private BaseSession(String sessionId) {
        this.sessionId = sessionId;
        user = new User();
        newHttpRequestData();

    }

    public static BaseSession create() {
        return new BaseSession(UUID.randomUUID().toString().replaceAll("-", ""));
    }

    private void newHttpRequestData() {
        logger.info("httpRequestData is empty, it will create a new HttpRequestData!");
        httpRequestData = HttpRequestData.creatHttpRequestData();
    }

    public HttpClient innerHttpClient() {
        if (httpRequestData == null) {
            httpRequestData = HttpRequestData.creatHttpRequestData();
        }
        return httpRequestData.httpClient();
    }


    public HttpRequestData getHttpRequestData() {
        if (httpRequestData == null) {
            newHttpRequestData();
        }
        return httpRequestData;
    }

    public void setHttpRequestData(HttpRequestData httpRequestData) {
        this.httpRequestData = httpRequestData;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getVerifyCode() {
        return verifyCode;
    }

    public void setVerifyCode(String verifyCode) {
        this.verifyCode = verifyCode;
    }

    public String getHtml() {
        return html;
    }

    public void setHtml(String html) {
        this.html = html;
    }




    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getPlatform() {
        return platform;
    }

    public void setPlatform(String platform) {
        this.platform = platform;
    }



    void setLoginInfo(String password, String account) {
        //填充账号密码
        setPassword(password);
        setAccount(account);
    }

    public void setAccountAndPswToUser() {
        //设置用户的账号和密码
        getUser().setAccount(account);
        getUser().setPassword(password);
    }

}
