package com.exam.controller;

import com.exam.common.PageResult;
import com.exam.common.Result;
import com.exam.entity.Notice;
import com.exam.service.NoticeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 公告控制器
 * 
 * @author system
 * @since 2024-10-16
 */
@Slf4j
@RestController
@RequestMapping("/v1/notices")
public class NoticeController {

    @Autowired
    private NoticeService noticeService;

    /**
     * 分页查询公告列表
     */
    @GetMapping
    public Result<PageResult<Notice>> getNoticePage(@RequestParam(defaultValue = "1") Integer current,
                                                   @RequestParam(defaultValue = "10") Integer size,
                                                   @RequestParam(required = false) String title,
                                                   @RequestParam(required = false) String type) {
        PageResult<Notice> result = noticeService.getPublishedNoticePage(current, size, type);
        return Result.success(result);
    }

    /**
     * 获取公告详情
     */
    @GetMapping("/{id}")
    public Result<Notice> getNoticeDetail(@PathVariable Long id) {
        Notice notice = noticeService.getById(id);
        if (notice == null || notice.getStatus() != 1) {
            return Result.notFound("公告不存在");
        }
        
        // 增加浏览次数
        noticeService.incrementViewCount(id);
        
        return Result.success(notice);
    }

    /**
     * 获取置顶公告
     */
    @GetMapping("/top")
    public Result<List<Notice>> getTopNotices() {
        List<Notice> notices = noticeService.getTopNotices();
        return Result.success(notices);
    }

    /**
     * 管理员分页查询公告列表
     */
    @GetMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<PageResult<Notice>> getAdminNoticePage(@RequestParam(defaultValue = "1") Integer current,
                                                        @RequestParam(defaultValue = "10") Integer size,
                                                        @RequestParam(required = false) String title,
                                                        @RequestParam(required = false) String type,
                                                        @RequestParam(required = false) Integer status) {
        PageResult<Notice> result = noticeService.getNoticePage(current, size, title, type, status);
        return Result.success(result);
    }

    /**
     * 创建公告
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Notice> createNotice(@RequestBody Notice notice) {
        Notice savedNotice = noticeService.createNotice(notice);
        return Result.success("创建成功", savedNotice);
    }

    /**
     * 更新公告
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> updateNotice(@PathVariable Long id, @RequestBody Notice notice) {
        notice.setId(id);
        boolean success = noticeService.updateById(notice);
        return success ? Result.success() : Result.error("更新失败");
    }

    /**
     * 删除公告
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> deleteNotice(@PathVariable Long id) {
        boolean success = noticeService.removeById(id);
        return success ? Result.success() : Result.error("删除失败");
    }
}
