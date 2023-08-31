package com.lcy.aigc.model.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;
@Data
public class UserVo implements Serializable {
    private static final long serialVersionUID = 962560927616766044L;
    private Long id;
   private String userAccount;
   private String userAvatar;
   private String userName;
   private String userPhone;
   private String userRole;
   private String tags;
   private String userSignature;
   private Date createTime;
   private Date updateTime;
}
