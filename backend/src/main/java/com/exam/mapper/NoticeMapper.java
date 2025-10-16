package com.exam.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.exam.entity.Notice;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 公告表Mapper接口
 * 
 * @author system
 * @since 2024-10-16
 */
@Mapper
public interface NoticeMapper extends BaseMapper<Notice> {

    /**
     * 分页查询公告列表
     * 
     * @param page 分页对象
     * @param title 标题
     * @param type 类型
     * @param status 状态
     * @return 公告列表
     */
    IPage<Notice> selectNoticePage(Page<Notice> page,
                                  @Param("title") String title,
                                  @Param("type") String type,
                                  @Param("status") Integer status);

    /**
     * 查询已发布的公告列表
     * 
     * @param page 分页对象
     * @param type 类型
     * @return 公告列表
     */
    IPage<Notice> selectPublishedNoticePage(Page<Notice> page, @Param("type") String type);

    /**
     * 查询置顶公告
     * 
     * @return 置顶公告列表
     */
    List<Notice> selectTopNotices();

    /**
     * 增加浏览次数
     * 
     * @param noticeId 公告ID
     * @return 更新行数
     */
    int incrementViewCount(@Param("noticeId") Long noticeId);
}
