package com.lcy.aigc.model.dto.blog;

import lombok.Data;

import java.io.Serializable;

@Data
public class UserBlogAddRequest implements Serializable {
    private static final long serialVersionUID = 4872486076625444451L;
    private String avatar;
    private String blogDescription;
    private String content;
    private String title;
}
