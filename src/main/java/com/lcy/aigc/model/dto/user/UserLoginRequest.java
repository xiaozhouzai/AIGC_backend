package com.lcy.aigc.model.dto.user;

import lombok.Data;

import java.io.Serializable;

@Data
public class UserLoginRequest implements Serializable {
    private static final long serialVersionUID = 3171435181245090980L;
    private String userAccount;
    private String userPassword;

}
