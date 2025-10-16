package com.exam.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 公告表实体类
 * 
 * @author system
 * @since 2024-10-16
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("notice")
public class Notice implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 公告标题
     */
    @TableField("title")
    private String title;

    /**
     * 公告内容(富文本)
     */
    @TableField("content")
    private String content;

    /**
     * 公告类型(notice-通知/policy-政策/exam-考试安排)
     */
    @TableField("type")
    private String type;

    /**
     * 是否置顶(0-否 1-是)
     */
    @TableField("is_top")
    private Integer isTop;

    /**
     * 状态(1-已发布 2-已下架)
     */
    @TableField("status")
    private Integer status;

    /**
     * 浏览次数
     */
    @TableField("view_count")
    private Integer viewCount;

    /**
     * 发布时间
     */
    @TableField("publish_time")
    private LocalDateTime publishTime;

    /**
     * 创建人ID
     */
    @TableField("create_by")
    private Long createBy;

    /**
     * 创建时间
     */
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
