package com.jaycekon.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;
import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

/*******************************************
 * 最简单的AES加密方式，不需要随机向量iv，也不需要cbc 和padding
 *
 *******************************************/
public class SimpleAES {

    private static final String ALGORITHM = "AES";

    private static final String AESPASS = "&*($HJDGH4867%&T345386754OHYOH*^(ughiuR5fu&f&$KHAOS$&^%";
    private static final String CHAR_SET = "UTF-8";
    private static Logger logger = LoggerFactory.getLogger(SimpleAES.class);

    private SimpleAES() {
    }

    /**
     * the password for decrypt read from config file ,config file key is
     * AES.passowrd
     */
    public static String encrypt(String plainText) {
        return encrypt(plainText, AESPASS);
    }

    /**
     * 加密
     *
     * @param plainText 明文
     * @param password  密码
     * @return 加密串
     */
    public static String encrypt(String plainText, String password) {
        if (isEmpty(plainText)) {
            return plainText;
        }
        try {
            return SecurityUtil.toHex(encrypt(plainText.getBytes(CHAR_SET), password));
        } catch (UnsupportedEncodingException | NoSuchPaddingException | NoSuchAlgorithmException | InvalidKeyException | BadPaddingException | IllegalBlockSizeException e) {
            logger.error("error in encrypt:", e);
        }
        return null;
    }

    /**
     * the password for decrypt read from config file ,config file key is
     * AES.passowrd
     */
    public static String decrypt(String cipherText) {
        if (isEmpty(cipherText)) {
            return cipherText;
        }
        return decrypt(cipherText, AESPASS);
    }

    /**
     * 解密 以String密文输入,String明文输出
     */
    public static String decrypt(String cipherText, String password) {
        try {
            byte[] bytes = decrypt(SecurityUtil.hexTobytes(cipherText), password);
            return new String(bytes, "UTF-8");
        } catch (UnsupportedEncodingException | NoSuchPaddingException | NoSuchAlgorithmException | InvalidKeyException | BadPaddingException | IllegalBlockSizeException e) {
            logger.error("error in decrypt:", e);
        }
        return null;
    }

    /**
     * 加密以byte[]明文输入,byte[]密文输出
     */
    public static byte[] encrypt(byte[] byteS, String pwd) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException, UnsupportedEncodingException {

        byte[] byteFina;
        Cipher cipher;
        cipher = Cipher.getInstance(ALGORITHM);
        SecretKeySpec keySpec = new SecretKeySpec(getKey(pwd), "AES");
        cipher.init(Cipher.ENCRYPT_MODE, keySpec);
        byteFina = cipher.doFinal(byteS);
        return byteFina;
    }

    /**
     * 解密以byte[]密文输入,以byte[]明文输出
     */
    public static byte[] decrypt(byte[] byteD, String pwd) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, UnsupportedEncodingException, BadPaddingException, IllegalBlockSizeException {
        byte[] byteFina;
        Cipher cipher = Cipher.getInstance(ALGORITHM);

        SecretKeySpec keySpec = new SecretKeySpec(getKey(pwd), "AES");

        cipher.init(Cipher.DECRYPT_MODE, keySpec);

        byteFina = cipher.doFinal(byteD);
        return byteFina;
    }

    private static byte[] getKey(String password) throws UnsupportedEncodingException {
        // 使用256位密码
        StringBuilder pwd = new StringBuilder("");
        if (password.length() >= 16) {
            pwd.append(password.substring(0, 16));
        } else if (password.length() < 16) {
            pwd.append(password);
            int count = 16 - password.length();
            for (int i = 0; i < count; i++) {
                pwd.append("0");
            }
        }

        return pwd.toString().getBytes(CHAR_SET);
    }

    public static boolean isEmpty(String str) {
        return str == null || str.length() == 0;
    }
}
