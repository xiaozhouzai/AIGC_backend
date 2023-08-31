package com.lcy.aigc.constant;

import com.lcy.aigc.utils.ValidateCodeUtils;

/**
 * 用户常量
*
 */
public interface UserConstant {

    /**
     * 用户登录态键
     */
    String USER_LOGIN_STATE = "user_login";

    String DEFAULT_ACCOUNT = "user_"+ ValidateCodeUtils.generateValidateCode(4);
    String DEFAULT_PASSWORD = "12345678";

    String DEFAULT_USER_NAME = "张三";
    String DEFAULT_USER_SIGNATURE = "这个人很懒，还没有设置座右铭";
    String DEFAULT_USER_AVATAR = "https://springboot-lcy.oss-cn-hangzhou.aliyuncs.com/avatar2.png";

    //  region 权限

    /**
     * 默认角色
     */
    String DEFAULT_ROLE = "user";

    /**
     * 管理员角色
     */
    String ADMIN_ROLE = "admin";

    /**
     * 被封号
     */
    String BAN_ROLE = "ban";

    // endregion
}
