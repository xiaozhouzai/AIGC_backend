package com.lcy.aigc.model.dto.user;

import lombok.Data;

import java.io.Serializable;

@Data
public class UserPhoneLoginRequest implements Serializable {

    private static final long serialVersionUID = -6123470434657234297L;
    private String captcha;
    private String userPhone;
}
