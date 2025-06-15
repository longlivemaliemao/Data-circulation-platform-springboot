package com.example.demo.Controller;

import com.example.demo.Mapper.FileMapper;
import com.example.demo.Model.APIResponse;
import com.example.demo.Model.File;
import com.example.demo.Model.Handle;
import com.example.demo.Service.ECDHService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpServletResponse;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.io.*;

import java.nio.file.*;


import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

@RestController
public class FileController {

    @Value("${file.storage.directory}")
    private String DIRECTORY_PATH;

    @Autowired
    private ECDHService ecdhService;

    @Autowired
    private FileMapper fileMapper;

    @GetMapping("/filesName")
    @PreAuthorize("hasAuthority('数据提供方')")
    public APIResponse<List<String>> getFileNamesByCreatorName(@RequestParam("creatorName") String creatorName) {
        try {
            List<String> fileNames = fileMapper.findFileByCreatorName(creatorName);
            return APIResponse.success(fileNames);
        } catch (Exception e) {
            e.printStackTrace();
            return APIResponse.error(500, "Error retrieving file names: " + e.getMessage());
        }
    }

    @GetMapping("/files")
    @PreAuthorize("hasAuthority('数据提供方')")
    public APIResponse<List<File>> getFilesByCreatorName(@RequestParam("creatorName") String creatorName) {
        try {
            List<File> fileNames = fileMapper.findFilesByCreatorName(creatorName);
            return APIResponse.success(fileNames);
        } catch (Exception e) {
            e.printStackTrace();
            return APIResponse.error(500, "Error retrieving file names: " + e.getMessage());
        }
    }

    @GetMapping("/start-upload")
    @PreAuthorize("hasAuthority('数据提供方')")
    public APIResponse<String> startUpload(HttpSession session) {
        // 生成唯一的文件标识符
        String fileId = UUID.randomUUID().toString();
        // 返回给前端，前端每次上传块时都携带该 fileId
        return APIResponse.success(fileId);
    }

    @PostMapping("/upload-chunk")
    @PreAuthorize("hasAuthority('数据提供方')")
    public APIResponse<String> uploadChunk(
            @RequestParam("chunk") MultipartFile chunk,
            @RequestParam("chunkIndex") int chunkIndex,
            @RequestParam("totalChunks") int totalChunks,
            @RequestParam("fileId") String fileId,
            @RequestParam("fileName") String fileName,
            @RequestParam("creatorName") String creatorName,
            @RequestParam("fileOutline") String fileOutline,
            HttpSession session) {
        try {
            List<String> FileNames = fileMapper.findFileByCreatorName(creatorName);
            if (FileNames.contains(fileName)) {
                return APIResponse.error(500, "文件名已被使用");
            }
            byte[] sharedSecret = (byte[]) session.getAttribute("sharedSecret");
            if (sharedSecret == null) {
                return APIResponse.error(500, "共享密钥不存在于会话中");
            }

            byte[] encryptedChunkBytes = chunk.getBytes();
            byte[] decryptedDataBytes = ecdhService.decryptFile(encryptedChunkBytes, sharedSecret);
            saveChunk(decryptedDataBytes, chunkIndex, fileId);

            if (chunkIndex == 0) {
                boolean inserted = insertFile(fileId, fileName, creatorName, fileOutline, totalChunks, 1);
                if (!inserted) {
                    return APIResponse.error(500, "插入数据库失败");
                }
            } else {
                fileMapper.updateChunksByFileID(chunkIndex + 1, fileId);
            }

            if (chunkIndex + 1 == totalChunks) {
                if (areAllChunksPresent(totalChunks, fileId)) {
                    mergeChunks(totalChunks, fileId, fileName);
                    return APIResponse.success("所有块上传并合并成功");
                } else {
                    return APIResponse.error(500, "数据块缺失");
                }
            }

            return APIResponse.success("Chunk " + (chunkIndex + 1) + " 上传并解密成功");
        } catch (Exception e) {
            e.printStackTrace();
            return APIResponse.error(500, "上传块 " + (chunkIndex + 1) + " 出错: " + e.getMessage());
        }
    }


    @GetMapping("/download")
    public StreamingResponseBody downloadFile(@RequestParam("fileName") String fileName,
                                              HttpServletResponse response) {
        return outputStream -> {
            try {
                File fileRecord = fileMapper.findFileByFileName(fileName);
                if (fileRecord == null) {
                    throw new FileNotFoundException("File not found");
                }

                String filePath = fileRecord.getFilePath();
                java.io.File file = new java.io.File(Paths.get(filePath, fileName + ".csv").toString());
                if (!file.exists()) {
                    throw new FileNotFoundException("File not found on server");
                }


                // 设置响应内容类型和文件名
                response.setContentType("text/csv");
                response.setHeader("Content-Disposition", "attachment; filename=\"" + fileName + ".csv\"");

                try (InputStream inputStream = Files.newInputStream(file.toPath())) {
                    byte[] buffer = new byte[10 * 1024]; // 10KB buffer
                    int bytesRead;

                    // 直接将文件内容写入响应输出流
                    while ((bytesRead = inputStream.read(buffer)) != -1) {
                        outputStream.write(buffer, 0, bytesRead);
                        outputStream.flush(); // 立即发送数据
                    }
                }
            } catch (Exception e) {
                e.printStackTrace(); // 打印堆栈跟踪信息
                throw new IOException("Error processing file download: " + e.getMessage(), e);
            }
        };
    }

    // **将字节数组转换为十六进制字符串的方法**
    private String bytesToHex(byte[] bytes) {
        StringBuilder hexString = new StringBuilder();
        for (byte b : bytes) {
            String hex = Integer.toHexString(0xFF & b);
            if (hex.length() == 1) {
                hexString.append('0'); // 确保每个字节都用两位表示
            }
            hexString.append(hex);
        }
        return hexString.toString();
    }

    // 保存单个文件块到以 fileId 命名的指定目录
    private void saveChunk(byte[] data, int chunkIndex, String fileId) throws IOException {
        // 指定保存目录为 C:\Users\zzy\Desktop\1\fileId
        Path chunkDir = Paths.get(DIRECTORY_PATH, fileId);
        
        // 创建保存目录（如果不存在）
        if (!Files.exists(chunkDir)) {
            Files.createDirectories(chunkDir);
        }

        // 保存块到以 fileId 命名的子目录
        Path chunkPath = chunkDir.resolve("chunk_" + chunkIndex);  // 保存为 chunk_0, chunk_1, etc.
        Files.write(chunkPath, data);
    }

    // 检查所有块是否都存在
    private boolean areAllChunksPresent(int totalChunks, String fileId) {
        Path chunkDir = Paths.get(DIRECTORY_PATH, fileId);
        for (int i = 0; i < totalChunks; i++) {
            Path chunkPath = chunkDir.resolve("chunk_" + i);
            if (!Files.exists(chunkPath)) {
                System.err.println("Missing chunk: " + chunkPath.toString());
                return false;  // 如果某个块不存在，返回 false
            }
        }
        return true;  // 如果所有块都存在，返回 true
    }

    // 合并所有块
    private void mergeChunks(int totalChunks, String fileId, String fileName) throws IOException {
        // 创建合并后的输出文件，保存在 C:\Users\zzy\Desktop\1\fileName.dat
        Path chunkDir = Paths.get(DIRECTORY_PATH, fileId); // 确保使用 fileId 作为文件夹
        Path outputFile = chunkDir.resolve(fileName + ".csv"); // 合并后的文件名

        try (OutputStream outputStream = Files.newOutputStream(outputFile)) {
            // 遍历所有块，按顺序合并它们
            for (int i = 0; i < totalChunks; i++) {
                Path chunkPath = chunkDir.resolve("chunk_" + i);
                Files.copy(chunkPath, outputStream);
            }
        }

        // 合并完成后可以选择删除块文件
        deleteChunks(totalChunks, fileId);
        System.out.println("Chunks merged successfully into: " + outputFile.toString());

    }

    // 删除所有块文件
    private void deleteChunks(int totalChunks, String fileId) throws IOException {
        Path chunkDir = Paths.get(DIRECTORY_PATH, fileId);
        for (int i = 0; i < totalChunks; i++) {
            Path chunkPath = chunkDir.resolve("chunk_" + i);
            Files.deleteIfExists(chunkPath);  // 删除块文件
        }
    }

    // 在数据库插入记录
    private boolean insertFile(String fileId, String fileName, String creatorName, String fileOutline, int totalChunks, int chunkIndex) throws IOException {
        Path chunkDir = Paths.get(DIRECTORY_PATH, fileId);

        File file = new File();
        file.setFileId(fileId);
        file.setFileName(fileName);
        file.setFilePath(chunkDir.toString()); // 将 Path 转为 String
        file.setUsageTime(new Date());
        file.setCreatorName(creatorName);
        file.setFileOutline(fileOutline);
        file.setTotalChunks(totalChunks);
        file.setUploadedChunks(chunkIndex + 1);
        System.out.println(file);
        int result = fileMapper.insert(file);


        return result > 0;
    }



}
