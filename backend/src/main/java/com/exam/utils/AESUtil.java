package com.exam.utils;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * AES加密工具类
 * 用于加密敏感信息（身份证号、手机号等）
 *
 * @author system
 * @since 2024-10-17
 */
public class AESUtil {

    /**
     * 加密密钥（16字节）
     * 注意：生产环境应从配置文件读取
     */
    private static final String SECRET_KEY = "ExamSystem2025!!";

    private static final String ALGORITHM = "AES";

    /**
     * AES加密
     *
     * @param content 待加密内容
     * @return 加密后的Base64字符串
     */
    public static String encrypt(String content) {
        if (content == null || content.isEmpty()) {
            return null;
        }

        try {
            SecretKeySpec keySpec = new SecretKeySpec(SECRET_KEY.getBytes(StandardCharsets.UTF_8), ALGORITHM);
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, keySpec);

            byte[] encrypted = cipher.doFinal(content.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(encrypted);
        } catch (Exception e) {
            throw new RuntimeException("AES加密失败", e);
        }
    }

    /**
     * AES解密
     *
     * @param encryptedContent 加密后的Base64字符串
     * @return 解密后的原始内容
     */
    public static String decrypt(String encryptedContent) {
        if (encryptedContent == null || encryptedContent.isEmpty()) {
            return null;
        }

        try {
            SecretKeySpec keySpec = new SecretKeySpec(SECRET_KEY.getBytes(StandardCharsets.UTF_8), ALGORITHM);
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, keySpec);

            byte[] decoded = Base64.getDecoder().decode(encryptedContent);
            byte[] decrypted = cipher.doFinal(decoded);
            return new String(decrypted, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException("AES解密失败", e);
        }
    }

    /**
     * 手机号脱敏显示
     *
     * @param phone 手机号
     * @return 脱敏后的手机号（138****8000）
     */
    public static String maskPhone(String phone) {
        if (phone == null || phone.length() != 11) {
            return phone;
        }
        return phone.substring(0, 3) + "****" + phone.substring(7);
    }

    /**
     * 身份证号脱敏显示
     *
     * @param idCard 身份证号
     * @return 脱敏后的身份证号（110***********1234）
     */
    public static String maskIdCard(String idCard) {
        if (idCard == null || idCard.length() < 8) {
            return idCard;
        }
        return idCard.substring(0, 3) + "***********" + idCard.substring(idCard.length() - 4);
    }
}
