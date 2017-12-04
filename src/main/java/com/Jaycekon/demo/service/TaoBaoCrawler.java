package com.Jaycekon.demo.service;

import com.Jaycekon.demo.util.FetchUtils;
import com.Jaycekon.demo.model.HttpRequestData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author <a href="mailto:weijie_huang@sui.com"> weijie_huang </a>
 * 2017/12/4 19:09
 */
public class TaoBaoCrawler {

    private static Logger logger = LoggerFactory.getLogger(TaoBaoCrawler.class);

    public static void main(String[] args) {
        HttpRequestData data = HttpRequestData.creatHttpRequestData();
        TaoBaoCrawler.crawlIndex(data);
    }


    public static void crawlIndex(HttpRequestData data) {

        try {
            String home = FetchUtils.get(data, "https://just4u.taobao.com/shop/view_shop.htm?spm=a230r.1.14.184.218cda2bVndmmJ&user_number_id=402991741");
            logger.info(home);

        } catch (Exception e) {
            logger.error("获取店铺首页异常！{}", e.getMessage(), e);
        }


    }

}
