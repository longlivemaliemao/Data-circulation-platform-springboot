package com.example.demo.Util;

import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;
import java.util.concurrent.TimeUnit;

@Component
public class DownloadTokenUtil {
    private static final String SECRET_KEY = "your-very-strong-secret-key";
    private static final long EXPIRE_MILLIS = TimeUnit.MINUTES.toMillis(2); // 2分钟

    // 构造签名：Base64(username:applicationId:timestamp:signature)
    public static String generateDownloadToken(String username, int applicationId) throws Exception {
        long timestamp = System.currentTimeMillis();
        String data = username + ":" + applicationId + ":" + timestamp;
        String signature = hmacSha256(data, SECRET_KEY);
        String token = data + ":" + signature;
        return Base64.getUrlEncoder().encodeToString(token.getBytes());
    }

    public static ParsedToken validateDownloadToken(String encodedToken) throws Exception {
        String decoded = new String(Base64.getUrlDecoder().decode(encodedToken));
        String[] parts = decoded.split(":");
        if (parts.length != 4) {
            throw new IllegalArgumentException("签名格式错误");
        }

        String username = parts[0];
        int applicationId = Integer.parseInt(parts[1]);
        long timestamp = Long.parseLong(parts[2]);
        String signature = parts[3];

        String data = username + ":" + applicationId + ":" + timestamp;
        String expectedSig = hmacSha256(data, SECRET_KEY);

        if (!expectedSig.equals(signature)) {
            throw new IllegalArgumentException("签名验证失败");
        }

        if (System.currentTimeMillis() - timestamp > EXPIRE_MILLIS) {
            throw new IllegalArgumentException("签名已过期");
        }

        return new ParsedToken(username, applicationId);
    }

    private static String hmacSha256(String data, String secret) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA256");
        SecretKeySpec secretKeySpec = new SecretKeySpec(secret.getBytes(), "HmacSHA256");
        mac.init(secretKeySpec);
        byte[] bytes = mac.doFinal(data.getBytes());
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    public static class ParsedToken {
        public final String username;
        public final int applicationId;

        public ParsedToken(String username, int applicationId) {
            this.username = username;
            this.applicationId = applicationId;
        }
    }
}

