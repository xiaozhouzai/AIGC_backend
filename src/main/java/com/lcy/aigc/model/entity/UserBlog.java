package com.lcy.aigc.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 我的博客
 * @TableName user_blog
 */
@Data
@TableName(value ="user_blog")
public class UserBlog implements Serializable {
    /**
     * id
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 用户id
     */
    private Long userId;

    /**
     * 文章图标标识
     */
    private String avatar;

    /**
     * 标题
     */
    private String title;

    /**
     * 文章简述
     */
    private String blogDescription;

    /**
     * 内容
     */
    private String content;

    /**
     * 
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    /**
     * 0:未删除,1:删除
     */
    @TableLogic
    private Integer isDelete;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;

}