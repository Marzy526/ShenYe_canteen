package com.canteen.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.canteen.entity.User;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper extends BaseMapper<User>{
}
