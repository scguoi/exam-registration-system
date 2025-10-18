package com.exam.util;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * BCrypt 密码哈希生成器
 * 用于生成和验证密码哈希
 */
public class PasswordHashGenerator {

    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

        // 要测试的密码
        String[] passwords = {"password", "admin123", "123456"};

        System.out.println("=".repeat(80));
        System.out.println("BCrypt 密码哈希生成器");
        System.out.println("=".repeat(80));

        // 生成新的哈希
        for (String password : passwords) {
            String hash = encoder.encode(password);
            System.out.println("\n密码: " + password);
            System.out.println("哈希: " + hash);

            // 验证生成的哈希
            boolean matches = encoder.matches(password, hash);
            System.out.println("验证: " + (matches ? "✓ 通过" : "✗ 失败"));
        }

        System.out.println("\n" + "=".repeat(80));
        System.out.println("验证现有哈希");
        System.out.println("=".repeat(80));

        // 验证现有的哈希
        String existingHash1 = "$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi";
        String existingHash2 = "$2a$10$YTR9b1V8emCUy8WkpT5.QuZLSqnGx3rToQ0p8KMmr0x1MmxAODQ8u";
        String existingHash3 = "$2a$10$EblZqNptyYvcLm/VwDCVAuBjzZOI7khzdyGPBr/w0wTRaI5p2S5K.";

        System.out.println("\n哈希1 (原始 admin 密码): " + existingHash1);
        for (String password : passwords) {
            boolean matches = encoder.matches(password, existingHash1);
            System.out.println("  - " + password + ": " + (matches ? "✓ 匹配" : "✗ 不匹配"));
        }

        System.out.println("\n哈希2 (当前 admin 密码): " + existingHash2);
        for (String password : passwords) {
            boolean matches = encoder.matches(password, existingHash2);
            System.out.println("  - " + password + ": " + (matches ? "✓ 匹配" : "✗ 不匹配"));
        }

        System.out.println("\n哈希3 (测试考生密码): " + existingHash3);
        for (String password : passwords) {
            boolean matches = encoder.matches(password, existingHash3);
            System.out.println("  - " + password + ": " + (matches ? "✓ 匹配" : "✗ 不匹配"));
        }

        System.out.println("\n" + "=".repeat(80));
    }
}
