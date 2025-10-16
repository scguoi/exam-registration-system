package com.exam.utils;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * 文件工具类
 * 
 * @author system
 * @since 2024-10-16
 */
@Slf4j
@Component
public class FileUtils {

    @Value("${file.upload.path}")
    private String uploadPath;

    @Value("${file.upload.avatar-path}")
    private String avatarPath;

    @Value("${file.upload.material-path}")
    private String materialPath;

    @Value("${file.upload.exam-file-path}")
    private String examFilePath;

    @Value("${file.upload.ticket-path}")
    private String ticketPath;

    /**
     * 上传头像
     */
    public String uploadAvatar(MultipartFile file, Long userId) throws IOException {
        return uploadFile(file, avatarPath, userId, "avatar");
    }

    /**
     * 上传证明材料
     */
    public String uploadMaterial(MultipartFile file, Long userId) throws IOException {
        return uploadFile(file, materialPath, userId, "material");
    }

    /**
     * 上传考试文件
     */
    public String uploadExamFile(MultipartFile file, Long examId) throws IOException {
        return uploadFile(file, examFilePath, examId, "exam");
    }

    /**
     * 上传准考证
     */
    public String uploadTicket(MultipartFile file, Long registrationId) throws IOException {
        return uploadFile(file, ticketPath, registrationId, "ticket");
    }

    /**
     * 通用文件上传方法
     */
    private String uploadFile(MultipartFile file, String subPath, Long id, String type) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("文件不能为空");
        }

        // 获取文件扩展名
        String originalFilename = file.getOriginalFilename();
        if (StrUtil.isBlank(originalFilename)) {
            throw new IllegalArgumentException("文件名不能为空");
        }

        String extension = FileUtil.extName(originalFilename);
        if (StrUtil.isBlank(extension)) {
            throw new IllegalArgumentException("文件扩展名不能为空");
        }

        // 验证文件类型
        if (!isAllowedFileType(extension, type)) {
            throw new IllegalArgumentException("不支持的文件类型: " + extension);
        }

        // 验证文件大小
        if (!isAllowedFileSize(file.getSize(), type)) {
            throw new IllegalArgumentException("文件大小超出限制");
        }

        // 生成文件名
        String fileName = generateFileName(type, id, extension);

        // 创建目录
        String datePath = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
        String fullPath = uploadPath + subPath + datePath + "/";
        FileUtil.mkdir(fullPath);

        // 保存文件
        String filePath = fullPath + fileName;
        file.transferTo(new File(filePath));

        // 返回相对路径
        String relativePath = subPath + datePath + "/" + fileName;
        log.info("文件上传成功: {}", relativePath);
        
        return relativePath;
    }

    /**
     * 生成文件名
     */
    private String generateFileName(String type, Long id, String extension) {
        String timestamp = String.valueOf(System.currentTimeMillis());
        String randomStr = IdUtil.fastSimpleUUID().substring(0, 8);
        return String.format("%s_%d_%s_%s.%s", type, id, timestamp, randomStr, extension);
    }

    /**
     * 验证文件类型
     */
    private boolean isAllowedFileType(String extension, String type) {
        String lowerExt = extension.toLowerCase();
        
        switch (type) {
            case "avatar":
                return "jpg".equals(lowerExt) || "jpeg".equals(lowerExt) || "png".equals(lowerExt);
            case "material":
                return "jpg".equals(lowerExt) || "jpeg".equals(lowerExt) || "png".equals(lowerExt) || "pdf".equals(lowerExt);
            case "exam":
                return "pdf".equals(lowerExt) || "doc".equals(lowerExt) || "docx".equals(lowerExt);
            case "ticket":
                return "pdf".equals(lowerExt);
            default:
                return false;
        }
    }

    /**
     * 验证文件大小
     */
    private boolean isAllowedFileSize(long size, String type) {
        switch (type) {
            case "avatar":
                return size <= 2 * 1024 * 1024; // 2MB
            case "material":
                return size <= 5 * 1024 * 1024; // 5MB
            case "exam":
                return size <= 10 * 1024 * 1024; // 10MB
            case "ticket":
                return size <= 1 * 1024 * 1024; // 1MB
            default:
                return false;
        }
    }

    /**
     * 删除文件
     */
    public boolean deleteFile(String relativePath) {
        if (StrUtil.isBlank(relativePath)) {
            return false;
        }

        String fullPath = uploadPath + relativePath;
        File file = new File(fullPath);
        
        if (file.exists() && file.isFile()) {
            boolean deleted = file.delete();
            if (deleted) {
                log.info("文件删除成功: {}", relativePath);
            } else {
                log.warn("文件删除失败: {}", relativePath);
            }
            return deleted;
        }
        
        return false;
    }

    /**
     * 获取文件完整路径
     */
    public String getFullPath(String relativePath) {
        if (StrUtil.isBlank(relativePath)) {
            return null;
        }
        return uploadPath + relativePath;
    }
}
