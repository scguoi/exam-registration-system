package com.exam.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.exam.entity.Notice;
import com.exam.common.PageResult;

import java.util.List;

/**
 * 公告服务接口
 * 
 * @author system
 * @since 2024-10-16
 */
public interface NoticeService extends IService<Notice> {

    /**
     * 分页查询公告列表
     */
    PageResult<Notice> getNoticePage(Integer current, Integer size, String title, String type, Integer status);

    /**
     * 分页查询已发布的公告列表
     */
    PageResult<Notice> getPublishedNoticePage(Integer current, Integer size, String type);

    /**
     * 获取置顶公告
     */
    List<Notice> getTopNotices();

    /**
     * 创建公告
     */
    Notice createNotice(Notice notice);

    /**
     * 增加浏览次数
     */
    boolean incrementViewCount(Long noticeId);
}
