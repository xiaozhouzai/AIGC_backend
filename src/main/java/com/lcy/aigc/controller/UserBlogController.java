package com.lcy.aigc.controller;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lcy.aigc.common.BaseResponse;
import com.lcy.aigc.common.DeleteRequest;
import com.lcy.aigc.common.ErrorCode;
import com.lcy.aigc.common.ResultUtils;
import com.lcy.aigc.constant.UserConstant;
import com.lcy.aigc.model.entity.User;
import com.lcy.aigc.model.entity.UserBlog;
import com.lcy.aigc.service.UserService;
import com.lcy.aigc.annotation.AuthCheck;
import com.lcy.aigc.exception.BusinessException;
import com.lcy.aigc.exception.ThrowUtils;
import com.lcy.aigc.model.dto.blog.UserBlogAddRequest;
import com.lcy.aigc.model.dto.blog.UserBlogQueryRequest;
import com.lcy.aigc.model.dto.blog.UserBlogUpdateRequest;
import com.lcy.aigc.service.UserBlogService;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;
@RestController
@RequestMapping("/blog")
public class UserBlogController {
    @Resource
    private UserBlogService userBlogService;

    @Resource
    private UserService userService;

    /**
     * 创建
     *
     * @param userBlogAddRequest
     * @param request
     * @return
     */
    @PostMapping("/add")
    public BaseResponse<Long> addUserBlog(@RequestBody UserBlogAddRequest userBlogAddRequest, HttpServletRequest request) {
        if (userBlogAddRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        UserBlog userBlog = new UserBlog();
        BeanUtils.copyProperties(userBlogAddRequest, userBlog);
        User loginUser = userService.getLoginUser(request);
        userBlog.setUserId(loginUser.getId());
        boolean result = userBlogService.save(userBlog);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        long newUserBlogId = userBlog.getId();
        return ResultUtils.success(newUserBlogId);
    }

    /**
     * 删除
     *
     * @param deleteRequest
     * @param request
     * @return
     */
    @PostMapping("/delete")
    public BaseResponse<Boolean> deleteUserBlog(@RequestBody DeleteRequest deleteRequest, HttpServletRequest request) {
        if (deleteRequest == null || deleteRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User user = userService.getLoginUser(request);
        long id = deleteRequest.getId();
        // 判断是否存在
        UserBlog olduserBlog = userBlogService.getById(id);
        ThrowUtils.throwIf(olduserBlog == null, ErrorCode.NOT_FOUND_ERROR);
        // 仅本人或管理员可删除
        if (!olduserBlog.getUserId().equals(user.getId()) && !userService.isAdmin(request)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        boolean b = userBlogService.removeById(id);
        return ResultUtils.success(b);
    }

    /**
     * 更新（仅管理员）
     *
     * @param userBlogUpdateRequest
     * @return
     */
    @PostMapping("/update")
    public BaseResponse<Boolean> updateUserBlog(@RequestBody UserBlogUpdateRequest userBlogUpdateRequest) {
        if (userBlogUpdateRequest == null || userBlogUpdateRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        UserBlog userBlog = new UserBlog();
        BeanUtils.copyProperties(userBlogUpdateRequest, userBlog);
        long id = userBlogUpdateRequest.getId();
        // 判断是否存在
        UserBlog olduserBlog = userBlogService.getById(id);
        ThrowUtils.throwIf(olduserBlog == null, ErrorCode.NOT_FOUND_ERROR);
        boolean result = userBlogService.updateById(userBlog);
        return ResultUtils.success(result);
    }

    @GetMapping("/get/myblog")
    public BaseResponse<List<UserBlog>> getUserBlogList(HttpServletRequest request) {
        ThrowUtils.throwIf(request == null,ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(request);
        ThrowUtils.throwIf(ObjectUtils.isEmpty(loginUser),ErrorCode.NOT_FOUND_ERROR);
        Long id = loginUser.getId();
        LambdaQueryWrapper<UserBlog> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(id != null,UserBlog::getUserId,id);
        List<UserBlog> list = userBlogService.list(queryWrapper);
        if (list.size() == 0){
            throw new BusinessException(10001,"您还没有博客");
        }
        return ResultUtils.success(list);
    }

    /**
     * 更新 个人编辑内容
     *
     * @param content
     * @param id
     * @return
     */
    @PostMapping("/update/user")
    @AuthCheck(mustRole = UserConstant.DEFAULT_ROLE)
    public BaseResponse<Boolean> updateMyBlog(@RequestParam("content") String content,@RequestParam("id") String id) {
        if (StringUtils.isAnyBlank(content,id)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        UserBlog userBlog = userBlogService.getById(id);
        if (ObjectUtils.isEmpty(userBlog)) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"查不到当前博客");
        }
        userBlog.setContent(content);
        boolean update = userBlogService.updateById(userBlog);
        return ResultUtils.success(update);
    }

    /**
     * 根据 id 获取
     *
     * @param id
     * @return
     */
    @GetMapping("/get")
    public BaseResponse<UserBlog> getUserBlogById(String id) {
        if (id == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        UserBlog userBlog = userBlogService.getById(Long.parseLong(id));
        if (userBlog == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }
        return ResultUtils.success(userBlog);
    }

    /**
     * 分页获取列表（封装类）
     *
     * @param userBlogQueryRequest
     * @return
     */
    @PostMapping("/list/page")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Page<UserBlog>> listUserBlogByPage(@RequestBody UserBlogQueryRequest userBlogQueryRequest) {
        long current = userBlogQueryRequest.getCurrent();
        long size = userBlogQueryRequest.getPageSize();
        // 限制爬虫
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
        Page<UserBlog> userBlogPage = userBlogService.page(new Page<>(current, size),
                userBlogService.getQueryWrapper(userBlogQueryRequest));
        return ResultUtils.success(userBlogPage);
    }

    /**
     * 分页获取当前用户的博客列表
     *
     * @param userBlogQueryRequest
     * @param request
     * @return
     */
    @PostMapping("/my/list/page")
    public BaseResponse<Page<UserBlog>> listMyUserBlogByPage(@RequestBody UserBlogQueryRequest userBlogQueryRequest,
                                                       HttpServletRequest request) {
        if (userBlogQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        Long userId = loginUser.getId();
        userBlogQueryRequest.setUserId(userId);
        long current = userBlogQueryRequest.getCurrent();
        long size = userBlogQueryRequest.getPageSize();
        // 限制爬虫
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
        QueryWrapper<UserBlog> queryWrapper = userBlogService.getQueryWrapper(userBlogQueryRequest);
        Page<UserBlog> userBlogPage = userBlogService.page(new Page<>(current, size),queryWrapper);
        return ResultUtils.success(userBlogPage);
    }
}
