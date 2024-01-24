package com.fm.fmplayer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Md5 {
    public static String encoder(String message) {
        try {
            // 创建MD5消息摘要对象
            MessageDigest md = MessageDigest.getInstance("MD5");

            // 计算消息的摘要
            byte[] digest = md.digest(message.getBytes());

            // 将摘要转换为十六进制字符串
            String hexString = bytesToHex(digest);

            return hexString;
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return  null;
    }

    public static String bytesToHex(byte[] bytes) {
        StringBuilder hexString = new StringBuilder();
        for (byte b : bytes) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString();
    }
}
