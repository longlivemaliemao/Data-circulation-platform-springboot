package com.example.demo.Filter;

import javax.servlet.ReadListener;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import java.io.*;

public class CachedBodyHttpServletRequest extends HttpServletRequestWrapper {

    private final byte[] cachedBody;

    /**
     * 构造函数，用于首次包装原始请求并缓存其 Body
     * @param request 原始 HttpServletRequest
     * @throws IOException 如果读取输入流失败
     */
    public CachedBodyHttpServletRequest(HttpServletRequest request) throws IOException {
        super(request);
        this.cachedBody = readBody(request.getInputStream());
    }

    /**
     * 构造函数，用于在验证后，用新的 Body 重新包装请求
     * @param request 原始 HttpServletRequest
     * @param newBody 新的请求体内容
     */
    public CachedBodyHttpServletRequest(HttpServletRequest request, byte[] newBody) {
        super(request);
        this.cachedBody = newBody;
    }

    @Override
    public ServletInputStream getInputStream() throws IOException {
        return new CachedBodyServletInputStream(this.cachedBody);
    }

    @Override
    public BufferedReader getReader() throws IOException {
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(this.cachedBody);
        String encoding = getCharacterEncoding();
        if (encoding == null) {
            encoding = "UTF-8";
        }
        return new BufferedReader(new InputStreamReader(byteArrayInputStream, encoding));
    }

    /**
     * 获取缓存的请求体
     * @return 请求体字节数组
     */
    public byte[] getBody() {
        return this.cachedBody;
    }

    /**
     * 新增的辅助方法：用于从 InputStream 读取所有字节，兼容 Java 8
     * @param inputStream 输入流
     * @return 包含流所有内容的字节数组
     * @throws IOException
     */
    private byte[] readBody(InputStream inputStream) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] buffer = new byte[4096]; // 4KB 缓冲区
        int bytesRead;
        while ((bytesRead = inputStream.read(buffer)) != -1) {
            baos.write(buffer, 0, bytesRead);
        }
        return baos.toByteArray();
    }


    private static class CachedBodyServletInputStream extends ServletInputStream {

        private final ByteArrayInputStream cachedBodyInputStream;

        public CachedBodyServletInputStream(byte[] cachedBody) {
            this.cachedBodyInputStream = new ByteArrayInputStream(cachedBody);
        }

        @Override
        public boolean isFinished() {
            return cachedBodyInputStream.available() == 0;
        }

        @Override
        public boolean isReady() {
            return true;
        }

        @Override
        public void setReadListener(ReadListener readListener) {
            throw new UnsupportedOperationException();
        }

        @Override
        public int read() throws IOException {
            return cachedBodyInputStream.read();
        }
    }
}