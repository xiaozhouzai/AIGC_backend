package com.lcy.aigc.model.dto.blog;

import lombok.Data;

import java.io.Serializable;

@Data
public class UserBlogUpdateRequest implements Serializable {
    private static final long serialVersionUID = 4860190397464028376L;
    private String avatar;
    private String blogDescription;
    private String content;
    private Long id;
    private String title;

}
