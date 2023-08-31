package com.lcy.aigc.model.dto.user;

import lombok.Data;

import java.io.Serializable;
@Data
public class UserUpdateRequest implements Serializable {
    private static final long serialVersionUID = -4664556807079315725L;
    private Long id;
    private String userAccount;
    private String userAvatar;
    private String userName;
    private String userPassword;
    private String userPhone;
    private String userRole;
}
