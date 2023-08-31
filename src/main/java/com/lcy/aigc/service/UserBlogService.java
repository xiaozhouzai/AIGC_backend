package com.lcy.aigc.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.lcy.aigc.model.entity.UserBlog;
import com.lcy.aigc.model.dto.blog.UserBlogQueryRequest;
import com.baomidou.mybatisplus.extension.service.IService;

/**
* @author lcyzh
* @description 针对表【user_blog(我的博客)】的数据库操作Service
* @createDate 2023-08-21 00:49:27
*/
public interface UserBlogService extends IService<UserBlog> {

    QueryWrapper<UserBlog> getQueryWrapper(UserBlogQueryRequest userBlogQueryRequest);
}
