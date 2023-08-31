package com.lcy.aigc.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lcy.aigc.model.entity.User;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户数据库操作
*
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {

}




