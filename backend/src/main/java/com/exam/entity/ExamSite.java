package com.exam.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 考点表实体类
 * 
 * @author system
 * @since 2024-10-16
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("exam_site")
public class ExamSite implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 考试ID
     */
    @TableField("exam_id")
    private Long examId;

    /**
     * 考点名称
     */
    @TableField("site_name")
    private String siteName;

    /**
     * 省份
     */
    @TableField("province")
    private String province;

    /**
     * 城市
     */
    @TableField("city")
    private String city;

    /**
     * 区/县
     */
    @TableField("district")
    private String district;

    /**
     * 详细地址
     */
    @TableField("address")
    private String address;

    /**
     * 联系人
     */
    @TableField("contact_person")
    private String contactPerson;

    /**
     * 联系电话
     */
    @TableField("contact_phone")
    private String contactPhone;

    /**
     * 容纳人数
     */
    @TableField("capacity")
    private Integer capacity;

    /**
     * 当前报名人数
     */
    @TableField("current_count")
    private Integer currentCount;

    /**
     * 经度(用于地图展示)
     */
    @TableField("longitude")
    private BigDecimal longitude;

    /**
     * 纬度(用于地图展示)
     */
    @TableField("latitude")
    private BigDecimal latitude;

    /**
     * 状态(1-启用 2-禁用)
     */
    @TableField("status")
    private Integer status;

    /**
     * 备注
     */
    @TableField("remark")
    private String remark;

    /**
     * 创建时间
     */
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
