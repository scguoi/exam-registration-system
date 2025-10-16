package com.exam.controller;

import com.exam.common.Result;
import com.exam.security.CustomUserDetails;
import com.exam.utils.FileUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * 文件控制器
 * 
 * @author system
 * @since 2024-10-16
 */
@Slf4j
@RestController
@RequestMapping("/v1/files")
public class FileController {

    @Autowired
    private FileUtils fileUtils;

    /**
     * 上传头像
     */
    @PostMapping("/avatar")
    public Result<String> uploadAvatar(@AuthenticationPrincipal CustomUserDetails userDetails,
                                      @RequestParam("file") MultipartFile file) {
        try {
            String filePath = fileUtils.uploadAvatar(file, userDetails.getUserId());
            return Result.success("上传成功", filePath);
        } catch (Exception e) {
            log.error("头像上传失败", e);
            return Result.error("上传失败: " + e.getMessage());
        }
    }

    /**
     * 上传证明材料
     */
    @PostMapping("/material")
    public Result<String> uploadMaterial(@AuthenticationPrincipal CustomUserDetails userDetails,
                                        @RequestParam("file") MultipartFile file) {
        try {
            String filePath = fileUtils.uploadMaterial(file, userDetails.getUserId());
            return Result.success("上传成功", filePath);
        } catch (Exception e) {
            log.error("材料上传失败", e);
            return Result.error("上传失败: " + e.getMessage());
        }
    }

    /**
     * 上传考试文件（管理员）
     */
    @PostMapping("/exam")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<String> uploadExamFile(@RequestParam("file") MultipartFile file,
                                        @RequestParam("examId") Long examId) {
        try {
            String filePath = fileUtils.uploadExamFile(file, examId);
            return Result.success("上传成功", filePath);
        } catch (Exception e) {
            log.error("考试文件上传失败", e);
            return Result.error("上传失败: " + e.getMessage());
        }
    }

    /**
     * 下载文件
     */
    @GetMapping("/download/**")
    public void downloadFile(HttpServletResponse response) {
        // 这里可以实现文件下载逻辑
        // 由于路径是通配符，需要从request中获取完整路径
        response.setStatus(HttpServletResponse.SC_NOT_IMPLEMENTED);
    }

    /**
     * 删除文件
     */
    @DeleteMapping("/{filePath:.+}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> deleteFile(@PathVariable String filePath) {
        try {
            boolean success = fileUtils.deleteFile(filePath);
            return success ? Result.success() : Result.error("删除失败");
        } catch (Exception e) {
            log.error("文件删除失败", e);
            return Result.error("删除失败: " + e.getMessage());
        }
    }
}
