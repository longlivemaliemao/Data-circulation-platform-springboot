package com.example.demo.Service;

import com.example.demo.Mapper.UserMapper;
import com.example.demo.Model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.crypto.*;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.spec.ECGenParameterSpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Arrays;
import java.util.Base64;
import java.util.Optional;

@Service
public class ECDHService {

    @Autowired
    private UserMapper userMapper;


    /**
     * 生成一个椭圆曲线 Diffie-Hellman (ECDH) 密钥对，用于密钥交换。
     * 采用 P-256 曲线，提供高安全性。
     *
     * @return 生成的 ECDH 密钥对
     * @throws NoSuchAlgorithmException 如果无法找到 "EC" 算法时抛出
     */
    public KeyPair generateKeyPair() throws NoSuchAlgorithmException, InvalidAlgorithmParameterException {
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("EC");
        keyPairGenerator.initialize(new ECGenParameterSpec("secp256r1"));  // 使用 P-256 椭圆曲线
        return keyPairGenerator.generateKeyPair();
    }

    /**
     * 根据私钥和对方的公钥生成共享密钥。
     * 共享密钥是通过 ECDH 算法计算得出的。
     *
     * @param privateKey 本地的私钥
     * @param publicKey  对方的公钥
     * @return 生成的共享密钥（字节数组形式）
     * @throws Exception 如果密钥初始化或密钥阶段出现问题时抛出
     */
    public byte[] generateSharedSecret(PrivateKey privateKey, PublicKey publicKey) throws Exception {
        KeyAgreement keyAgreement = KeyAgreement.getInstance("ECDH");
        keyAgreement.init(privateKey);  // 初始化 KeyAgreement，使用本地的私钥
        keyAgreement.doPhase(publicKey, true);  // 执行密钥交换，使用对方的公钥
        return keyAgreement.generateSecret();  // 生成共享密钥
    }

    /**
     * 使用共享密钥加密数据。
     * 采用 AES/CTR/NoPadding 算法，CTR 模式提供了加密功能。
     *
     * @param data 要加密的原始数据
     * @return 加密后的数据，Base64 编码，包含加密后的数据和 IV
     * @throws Exception 如果加密过程出现问题时抛出
     */
    public String encrypt(byte[] data, byte[] sharedSecret) {
        try {
            // 检查共享密钥是否为 null 或空
            if (sharedSecret == null || sharedSecret.length == 0) {
                throw new IllegalArgumentException("Shared secret is null or empty");
            }

            // 创建 AES 密钥
            SecretKey secretKey = new SecretKeySpec(sharedSecret, "AES");

            // 使用 AES/CTR/NoPadding 模式
            Cipher cipher = Cipher.getInstance("AES/CTR/NoPadding");

            // 生成随机的 IV，长度为 16 字节
            byte[] iv = new byte[16];
            SecureRandom secureRandom = new SecureRandom();
            secureRandom.nextBytes(iv);  // 生成随机的 IV

            // 初始化 Cipher
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, new IvParameterSpec(iv));  // 使用 IV 初始化

            // 加密数据
            byte[] encryptedData = cipher.doFinal(data);

            // 将 IV 和加密后的数据组合在一起
            byte[] encryptedDataWithIv = new byte[iv.length + encryptedData.length];
            System.arraycopy(iv, 0, encryptedDataWithIv, 0, iv.length);
            System.arraycopy(encryptedData, 0, encryptedDataWithIv, iv.length, encryptedData.length);

            // 返回 Base64 编码字符串
            return Base64.getEncoder().encodeToString(encryptedDataWithIv);
        } catch (Exception e) {
            throw new RuntimeException("Unexpected error during encryption: " + e.getMessage(), e);
        }
    }


    // 将字节数组转换为十六进制字符串
    private static String bytesToHex(byte[] bytes) {
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

    /**
     * 使用共享密钥解密数据。
     * 采用 AES/CTR/NoPadding 算法，CTR 模式提供了解密功能。
     *
     * @param encryptedData 加密后的数据，Base64 编码，包含加密的数据和 IV
     * @param sharedSecret 用于解密的共享密钥
     * @return 解密后的原始数据
     * @throws Exception 如果解密过程出现问题时抛出
     */
    public String decrypt(String encryptedData, byte[] sharedSecret) throws Exception {
        try {
            byte[] encryptedDataWithIv = Base64.getDecoder().decode(encryptedData);
            byte[] iv = new byte[16];
            System.arraycopy(encryptedDataWithIv, 0, iv, 0, iv.length);

            byte[] encryptedBytes = new byte[encryptedDataWithIv.length - iv.length];
            System.arraycopy(encryptedDataWithIv, iv.length, encryptedBytes, 0, encryptedBytes.length);

            SecretKey secretKey = new SecretKeySpec(sharedSecret, "AES");

            // 使用 AES/CTR/NoPadding 模式
            Cipher cipher = Cipher.getInstance("AES/CTR/NoPadding");
            cipher.init(Cipher.DECRYPT_MODE, secretKey, new IvParameterSpec(iv));

            // 解密数据
            byte[] decryptedData = cipher.doFinal(encryptedBytes);
            return new String(decryptedData, StandardCharsets.UTF_8);
        } catch (Exception e) {
            System.err.println("Decryption failed: " + e.getMessage());
            throw new Exception("Decryption failed", e);
        }
    }


    /**
     * 使用共享密钥解密文件的二进制数据。
     * 采用 AES/CTR/NoPadding 算法，CTR 模式提供了解密功能。
     *
     * @param encryptedDataWithIv 加密后的二进制数据，包含加密的数据和 IV
     * @param sharedSecret 用于解密的共享密钥
     * @return 解密后的原始数据（字节数组）
     * @throws Exception 如果解密过程出现问题时抛出
     */
    public byte[] decryptFile(byte[] encryptedDataWithIv, byte[] sharedSecret) throws Exception {
        try {
            int offset = 4;  // 假设有长度前缀

            byte[] iv = new byte[16];  // IV 长度为 16 字节
            System.arraycopy(encryptedDataWithIv, offset, iv, 0, iv.length);
            offset += iv.length;

            byte[] encryptedBytes = new byte[encryptedDataWithIv.length - offset];
            System.arraycopy(encryptedDataWithIv, offset, encryptedBytes, 0, encryptedBytes.length);

            SecretKey secretKey = new SecretKeySpec(sharedSecret, "AES");
            Cipher cipher = Cipher.getInstance("AES/CTR/NoPadding");
            cipher.init(Cipher.DECRYPT_MODE, secretKey, new IvParameterSpec(iv));

            return cipher.doFinal(encryptedBytes);
        } catch (Exception e) {
            System.err.println("Decryption failed: " + e.getMessage());
            throw new Exception("Decryption failed", e);
        }
    }

    /**
     * 将 Base64 编码的公钥字符串解码为 PublicKey 对象。
     *
     * @param publicKeyStr Base64 编码的公钥字符串
     * @return 解码后的 PublicKey 对象
     * @throws Exception 如果解码或密钥生成过程中出现问题时抛出
     */
    public PublicKey decodePublicKey(String publicKeyStr) throws Exception {
        // 将 Base64 编码的公钥字符串解码为字节数组
        byte[] publicKeyBytes = Base64.getDecoder().decode(publicKeyStr);

        // 使用 X509 编码规范生成公钥
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(publicKeyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance("EC");
        return keyFactory.generatePublic(keySpec);  // 生成 PublicKey 对象并返回
    }
}
