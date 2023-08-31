package com.lcy.aigc.model.dto.user;

import lombok.Data;

import java.io.Serializable;

@Data
public class UserUpdateMyRequest implements Serializable {
    private static final long serialVersionUID = 6939746374570152443L;
    private String userAvatar;
    private String userName;
    private String userPhone;
}
