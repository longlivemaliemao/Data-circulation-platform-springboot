package com.example.demo.Controller;

import com.example.demo.Mapper.FileMapper;
import com.example.demo.Model.APIResponse;
import com.example.demo.Model.File;
import com.example.demo.Model.FileInform;
import com.example.demo.Service.ECDHService;
import com.example.demo.Util.DownloadTokenUtil;
import com.example.demo.Util.UserContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;
import java.util.List;
import java.util.UUID;

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
            creatorName = UserContext.getUsername();
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
            creatorName = UserContext.getUsername();
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
            // 合法化处理文件名和 fileId，防止路径穿越
            fileId = Paths.get(fileId).getFileName().toString();
            fileName = Paths.get(fileName).getFileName().toString();
            creatorName = UserContext.getUsername();
            byte[] sharedSecret = (byte[]) session.getAttribute("sharedSecret");
            if (sharedSecret == null) {
                return APIResponse.error(500, "共享密钥不存在于会话中");
            }

            byte[] encryptedChunkBytes = chunk.getBytes();
            byte[] decryptedDataBytes = ecdhService.decryptFile(encryptedChunkBytes, sharedSecret);

            // 在保存分片前，先进行数据库检查
            if (chunkIndex == 0) {
                // 对于第一个分片，检查文件名是否已存在
                if (fileMapper.findFileByFileName(creatorName, fileName) != null) {
                    return APIResponse.error(500, "文件名已被使用");
                }
                // 插入文件元数据
                boolean inserted = insertFile(fileId, fileName, creatorName, fileOutline, totalChunks, chunkIndex);
                if (!inserted) {
                    return APIResponse.error(500, "插入数据库失败");
                }
            } else {
                // 对于后续分片，快速检查数据库中文件状态
                File file = fileMapper.findFileByFileName(creatorName, fileName);
                // 如果文件记录不存在（首个分片未成功），或文件已标记为完成，则拒绝
                if (file == null || file.getUploadedChunks() == file.getTotalChunks()) {
                    deleteChunks(totalChunks, fileId); // 清理可能已保存的无效分片
                    return APIResponse.error(500, "禁止上传分片：文件不存在或已完成");
                }
                // 注意：此处我们只读不写，避免了频繁更新数据库的性能开销
            }

            // 保存分片到磁盘
            saveChunk(decryptedDataBytes, chunkIndex, fileId);

            // 当最后一个分片上传时，执行合并操作并更新数据库
            if (chunkIndex + 1 == totalChunks) {
                if (areAllChunksPresent(totalChunks, fileId)) {
                    mergeChunks(totalChunks, fileId, fileName);
                    // 合并成功后，一次性将数据库中的 UPLOADED_CHUNKS 更新为 totalChunks
                    fileMapper.updateChunksByFileID(totalChunks, fileId);
                    return APIResponse.success("所有块上传并合并成功");
                } else {
                    deleteChunks(totalChunks, fileId);
                    return APIResponse.error(500, "数据块缺失");
                }
            }

            // 对于中间的分片，直接返回成功，不更新数据库
            return APIResponse.success("Chunk " + (chunkIndex + 1) + " 上传并解密成功");
        } catch (Exception e) {
            e.printStackTrace();
            return APIResponse.error(500, "上传块 " + (chunkIndex + 1) + " 出错: " + e.getMessage());
        }
    }

    @GetMapping("/stopUpload")
    @PreAuthorize("hasAuthority('数据提供方')")
    public APIResponse<String> stopUpload(@RequestParam("fileName") String fileName) {
        try {
            String creatorName = UserContext.getUsername();
            fileName = Paths.get(fileName).getFileName().toString();
            File file = fileMapper.findFileByFileName(creatorName, fileName);
            if (file == null || file.getUploadedChunks() == file.getTotalChunks()) {
                return APIResponse.error(400, "该文件已上传完成或该文件不存在");
            }
            fileMapper.deleteFilesByCreatorName(creatorName, fileName);
            deleteChunks(file.getTotalChunks(), file.getFileId());
            return APIResponse.success("停止下载成功");
        }catch (Exception e) {
            return APIResponse.error(500,"服务器内部错误: " + e.getMessage());
        }
    }


    @GetMapping("/getDownloadSign")
    public APIResponse<String> getDownloadSign(@RequestParam("applicationId") int applicationId) {
        try {
            String username = UserContext.getUsername();
            FileInform fileInform = fileMapper.selectFileInform(username, applicationId);
            java.io.File file = new java.io.File(fileInform.getFilePath(), fileInform.getFileName() + ".csv");
            if (!file.exists()) {
                return APIResponse.error(404, "文件不存在");
            }
            if (new Date().after(fileInform.getAuthEndTime())) {
                return APIResponse.error(404, "该数据授权已过期");
            }
            String signedToken = DownloadTokenUtil.generateDownloadToken(username, applicationId);
            return APIResponse.success(signedToken);
        } catch (Exception e) {
            return APIResponse.error(500, "生成下载链接失败: " + e.getMessage());
        }
    }

    @GetMapping("/download/{signedToken}")
    public void downloadFile(@PathVariable String signedToken, HttpServletResponse response) throws IOException {
        try {
            DownloadTokenUtil.ParsedToken token = DownloadTokenUtil.validateDownloadToken(signedToken);
            FileInform fileInform = fileMapper.selectFileInform(token.username, token.applicationId);
            java.io.File file = new java.io.File(fileInform.getFilePath(), fileInform.getFileName() + ".csv");

            if (new Date().after(fileInform.getAuthEndTime())) {
                response.sendError(403, "授权已过期");
                return;
            }

            response.reset();
            response.setContentType("application/octet-stream");
            response.setCharacterEncoding("utf-8");
            response.setContentLength((int) file.length());
            response.setHeader("Content-Disposition", "attachment;filename=" + fileInform.getFileName() + ".csv");

            try (BufferedInputStream bis = new BufferedInputStream(Files.newInputStream(file.toPath()));
                 OutputStream os = response.getOutputStream()) {
                byte[] buff = new byte[1024];
                int i;
                while ((i = bis.read(buff)) != -1) {
                    os.write(buff, 0, i);
                }
            }

        } catch (IllegalArgumentException e) {
            response.sendError(401, "下载链接无效或已过期");
        } catch (Exception e) {
            response.sendError(500, "服务器内部错误: " + e.getMessage());
        }
    }


    @GetMapping("/download")
    public void fileDownload(HttpServletResponse response,
                             @RequestParam("fileName") String fileName,
                             @RequestParam("applicationId") int applicationId) {
        try {
            // 文件名合法化，防止路径穿越
            fileName = Paths.get(fileName).getFileName().toString();
            String creatorName = UserContext.getUsername();

            // 从数据库获取文件记录
            FileInform fileInform = fileMapper.selectFileInform(creatorName, applicationId);
            if (fileInform == null) {
                response.sendError(HttpStatus.NOT_FOUND.value(), "文件记录不存在");
                return;
            }

            String filePath = fileInform.getFilePath();
            java.io.File file = new java.io.File(filePath, fileName + ".csv");

            if (!file.exists()) {
                response.sendError(HttpStatus.NOT_FOUND.value(), "文件未找到");
                return;
            }

            if (new Date().after(fileInform.getAuthEndTime())) {
                response.sendError(HttpStatus.UNAUTHORIZED.value(), "授权期限已过或未授权下载该文件");
                return;
            }

            // 设置下载响应头
            response.reset();
            response.setContentType("application/octet-stream");
            response.setCharacterEncoding("utf-8");
            String encodedFileName = URLEncoder.encode(fileName + ".csv", StandardCharsets.UTF_8.name());
            response.setHeader("Content-Disposition", "attachment;filename=" + encodedFileName);
            response.setContentLengthLong(file.length()); // 可选

            // 使用 try-with-resources 自动关闭资源
            try (InputStream in = Files.newInputStream(file.toPath());
                 OutputStream out = response.getOutputStream()) {

                byte[] buffer = new byte[8192]; // 更大缓冲更高效
                int bytesRead;
                while ((bytesRead = in.read(buffer)) != -1) {
                    out.write(buffer, 0, bytesRead);
                }
                out.flush();
            }

        } catch (Exception e) {
            // 打印完整异常堆栈
            e.printStackTrace();
            try {
                response.sendError(HttpStatus.INTERNAL_SERVER_ERROR.value(), "文件下载失败: " + e.getMessage());
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        }
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
