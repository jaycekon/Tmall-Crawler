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
import java.net.URLEncoder;
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
            data.setHeaders("Host", "pay.weixin.qq.com");
            data.setHeaders("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/39.0.2171.95 Safari/537.36 MicroMessenger/6.5.2.501 NetType/WIFI WindowsWechat QBCore/3.43.691.400 QQBrowser/9.0.2524.400");

            String url = "https://wx2.qq.com/cgi-bin/mmwebwx-bin/webwxcheckurl?requrl=https%3A%2F%2Fopen.weixin.qq.com%2Fconnect%2Foauth2%2Fauthorize%3Fappid%3Dwx57849631bb367f52%26redirect_uri%3Dhttps%253A%252F%252Fpay.weixin.qq.com%252Fwebank%252Fwebankqueryaccount%253Fcc%253D1%2526channelid%253D1004%2526pass_ticket%253DEew%252BtGLkMF3GRloLMtSMwwiO%252Fe4CF0Wtd0vc%252B7YmYyI%253D%26response_type%3Dcode%26scope%3Dsnsapi_base%26state%3D123%23wechat_redirect&skey=" + URLEncoder.encode(session.getSkey(), "utf-8") + "&deviceid=e227855143313418&pass_ticket=" + session.getPassTicket() + "&opcode=2&scene=1&username=@03a2aa17f6028b8034caafdb8f9bc33552004108c9359924b47e2aa4103e2f49";
            String result = FetchUtils.get(data, url);
            logger.info(result);
            Util.saveFile(session, "wld_crawl_result", "wld", result);

            Map<String, String> map = new HashMap<>();
            map.put("channelid", "1004");
            map.put("outputtype", "json");
            map.put("overdue_unpay", "0");
            map.put("pay_date", "20180109");
            map.put("wxpay", "Y");

            String baseResult = FetchUtils.post(data, "https://pay.weixin.qq.com/webank/webankqueryloanbydate", map);
            Util.saveFile(session, "wld_crawl_baseResult", "wld", baseResult);

            map.clear();
            map.put("channelid", "1004");
            map.put("loan_receipt_nbr", "20211708160756079369");
            map.put("outputtype", "json");
            map.put("wxpay", "Y");
            String loanResult = FetchUtils.post(data, "https://pay.weixin.qq.com/webank/webankqueryloandetail", map);
            String content = new String(loanResult.getBytes("ISO8859-1"), "utf-8");
            Util.saveFile(session, "wld_crawl_loanResult", "wld", loanResult);


        } catch (Exception e) {
            logger.error("获取微粒贷信息异常！{}", e.getMessage(), e);
        }
    }
}
