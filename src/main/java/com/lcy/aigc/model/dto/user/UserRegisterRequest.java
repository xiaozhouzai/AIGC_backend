package com.lcy.aigc.model.dto.user;

import lombok.Data;

import java.io.Serializable;

@Data
public class UserRegisterRequest implements Serializable {
    private static final long serialVersionUID = -3253489667884614984L;
    private String checkPassword;
    private String userAccount;
    private String userPassword;
}
