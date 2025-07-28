package com.example.demo.Filter;

import com.example.demo.Service.ECDHService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.Collections;
import java.util.Map;

@Component
public class ApiSignatureFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(ApiSignatureFilter.class);
    private static final long TIMESTAMP_VALIDITY_MS = 5 * 60 * 1000;

    @Autowired
    private ECDHService ecdhService;
    @Autowired
    private ObjectMapper objectMapper;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        boolean isPostOrPut = "POST".equalsIgnoreCase(request.getMethod()) || "PUT".equalsIgnoreCase(request.getMethod());
        // 只处理 application/json 类型的 POST/PUT 请求
        String contentType = request.getContentType();
        if (!isPostOrPut || contentType == null || !contentType.contains("application/json")
                || request.getRequestURI().equals("/exchange-keys")) {
            filterChain.doFilter(request, response);
            return;
        }

        CachedBodyHttpServletRequest requestToUse = new CachedBodyHttpServletRequest(request);

        byte[] bodyBytes = requestToUse.getBody();
        if (bodyBytes == null || bodyBytes.length == 0) {
            filterChain.doFilter(requestToUse, response);
            return;
        }

        try {
            // 1. 解析请求体为可变的JSON节点树
            ObjectNode topLevelPayloadNode = (ObjectNode) objectMapper.readTree(bodyBytes);

            // 2. 检查是否存在 sign 和 timestamp 字段
            if (!topLevelPayloadNode.has("sign") || !topLevelPayloadNode.has("timestamp")) {
                filterChain.doFilter(requestToUse, response);
                return;
            }

            logger.info("检测到签名请求，开始验证...");

            // 3. 提取 sign 和 timestamp
            String sign = topLevelPayloadNode.get("sign").asText();
            long timestamp = topLevelPayloadNode.get("timestamp").asLong();

            // 4. 移除 sign 和 timestamp 字段，获取原始数据
            topLevelPayloadNode.remove("sign");
            //topLevelPayloadNode.remove("timestamp");

            // 5. 将清理后的数据转换为 Map 和 JSON 字符串
            Map<String, Object> originalDataMap = objectMapper.convertValue(topLevelPayloadNode, new TypeReference<Map<String, Object>>() {});
            String originalDataJson = objectMapper.writeValueAsString(originalDataMap);

            // 6. 验证时间戳
            if (System.currentTimeMillis() - timestamp > TIMESTAMP_VALIDITY_MS) {
                throw new SecurityException("请求时间戳已过期");
            }

            // 7. 获取共享密钥并解密签名
            HttpSession session = requestToUse.getSession(false);
            if (session == null) {
                throw new SecurityException("签名验证失败：会话不存在。");
            }
            byte[] sharedSecret = (byte[]) session.getAttribute("sharedSecret");
            if (sharedSecret == null) {
                throw new SecurityException("签名验证失败：共享密钥未找到。");
            }

            String decryptedPayload = ecdhService.decrypt(sign, sharedSecret);

            // 8. 比较签名
            if (!originalDataJson.equals(decryptedPayload)) {
                logger.error("签名不匹配！原始数据: {}, 解密后数据: {}", originalDataJson, decryptedPayload);
                throw new SecurityException("签名验证失败");
            }

            logger.info("签名和时间戳验证成功！");
            logger.info("原始数据: {}, 解密后数据: {}", originalDataJson, decryptedPayload);

            // 9. 创建新请求，去除 sign 和 timestamp 后放行
            topLevelPayloadNode.remove("timestamp");
            originalDataMap = objectMapper.convertValue(topLevelPayloadNode, new TypeReference<Map<String, Object>>() {});
            byte[] newBody = objectMapper.writeValueAsBytes(originalDataMap);
            HttpServletRequest newRequest = new CachedBodyHttpServletRequest(requestToUse, newBody);
            filterChain.doFilter(newRequest, response);

        } catch (Exception e) {
            logger.error("API签名验证失败: {}", e.getMessage(), e);
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json;charset=UTF-8");
            Map<String, String> errorResponse = Collections.singletonMap("error", "无效的API签名或时间戳");
            response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
        }
    }
}