package com.jaycekon.util;

import com.jaycekon.model.BaseSession;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;


public class Util {
    public static final String KEY_RESULT = "result";
    private static final Logger logger = LoggerFactory.getLogger(Util.class);

    private static String fileStore = "/var/attachDir/onlineLoan/";

    private Util() {
    }

    public static void setFileStore(String fileStore) {
        Util.fileStore = fileStore;
    }


    /**
     * 抽取字符串中的数字和小数点，返回高精度数
     *
     * @param src 字符串
     * @return 如果字符串没有数字则返回0
     */
    public static BigDecimal extractNumber(String src) {
        if (StringUtils.isEmpty(src)) {
            return new BigDecimal(0);
        }
        char[] array = src.toCharArray();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < array.length; i++) {
            if ((array[i] >= 48 && array[i] <= 58) || array[i] == 46) {
                sb.append(array[i]);
            }
        }
        BigDecimal result;
        if (sb.length() == 0) {
            result = new BigDecimal(0);
        } else {
            result = new BigDecimal(sb.toString());
        }
        return result;
    }

    /**
     * 字符串截取
     *
     * @param string 字符串
     * @param begin  开始字符串
     * @param end    结尾字符串
     * @return 截取的字符串
     */
    public static String subString(String string, String begin, String end) {
        if (string != null && !"".equals(string)) {
            int index = string.indexOf(begin);
            if (index == -1)
                return "";
            int beginIndex = index + begin.length();
            int endIndex = string.indexOf(end, beginIndex);
            if (beginIndex > endIndex)
                return "";
            else return string.substring(beginIndex, endIndex).trim();
        }
        return "";
    }

//
//    /**
//     * 众联打码接口
//     *
//     * @param imgBytes
//     * @return 验证码
//     */
//    public static String recognizeVerifyCode(byte[] imgBytes) {
//        String result = "";
//        try {
//            String fileName = UUID.randomUUID().toString().replace("-", "") + ".jpg";
//            result = HttpDama.manualVerify(imgBytes, fileName);
//            logger.info("打码上传返回结果：{}", result);
//        } catch (Exception e) {
//            logger.error("打码失败" + e.getMessage(), e);
//        }
//
//        return result;
//    }

    public static boolean isPhoneNum(String value) {
        boolean result = true;
        if (value.length() == 11)
            for (char c : value.toCharArray()) {
                if (!Character.isDigit(c)) {
                    result = false;
                    break;
                }
            }
        return result;
    }


    public static boolean isEmpty(String value) {
        return value == null || "".equals(value) || value.length() == 0;
    }

    public static boolean isBlank(CharSequence cs) {
        int strLen;
        if (cs != null && (strLen = cs.length()) != 0) {
            for (int i = 0; i < strLen; ++i) {
                if (!Character.isWhitespace(cs.charAt(i))) {
                    return false;
                }
            }

            return true;
        } else {
            return true;
        }
    }


    /**
     * 保存文件
     *
     * @param session 账号
     * @param fName   文件名称 为空时，使用手机号base作为文件名
     * @param content 保存内容
     * @return 返回文件保存路径
     */
    public static String saveFile(BaseSession session, String fName, String bankCode, String content) {
        String fileName = StringUtils.isEmpty(fName) ? SimpleAES.encrypt(session.getAccount()) : fName;
        String path = getFileStorePath(fileStore, session.getAccount(), bankCode, session.getSessionId()) + File.separator + fileName;
        try {
            FileUtils.writeStringToFile(new File(path), content, "utf-8");
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }
        return path;
    }

    /**
     * 流方式-保存文件
     *
     * @param session 账号
     * @param fName   文件名称 为空时，使用手机号base作为文件名
     * @param bytes   文件流
     * @return 返回文件保存路径
     */
    public static String saveFile(BaseSession session, String fName, String bankCode, byte[] bytes) {
        String fileName = StringUtils.isEmpty(fName) ? SimpleAES.encrypt(session.getAccount()) : fName;
        String path = getFileStorePath(fileStore, session.getAccount(), bankCode, session.getSessionId()) + File.separator + fileName;
        try {
            FileUtils.writeByteArrayToFile(new File(path), bytes);
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }
        return path;
    }

    /**
     * 创建保存文件的多级目录
     * /var/attachDir/onlineLoan/{account}/{sessionId}
     *
     * @param account   账号
     * @param sessionId sessionId
     * @return 文件目录
     */
    public static String getFileStorePath(String dir, String account, String bankCode, String sessionId) {
        if (dir == null || bankCode == null) {
            return fileStore;
        }
        String platform = bankCode.toLowerCase();
        StringBuilder sb = new StringBuilder();
        sb.append(dir);
        if (!dir.endsWith("/")) {
            sb.append("/");
        }
        sb.append(platform).append("/").append("/").append(account).append("/").append(sessionId);
        return sb.toString();
    }



    public static BigDecimal toBigDecimal(String amount) {
        return str2BigDecimal(amount);
    }

    /**
     * 字符串转大数
     *
     * @param text 字符串不需要去空格
     * @return 如果字符串不是数字或者是空则返回空
     */
    public static BigDecimal str2BigDecimal(String text) {
        if (StringUtils.isEmpty(text))
            return null;
        char[] chars = text.toCharArray();
        StringBuilder builder = new StringBuilder();
        for (char val : chars) {
            if (Character.isDigit(val) || val == '.') {
                builder.append(val);
            }
        }
        BigDecimal result = null;
        if (builder.length() != 0) {
            result = new BigDecimal(builder.toString());
        }
        return result;
    }

    /**
     * 消除掉字符串中的字符，只保留数字
     *
     * @param text 不需要去空格
     * @return 不合法则返回空
     */
    public static Integer extractDigit(String text) {
        if (StringUtils.isEmpty(text))
            return 0;
        char[] arr = text.toCharArray();
        StringBuilder builder = new StringBuilder();
        for (char val : arr) {
            if (Character.isDigit(val)) {
                builder.append(val);
            }
        }
        return builder.length() == 0 ? 0 : Integer.valueOf(builder.toString());
    }

    /**
     * @param status 状态
     * @return 0为已还清，1为未还
     */
    public static int convertStat(String status) {
        if (status.contains("已结")
                || status.contains("已还")
                || status.contains("按期还")
                || status.contains("准时还")
                || status.contains("逾期还")
                || status.contains("提前还")) {
            return 0;
        } else return 1;
    }

    /**
     * 获取mac地址
     *
     * @param loginName 登录账号
     */
    public static String generateMac(String loginName) {
        String chars = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        loginName = StringUtils.substring(StringUtils.rightPad(loginName.toUpperCase(), 12, 'A'), 0, 12);
        char[] cs = loginName.toCharArray();
        StringBuilder mac = new StringBuilder();
        for (int i = 0; i < cs.length; i++) {
            char c = cs[i];
            if (chars.indexOf(c) >= chars.indexOf('F')) {
                c = chars.charAt(chars.indexOf(c) - chars.indexOf('F'));
            }
            mac.append(c);
            if ((i + 1) % 2 == 0 && i < cs.length - 1) {
                mac.append(':');
            }
        }
        return mac.toString();
    }
}