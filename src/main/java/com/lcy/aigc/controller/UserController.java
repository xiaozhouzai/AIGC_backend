package com.lcy.aigc.controller;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.google.gson.Gson;
import com.lcy.aigc.annotation.AuthCheck;
import com.lcy.aigc.common.BaseResponse;
import com.lcy.aigc.common.DeleteRequest;
import com.lcy.aigc.common.ErrorCode;
import com.lcy.aigc.common.ResultUtils;
import com.lcy.aigc.constant.UserConstant;
import com.lcy.aigc.exception.BusinessException;
import com.lcy.aigc.exception.ThrowUtils;
import com.lcy.aigc.model.dto.user.*;
import com.lcy.aigc.model.entity.User;
import com.lcy.aigc.model.vo.LoginUserVo;
import com.lcy.aigc.model.vo.TagVo;
import com.lcy.aigc.model.vo.UserVo;
import com.lcy.aigc.service.UserService;
import com.lcy.aigc.utils.SMSUtils;
import com.lcy.aigc.utils.ValidateCodeUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.lcy.aigc.constant.UserConstant.USER_LOGIN_STATE;

/**
 * 用户接口
*
 */
@RestController
@RequestMapping("/user")
@Slf4j
public class UserController {

    @Resource
    private UserService userService;

    private static final String SALT = "lcy";

    @Resource
    private StringRedisTemplate stringRedisTemplate;


    // region 登录相关

    /**
     * 用户注册
     *
     * @param userRegisterRequest
     * @return
     */
    @PostMapping("/register")
    public BaseResponse<Long> userRegister(@RequestBody UserRegisterRequest userRegisterRequest) {
        if (userRegisterRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        String userAccount = userRegisterRequest.getUserAccount();
        String userPassword = userRegisterRequest.getUserPassword();
        String checkPassword = userRegisterRequest.getCheckPassword();
        if (StringUtils.isAnyBlank(userAccount, userPassword, checkPassword)) {
            return null;
        }
        long result = userService.userRegister(userAccount, userPassword, checkPassword);
        return ResultUtils.success(result);
    }

    /**
     * 用户登录
     *
     * @param userLoginRequest
     * @param request
     * @return
     */
    @PostMapping("/login")
    public BaseResponse<LoginUserVo> userLogin(@RequestBody UserLoginRequest userLoginRequest, HttpServletRequest request) {
        if (userLoginRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        String userAccount = userLoginRequest.getUserAccount();
        String userPassword = userLoginRequest.getUserPassword();
        if (StringUtils.isAnyBlank(userAccount, userPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        LoginUserVo loginUserVO = userService.userLogin(userAccount, userPassword, request);
        return ResultUtils.success(loginUserVO);
    }

    @PostMapping("/sendMsg")
    public BaseResponse<String> sendMsg(@RequestBody UserSendMessageRequest userSendMessageRequest, HttpServletRequest request){
        //获取手机号
        String phone = userSendMessageRequest.getUserPhone();

        if(StringUtils.isEmpty(phone)){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        //生成随机的4位验证码
        String code = ValidateCodeUtils.generateValidateCode(4).toString();
        log.info("code={}",code);

        //调用阿里云提供的短信服务API完成发送短信
        SMSUtils.sendMessage("瑞吉外卖","SMS_461435232",phone,code);
        //需要将生成的验证码保存到Session
        request.getSession().setAttribute(phone,code);
        //验证码加入缓存,设置五分钟过期
        stringRedisTemplate.opsForValue().set("code",code,5, TimeUnit.MINUTES);
        return ResultUtils.success("手机验证码短信发送成功,有效期五分钟");

    }

    /**
     * 用户登录
     *
     * @param userPhoneLoginRequest
     * @param request
     * @return
     */
    @PostMapping("/login/phone")
    public BaseResponse<LoginUserVo> userPhoneLogin(@RequestBody UserPhoneLoginRequest userPhoneLoginRequest, HttpServletRequest request) {
        log.info(userPhoneLoginRequest.toString());

        //获取手机号
        String phone = userPhoneLoginRequest.getUserPhone();
        //获取验证码
        String code = userPhoneLoginRequest.getCaptcha();
        //从Session中获取保存的验证码
        //Object codeInSession = session.getAttribute(phone);
        //从缓存中查询验证码
        Object codeInRedis = stringRedisTemplate.opsForValue().get("code");
        //进行验证码的比对（页面提交的验证码和Session中保存的验证码比对）
        if(codeInRedis == null || !codeInRedis.equals(code)){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getUserPhone,phone);
        User user = userService.getOne(queryWrapper);
        if(user == null){
            throw new BusinessException(100010,"您还未注册,或者您还未关联手机号");
        }
        request.getSession().setAttribute(USER_LOGIN_STATE, user);
        LoginUserVo loginUserVO = userService.getLoginUserVO(user);
        stringRedisTemplate.delete("code");
        return ResultUtils.success(loginUserVO);
    }


    /**
     * 用户注销
     *
     * @param request
     * @return
     */
    @PostMapping("/logout")
    public BaseResponse<Boolean> userLogout(HttpServletRequest request) {
        if (request == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        boolean result = userService.userLogout(request);
        return ResultUtils.success(result);
    }

    /**
     * 获取当前登录用户
     *
     * @param request
     * @return
     */
    @GetMapping("/get/login")
    public BaseResponse<LoginUserVo> getLoginUser(HttpServletRequest request) {
        User user = userService.getLoginUser(request);
        return ResultUtils.success(userService.getLoginUserVO(user));
    }

    // endregion

    // region 增删改查

    /**
     * 创建用户
     *
     * @param userAddRequest
     * @param request
     * @return
     */
    @PostMapping("/add")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Long> addUser(@RequestBody UserAddRequest userAddRequest, HttpServletRequest request) {
        if (userAddRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        String userPassword = userAddRequest.getUserPassword();
        // 2. 加密
        String encryptPassword = DigestUtils.md5DigestAsHex((SALT + userPassword).getBytes());
        userAddRequest.setUserPassword(encryptPassword);
        User user = new User();
        BeanUtils.copyProperties(userAddRequest, user);
        boolean result = userService.save(user);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(user.getId());
    }

    /**
     * 删除用户
     *
     * @param deleteRequest
     * @param request
     * @return
     */
    @PostMapping("/delete")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> deleteUser(@RequestBody DeleteRequest deleteRequest, HttpServletRequest request) {
        if (deleteRequest == null || deleteRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        boolean b = userService.removeById(deleteRequest.getId());
        return ResultUtils.success(b);
    }

    @DeleteMapping("/delete/all")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> deleteUserByIds(@RequestBody List<Long> ids) {
        if (ids.size() == 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        boolean b = userService.removeByIds(ids);
        return ResultUtils.success(b);
    }
    /**
     * 更新用户
     *
     * @param userUpdateRequest
     * @param request
     * @return
     */
    @PostMapping("/update")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> updateUser(@RequestBody UserUpdateRequest userUpdateRequest,
            HttpServletRequest request) {
        if (userUpdateRequest == null || userUpdateRequest.getId() == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User user = new User();
        //查表，如果管理员修改了密码，判断修改的密码和数据库的密码是否一致
        Long id = userUpdateRequest.getId();
        User oneUser = userService.getById(id);
        //查库的密码
        String userPassword = oneUser.getUserPassword();
        //要修改的密码
        String updateUserPassword = userUpdateRequest.getUserPassword();
        //如果不一致，说明密码被修改，需要重新加密
        if (!userPassword.equals(updateUserPassword)){
            // 2. 加密
            String encryptPassword = DigestUtils.md5DigestAsHex((SALT + updateUserPassword).getBytes());
            userUpdateRequest.setUserPassword(encryptPassword);
        }


        //一致则不需要再次加密
        BeanUtils.copyProperties(userUpdateRequest, user);
        boolean result = userService.updateById(user);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }

    /**
     * 根据 id 获取用户（仅管理员）
     *
     * @param id
     * @param request
     * @return
     */
    @GetMapping("/get")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<User> getUserById(long id, HttpServletRequest request) {
        if (id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User user = userService.getById(id);
        ThrowUtils.throwIf(user == null, ErrorCode.NOT_FOUND_ERROR);
        return ResultUtils.success(user);
    }

    /**
     * 根据 id 获取包装类
     *
     * @param id
     * @param request
     * @return
     */
    @GetMapping("/get/vo")
    public BaseResponse<UserVo> getUserVOById(long id, HttpServletRequest request) {
        BaseResponse<User> response = getUserById(id, request);
        User user = response.getData();
        return ResultUtils.success(userService.getUserVO(user));
    }

    /**
     * 分页获取用户列表（仅管理员）
     *
     * @param userQueryRequest
     * @param request
     * @return
     */
    @PostMapping("/list/page")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Page<User>> listUserByPage(@RequestBody UserQueryRequest userQueryRequest,
            HttpServletRequest request) {
        long current = userQueryRequest.getCurrent();
        long size = userQueryRequest.getPageSize();
        Page<User> userPage = userService.page(new Page<>(current, size),
                userService.getQueryWrapper(userQueryRequest));
        return ResultUtils.success(userPage);
    }

    /**
     * 分页获取用户封装列表
     *
     * @param userQueryRequest
     * @param request
     * @return
     */
    @PostMapping("/list/page/vo")
    public BaseResponse<Page<UserVo>> listUserVOByPage(@RequestBody UserQueryRequest userQueryRequest,
            HttpServletRequest request) {
        if (userQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        long current = userQueryRequest.getCurrent();
        long size = userQueryRequest.getPageSize();
        // 限制爬虫
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
        Page<User> userPage = userService.page(new Page<>(current, size),
                userService.getQueryWrapper(userQueryRequest));
        Page<UserVo> userVOPage = new Page<>(current, size, userPage.getTotal());
        List<UserVo> userVO = userService.getUserVO(userPage.getRecords());
        userVOPage.setRecords(userVO);
        return ResultUtils.success(userVOPage);
    }

    // endregion

    /**
     * 更新个人信息
     *
     * @param userUpdateMyRequest
     * @param request
     * @return
     */
    @PostMapping("/update/my")
    public BaseResponse<Boolean> updateMyUser(@RequestBody UserUpdateMyRequest userUpdateMyRequest,
            HttpServletRequest request) {
        if (userUpdateMyRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        User user = new User();
        BeanUtils.copyProperties(userUpdateMyRequest, user);
        user.setId(loginUser.getId());
        boolean result = userService.updateById(user);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }
    /**
     * 添加标签
     *
     */
    @PostMapping("/add/tag")
    public BaseResponse<TagVo> addTag(@RequestBody TagVo tagVo,
                                      HttpServletRequest request) {
        String tag = tagVo.getLabel();
        if (tag == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Object attribute = request.getSession().getAttribute(USER_LOGIN_STATE);
        if (attribute == null){
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }
        Gson gson = new Gson();
        User user = (User)attribute;
        String tags = user.getTags();
        List<String> list = gson.fromJson(tags, List.class);
        if (list == null){
            list = new ArrayList<>();
        }
        list.add(tag);
        String json = gson.toJson(list);
        user.setTags(json);
        boolean b = userService.updateById(user);
        ThrowUtils.throwIf(!b, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(tagVo);
    }


}
