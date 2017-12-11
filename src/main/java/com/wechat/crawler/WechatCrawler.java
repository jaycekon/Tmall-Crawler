package com.wechat.crawler;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.jaycekon.model.BaseSession;
import com.jaycekon.model.HttpRequestData;
import com.jaycekon.util.FetchUtils;
import com.jaycekon.util.Util;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.util.*;

/**
 * Created by Jaycekon on 2017/12/11.
 */
public class WechatCrawler {
    private Logger logger = LoggerFactory.getLogger(WechatCrawler.class);

    private static final String APPID = "wx782c26e4c19acffb";
    private static String deviceId = "e" + System.currentTimeMillis();

    // 特殊用户 须过滤
    private static Set<String> API_SPECIAL_USER = new HashSet<String>(Arrays.asList("newsapp", "filehelper", "weibo", "qqmail",
            "fmessage", "tmessage", "qmessage", "qqsync",
            "floatbottle", "lbsapp", "shakeapp", "medianote",
            "qqfriend", "readerapp", "blogapp", "facebookapp",
            "masssendapp", "meishiapp", "feedsapp", "voip",
            "blogappweixin", "brandsessionholder", "weixin",
            "weixinreminder", "officialaccounts", "wxitil",
            "notification_messages", "wxid_novlwrv3lqwv11",
            "gh_22b87fa7cb3c", "userexperience_alarm"));

    public WechatCrawler() {
        System.setProperty("https.protocols", "TLSv1");
        System.setProperty("jsse.enableSNIExtension", "false");
    }

    public static void main(String[] args) {
        BaseSession session = BaseSession.create();
        WechatCrawler crawler = new WechatCrawler();
        crawler.getUUID(session);
        crawler.getVerifycode(session);
        Scanner scanner = new Scanner(System.in);
        //阻塞
        scanner.nextLine();
        crawler.checkLogin(session);
        crawler.login(session);
        crawler.initWechat(session);
        crawler.getContactList(session);
        crawler.crawlLoan(session);
    }


    public boolean getUUID(BaseSession session) {
        HttpRequestData data = session.getHttpRequestData();

        try {
            StringBuilder url = new StringBuilder("https://login.weixin.qq.com/jslogin");
            url.append("?appid=").append(APPID).append("&fun=new&lang=zh_CN&_=").append(System.currentTimeMillis());

            String result = FetchUtils.get(data, url.toString());
            logger.info(result);

            String code = Util.match("window.QRLogin.code = (\\d+);", result);
            if (StringUtils.isEmpty(code)) {
                logger.info("微信登录获取用户UUID 失败！");
                return false;
            }
            String uuid = Util.match("window.QRLogin.uuid = \"(.*)\";", result);
            session.setUuid(uuid);

            return true;
        } catch (Exception e) {
            logger.info("微信登录获取图片验证码异常! {}", e.getMessage(), e);
        }
        return false;
    }


    public void getVerifycode(BaseSession session) {
        HttpRequestData data = session.getHttpRequestData();
        File output = new File("qrcode.jpg");
        try (FileOutputStream fos = new FileOutputStream(output)) {
            String url = "https://login.weixin.qq.com/qrcode/" + session.getUuid();
            Map<String, String> map = new HashMap<>();
            map.put("t", "webwx");
            map.put("_", String.valueOf(System.currentTimeMillis()));

            byte[] bytes = FetchUtils.getPostByte(data, url, map);
            //暂时保存本地，后续返回到前端
            fos.write(bytes);
            logger.info("获取二维码成功！");
        } catch (Exception e) {
            logger.error("获取登录二维码异常！{}", e.getMessage(), e);
        }

    }


    public void checkLogin(BaseSession session) {
        HttpRequestData data = session.getHttpRequestData();
        try {
            //需要校验两次？ tip =1 和 tip = 0
            String result = FetchUtils.get(data, "https://wx.qq.com/cgi-bin/mmwebwx-bin/login?tip=1&uuid="
                    + session.getUuid() + "&_" + System.currentTimeMillis());

            logger.info(result);

            String secondAuth = FetchUtils.get(data, "https://wx2.qq.com/cgi-bin/mmwebwx-bin/login?tip=0&uuid=" +
                    session.getUuid() + "&_" + System.currentTimeMillis());

            logger.info(secondAuth);

            String pm = Util.match("window.redirect_uri=\"(\\S+?)\";", secondAuth);
            session.setRedirectUrl(pm + "&fun=new");

        } catch (Exception e) {
            logger.error("微信校验登录异常！{}", e.getMessage(), e);
        }
    }


    public void login(BaseSession session) {
        HttpRequestData data = session.getHttpRequestData();
        try {
            String result = FetchUtils.get(data, session.getRedirectUrl());

            data.printCookie();

            session.setSkey(Util.match("<skey>(\\S+)</skey>", result));
            session.setSid(Util.match("<wxsid>(\\S+)</wxsid>", result));
            session.setUin(Util.match("<wxuin>(\\S+)</wxuin>", result));
            session.setPassTicket(Util.match("<pass_ticket>(\\S+)</pass_ticket>", result));
        } catch (Exception e) {
            logger.error("微信登录异常！{}", e.getMessage(), e);
        }
    }

    public void initWechat(BaseSession session) {
        HttpRequestData data = session.getHttpRequestData();
        try {
            String url = "https://wx2.qq.com/cgi-bin/mmwebwx-bin/webwxinit?pass_ticket=" +
                    session.getPassTicket() + "&skey=" + session.getSkey() + "&r=" + System.currentTimeMillis();

            JSONObject map = new JSONObject();
            JSONObject param = new JSONObject();
            param.put("Skey", session.getSkey());
            param.put("Uin", session.getUin());
            param.put("DeviceID", deviceId);
            param.put("Sid", session.getSid());

            map.put("BaseRequest", param.toJSONString());
            String result = FetchUtils.postText(data, url, map.toJSONString(), "application/json", "utf-8");

            JSONObject jsonObject = JSONObject.parseObject(result);
            makeSynckey(session, jsonObject);

            JSONObject baseResponse = jsonObject.getJSONObject("BaseResponse");
            if (baseResponse.getInteger("Ret") == 0) {
                logger.info("微信登录成功！");
                return;
            }

        } catch (Exception e) {
            logger.error("微信初始化异常！{}", e.getMessage(), e);
        }

        logger.info("微信登录失败！{}");
    }


    private void makeSynckey(BaseSession session, JSONObject dic) {
        JSONObject synckeyDic = dic.getJSONObject("SyncKey");
        StringBuilder synckey = new StringBuilder();
        JSONArray list = synckeyDic.getJSONArray("List");
        for (Object element : list) {
            JSONObject item = (JSONObject) element;
            synckey.append("|").append(item.getString("Key")).append("_").append(item.getString("Val"));
        }
        if (synckey.length() > 0) {
            session.setSynckey(synckey.substring(1));
        }
    }


    public void getContactList(BaseSession session) {
        HttpRequestData data = session.getHttpRequestData();
        try {
            String url = "https://wx2.qq.com/cgi-bin/mmwebwx-bin/webwxgetcontact?pass_ticket="
                    + session.getPassTicket() + "&skey=" + session.getSkey() + "&r=" + System.currentTimeMillis();
            String result = FetchUtils.postText(data, url, "", "application/json", "utf-8");

            JSONObject jsonObject = JSONObject.parseObject(result);
            int memberCount = jsonObject.getInteger("MemberCount");
            logger.info("好友数量为：{}", memberCount);
        } catch (Exception e) {
            logger.error("微信获取用户好友异常!{}", e.getMessage(), e);
        }
    }

    public void crawlLoan(BaseSession session) {
        HttpRequestData data = session.getHttpRequestData();
        try {
            String result = FetchUtils.get(data, "https://pay.weixin.qq.com/webank/webankqueryaccount?channelid=259");
            logger.info(result);
        } catch (Exception e) {
            logger.error("获取微粒贷信息异常！{}", e.getMessage(), e);
        }
    }
}
