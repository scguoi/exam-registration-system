package com.exam.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.exam.entity.Notice;
import com.exam.mapper.NoticeMapper;
import com.exam.service.NoticeService;
import com.exam.common.PageResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 公告服务实现类
 * 
 * @author system
 * @since 2024-10-16
 */
@Slf4j
@Service
public class NoticeServiceImpl extends ServiceImpl<NoticeMapper, Notice> implements NoticeService {

    @Override
    public PageResult<Notice> getNoticePage(Integer current, Integer size, String title, String type, Integer status) {
        Page<Notice> page = new Page<>(current, size);
        IPage<Notice> result = baseMapper.selectNoticePage(page, title, type, status);
        return PageResult.of(result.getRecords(), result.getTotal(), result.getCurrent(), result.getSize());
    }

    @Override
    public PageResult<Notice> getPublishedNoticePage(Integer current, Integer size, String type) {
        Page<Notice> page = new Page<>(current, size);
        IPage<Notice> result = baseMapper.selectPublishedNoticePage(page, type);
        return PageResult.of(result.getRecords(), result.getTotal(), result.getCurrent(), result.getSize());
    }

    @Override
    public List<Notice> getTopNotices() {
        return baseMapper.selectTopNotices();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Notice createNotice(Notice notice) {
        // 设置默认值
        if (notice.getType() == null) {
            notice.setType("notice");
        }
        if (notice.getIsTop() == null) {
            notice.setIsTop(0);
        }
        if (notice.getStatus() == null) {
            notice.setStatus(1);
        }
        if (notice.getViewCount() == null) {
            notice.setViewCount(0);
        }
        if (notice.getPublishTime() == null && notice.getStatus() == 1) {
            notice.setPublishTime(LocalDateTime.now());
        }

        save(notice);
        log.info("创建公告成功: {}", notice.getTitle());
        return notice;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean incrementViewCount(Long noticeId) {
        return baseMapper.incrementViewCount(noticeId) > 0;
    }
}
