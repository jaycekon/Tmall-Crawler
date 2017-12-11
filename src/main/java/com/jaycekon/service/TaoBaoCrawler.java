package com.jaycekon.service;

import com.jaycekon.model.BaseSession;
import com.jaycekon.util.FetchUtils;
import com.jaycekon.model.HttpRequestData;
import com.jaycekon.util.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author <a href="mailto:weijie_huang@sui.com"> weijie_huang </a>
 * 2017/12/4 19:09
 */
public class TaoBaoCrawler {

    private static Logger logger = LoggerFactory.getLogger(TaoBaoCrawler.class);

    public static void main(String[] args) {
        BaseSession session = BaseSession.create();
        session.setAccount("xiangshui");
        TaoBaoCrawler.crawlIndex(session);
    }


    public static void crawlIndex(BaseSession session) {
        HttpRequestData data =session.getHttpRequestData();
        try {
            String home = FetchUtils.get(data, "https://just4u.taobao.com/shop/view_shop.htm?spm=a230r.1.14.184.218cda2bVndmmJ&user_number_id=402991741");
            logger.info(home);

            Util.saveFile(session,"xiangshui_home","xiangshui",home);


        } catch (Exception e) {
            logger.error("获取店铺首页异常！{}", e.getMessage(), e);
        }


    }

}
