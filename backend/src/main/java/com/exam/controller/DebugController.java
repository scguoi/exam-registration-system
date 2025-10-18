package com.exam.controller;

import com.exam.common.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 调试控制器 - 仅用于开发和测试
 * 生产环境请删除此控制器
 */
@Slf4j
@RestController
@RequestMapping("/v1/debug")
public class DebugController {

    @Autowired
    private PasswordEncoder passwordEncoder;

    /**
     * 生成 BCrypt 密码哈希
     *
     * @param password 明文密码
     * @return 密码哈希
     */
    @PostMapping("/hash-password")
    public Result hashPassword(@RequestParam String password) {
        String hash = passwordEncoder.encode(password);
        Map<String, Object> result = new HashMap<>();
        result.put("password", password);
        result.put("hash", hash);

        // 验证生成的哈希
        boolean matches = passwordEncoder.matches(password, hash);
        result.put("verified", matches);

        log.info("生成密码哈希 - 密码长度: {}, 哈希: {}", password.length(), hash);

        return Result.success(result);
    }

    /**
     * 验证密码与哈希是否匹配
     *
     * @param password 明文密码
     * @param hash BCrypt 哈希
     * @return 验证结果
     */
    @PostMapping("/verify-password")
    public Result verifyPassword(@RequestParam String password, @RequestParam String hash) {
        boolean matches = passwordEncoder.matches(password, hash);

        Map<String, Object> result = new HashMap<>();
        result.put("password", password);
        result.put("hash", hash);
        result.put("matches", matches);

        log.info("验证密码 - 密码: {}, 匹配: {}", password, matches);

        return Result.success(result);
    }

    /**
     * 批量测试密码哈希
     *
     * @return 测试结果
     */
    @GetMapping("/test-passwords")
    public Result testPasswords() {
        Map<String, Object> results = new HashMap<>();

        // 要测试的密码
        String[] passwords = {"password", "admin123", "123456"};

        // 数据库中的哈希
        Map<String, String> hashes = new HashMap<>();
        hashes.put("原始 admin 密码", "$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi");
        hashes.put("当前 admin 密码", "$2a$10$YTR9b1V8emCUy8WkpT5.QuZLSqnGx3rToQ0p8KMmr0x1MmxAODQ8u");
        hashes.put("测试考生密码", "$2a$10$EblZqNptyYvcLm/VwDCVAuBjzZOI7khzdyGPBr/w0wTRaI5p2S5K.");

        // 生成新密码哈希
        Map<String, String> newHashes = new HashMap<>();
        for (String password : passwords) {
            String hash = passwordEncoder.encode(password);
            newHashes.put(password, hash);
        }
        results.put("新生成的哈希", newHashes);

        // 验证现有哈希
        Map<String, Map<String, Boolean>> verification = new HashMap<>();
        for (Map.Entry<String, String> entry : hashes.entrySet()) {
            Map<String, Boolean> passwordMatches = new HashMap<>();
            for (String password : passwords) {
                boolean matches = passwordEncoder.matches(password, entry.getValue());
                passwordMatches.put(password, matches);
            }
            verification.put(entry.getKey(), passwordMatches);
        }
        results.put("现有哈希验证", verification);

        return Result.success(results);
    }
}
