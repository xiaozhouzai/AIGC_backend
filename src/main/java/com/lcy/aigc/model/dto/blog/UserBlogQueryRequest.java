package com.lcy.aigc.model.dto.blog;

import com.lcy.aigc.common.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

@EqualsAndHashCode(callSuper = true)
@Data
public class UserBlogQueryRequest extends PageRequest implements Serializable {
    private static final long serialVersionUID = -3300962853870796078L;
    private Long id;
    private Long userId;
    private String avatar;
    private String blogDescription;
    private String content;
    private String title;


}
