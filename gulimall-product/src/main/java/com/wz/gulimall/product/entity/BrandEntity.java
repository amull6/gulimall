package com.wz.gulimall.product.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.io.Serializable;
import java.util.Date;

import lombok.Data;
import org.hibernate.validator.constraints.URL;

import javax.validation.constraints.*;

/**
 * 品牌
 *
 * @author qj
 * @email emailofqj@163.com
 * @date 2022-04-29 10:51:50
 */
@Data
@TableName("pms_brand")
public class BrandEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 品牌id
     */
    @TableId
    private Long brandId;
    /**
     * 品牌名
     */
    @NotBlank(message = "品牌名必须非空")
    private String name;
    /**
     * 品牌logo地址
     */
    @NotBlank
    @URL(message = "品牌logo地址必须是一个合法URL")
    private String logo;
    /**
     * 介绍
     */
    @NotBlank(message = "介绍必须非空")
    private String descript;
    /**
     * 显示状态[0-不显示；1-显示]
     */
    @NotNull
    private Integer showStatus;
    /**
     * 检索首字母
     */
    @Pattern(regexp = "^[a-zA-Z]$", message = "检索首字母必须在a-z或A-Z之间")
    @NotBlank
    private String firstLetter;
    /**
     * 排序
     */
    @NotNull
    @Min(value = 0, message = "排序必须是大于零的整数")
    private Integer sort;

}
