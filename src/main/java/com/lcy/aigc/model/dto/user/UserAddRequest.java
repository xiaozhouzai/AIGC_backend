package com.lcy.aigc.model.dto.user;

import lombok.Data;

import java.io.Serializable;

@Data
public class UserAddRequest implements Serializable {

    private static final long serialVersionUID = -455919718822917722L;
    private String userAccount;
    private String userAvatar;
    private String userName;
    private String userPassword;
    private String userPhone;
    private String userRole;
}
