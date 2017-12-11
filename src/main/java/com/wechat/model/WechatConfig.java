package com.wechat.model;

import com.alibaba.fastjson.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Jaycekon on 2017/12/11.
 */
public class WechatConfig {

    public static  Map<String, String> conf = new HashMap<>();

    protected String deviceId = "e" + System.currentTimeMillis();

    private String wxHost;

    protected String appid = "wx782c26e4c19acffb";

    protected Map<String, Object> baseRequest;

    protected JSONObject synckeyDic;
    protected String     synckey;




    private void conf_factory() {
        // wx.qq.com
        String e = this.wxHost;
        String t = "login.weixin.qq.com";
        String o = "file.wx.qq.com";
        String n = "webpush.weixin.qq.com";

        if (e.contains("wx2.qq.com")) {
            t = "login.wx2.qq.com";
            o = "file.wx2.qq.com";
            n = "webpush.wx2.qq.com";
        } else if (e.contains("wx8.qq.com")) {
            t = "login.wx8.qq.com";
            o = "file.wx8.qq.com";
            n = "webpush.wx8.qq.com";
        } else if (e.contains("qq.com")) {
            t = "login.wx.qq.com";
            o = "file.wx.qq.com";
            n = "webpush.wx.qq.com";
        } else if (e.contains("web2.wechat.com")) {
            t = "login.web2.wechat.com";
            o = "file.web2.wechat.com";
            n = "webpush.web2.wechat.com";
        } else if (e.contains("wechat.com")) {
            t = "login.web.wechat.com";
            o = "file.web.wechat.com";
            n = "webpush.web.wechat.com";
        }
        conf.put("LANG", "zh_CN");
        conf.put("API_jsLogin", "https://login.weixin.qq.com/jslogin");
        conf.put("API_qrcode", "https://login.weixin.qq.com/l/");
        conf.put("API_qrcode_img", "https://login.weixin.qq.com/qrcode/");

        conf.put("API_login", "https://" + e + "/cgi-bin/mmwebwx-bin/login");
        conf.put("API_synccheck", "https://" + n + "/cgi-bin/mmwebwx-bin/synccheck");
        conf.put("API_webwxdownloadmedia", "https://" + o + "/cgi-bin/mmwebwx-bin/webwxgetmedia");
        conf.put("API_webwxuploadmedia", "https://" + o + "/cgi-bin/mmwebwx-bin/webwxuploadmedia");
        conf.put("API_webwxpreview", "https://" + e + "/cgi-bin/mmwebwx-bin/webwxpreview");
        conf.put("API_webwxinit", "https://" + e + "/cgi-bin/mmwebwx-bin/webwxinit");
        conf.put("API_webwxgetcontact", "https://" + e + "/cgi-bin/mmwebwx-bin/webwxgetcontact");
        conf.put("API_webwxsync", "https://" + e + "/cgi-bin/mmwebwx-bin/webwxsync");
        conf.put("API_webwxbatchgetcontact", "https://" + e + "/cgi-bin/mmwebwx-bin/webwxbatchgetcontact");
        conf.put("API_webwxgeticon", "https://" + e + "/cgi-bin/mmwebwx-bin/webwxgeticon");
        conf.put("API_webwxsendmsg", "https://" + e + "/cgi-bin/mmwebwx-bin/webwxsendmsg");
        conf.put("API_webwxsendmsgimg", "https://" + e + "/cgi-bin/mmwebwx-bin/webwxsendmsgimg");
        conf.put("API_webwxsendmsgvedio", "https://" + e + "/cgi-bin/mmwebwx-bin/webwxsendvideomsg");
        conf.put("API_webwxsendemoticon", "https://" + e + "/cgi-bin/mmwebwx-bin/webwxsendemoticon");
        conf.put("API_webwxsendappmsg", "https://" + e + "/cgi-bin/mmwebwx-bin/webwxsendappmsg");
        conf.put("API_webwxgetheadimg", "https://" + e + "/cgi-bin/mmwebwx-bin/webwxgetheadimg");
        conf.put("API_webwxgetmsgimg", "https://" + e + "/cgi-bin/mmwebwx-bin/webwxgetmsgimg");
        conf.put("API_webwxgetmedia", "https://" + e + "/cgi-bin/mmwebwx-bin/webwxgetmedia");
        conf.put("API_webwxgetvideo", "https://" + e + "/cgi-bin/mmwebwx-bin/webwxgetvideo");
        conf.put("API_webwxlogout", "https://" + e + "/cgi-bin/mmwebwx-bin/webwxlogout");
        conf.put("API_webwxgetvoice", "https://" + e + "/cgi-bin/mmwebwx-bin/webwxgetvoice");
        conf.put("API_webwxupdatechatroom", "https://" + e + "/cgi-bin/mmwebwx-bin/webwxupdatechatroom");
        conf.put("API_webwxcreatechatroom", "https://" + e + "/cgi-bin/mmwebwx-bin/webwxcreatechatroom");
        conf.put("API_webwxstatusnotify", "https://" + e + "/cgi-bin/mmwebwx-bin/webwxstatusnotify");
        conf.put("API_webwxcheckurl", "https://" + e + "/cgi-bin/mmwebwx-bin/webwxcheckurl");
        conf.put("API_webwxverifyuser", "https://" + e + "/cgi-bin/mmwebwx-bin/webwxverifyuser");
        conf.put("API_webwxfeedback", "https://" + e + "/cgi-bin/mmwebwx-bin/webwxsendfeedback");
        conf.put("API_webwxreport", "https://" + e + "/cgi-bin/mmwebwx-bin/webwxstatreport");
        conf.put("API_webwxsearch", "https://" + e + "/cgi-bin/mmwebwx-bin/webwxsearchcontact");
        conf.put("API_webwxoplog", "https://" + e + "/cgi-bin/mmwebwx-bin/webwxoplog");
        conf.put("API_checkupload", "https://" + e + "/cgi-bin/mmwebwx-bin/webwxcheckupload");
        conf.put("API_webwxrevokemsg", "https://" + e + "/cgi-bin/mmwebwx-bin/webwxrevokemsg");
        conf.put("API_webwxpushloginurl", "https://" + e + "/cgi-bin/mmwebwx-bin/webwxpushloginurl");

        conf.put("CONTACTFLAG_CONTACT", "1");
        conf.put("CONTACTFLAG_CHATCONTACT", "2");
        conf.put("CONTACTFLAG_CHATROOMCONTACT", "4");
        conf.put("CONTACTFLAG_BLACKLISTCONTACT", "8");
        conf.put("CONTACTFLAG_DOMAINCONTACT", "16");
        conf.put("CONTACTFLAG_HIDECONTACT", "32");
        conf.put("CONTACTFLAG_FAVOURCONTACT", "64");
        conf.put("CONTACTFLAG_3RDAPPCONTACT", "128");
        conf.put("CONTACTFLAG_SNSBLACKLISTCONTACT", "256");
        conf.put("CONTACTFLAG_NOTIFYCLOSECONTACT", "512");
        conf.put("CONTACTFLAG_TOPCONTACT", "2048");
        conf.put("MSGTYPE_TEXT", "1");
        conf.put("MSGTYPE_IMAGE", "3");
        conf.put("MSGTYPE_VOICE", "34");
        conf.put("MSGTYPE_VIDEO", "43");
        conf.put("MSGTYPE_MICROVIDEO", "62");
        conf.put("MSGTYPE_EMOTICON", "47");
        conf.put("MSGTYPE_APP", "49");
        conf.put("MSGTYPE_VOIPMSG", "50");
        conf.put("MSGTYPE_VOIPNOTIFY", "52");
        conf.put("MSGTYPE_VOIPINVITE", "53");
        conf.put("MSGTYPE_LOCATION", "48");
        conf.put("MSGTYPE_STATUSNOTIFY", "51");
        conf.put("MSGTYPE_SYSNOTICE", "9999");
        conf.put("MSGTYPE_POSSIBLEFRIEND_MSG", "40");
        conf.put("MSGTYPE_VERIFYMSG", "37");
        conf.put("MSGTYPE_SHARECARD", "42");
        conf.put("MSGTYPE_SYS", "10000");
        conf.put("MSGTYPE_RECALLED", "10002");
        conf.put("APPMSGTYPE_TEXT", "1");
        conf.put("APPMSGTYPE_IMG", "2");
        conf.put("APPMSGTYPE_AUDIO", "3");
        conf.put("APPMSGTYPE_VIDEO", "4");
        conf.put("APPMSGTYPE_URL", "5");
        conf.put("APPMSGTYPE_ATTACH", "6");
        conf.put("APPMSGTYPE_OPEN", "7");
        conf.put("APPMSGTYPE_EMOJI", "8");
        conf.put("APPMSGTYPE_VOICE_REMIND", "9");
        conf.put("APPMSGTYPE_SCAN_GOOD", "10");
        conf.put("APPMSGTYPE_GOOD", "13");
        conf.put("APPMSGTYPE_EMOTION", "15");
        conf.put("APPMSGTYPE_CARD_TICKET", "16");
        conf.put("APPMSGTYPE_REALTIME_SHARE_LOCATION", "17");
        conf.put("APPMSGTYPE_TRANSFERS", "2e3");
        conf.put("APPMSGTYPE_RED_ENVELOPES", "2001");
        conf.put("APPMSGTYPE_READER_TYPE", "100001");
        conf.put("UPLOAD_MEDIA_TYPE_IMAGE", "1");
        conf.put("UPLOAD_MEDIA_TYPE_VIDEO", "2");
        conf.put("UPLOAD_MEDIA_TYPE_AUDIO", "3");
        conf.put("UPLOAD_MEDIA_TYPE_ATTACHMENT", "4");
    }
}
