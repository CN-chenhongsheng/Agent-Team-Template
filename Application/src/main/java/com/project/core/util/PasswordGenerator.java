package com.project.core.util;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * 密码生成工具
 * 用于生成 BCrypt 加密密码，方便测试和初始化
 *
 * @author 陈鸿昇
 * @date 2026-01-15
 */
public class PasswordGenerator {

    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

        // 测试密码
        String[] passwords = {"123456", "654321", "111111"};

        System.out.println("BCrypt 密码生成工具");
        System.out.println("=====================");

        for (String password : passwords) {
            String encoded = encoder.encode(password);
            System.out.println("明文密码: " + password);
            System.out.println("加密密码: " + encoded);
            System.out.println("验证结果: " + encoder.matches(password, encoded));
            System.out.println("---------------------");
        }

        // 自定义密码生成
        if (args.length > 0) {
            System.out.println("\n自定义密码生成");
            for (String arg : args) {
                System.out.println("明文: " + arg + " => 加密: " + encoder.encode(arg));
            }
        }
    }
}
