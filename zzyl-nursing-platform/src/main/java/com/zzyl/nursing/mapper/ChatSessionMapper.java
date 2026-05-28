package com.zzyl.nursing.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zzyl.nursing.domain.ChatSession;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface ChatSessionMapper extends BaseMapper<ChatSession>
{
    List<ChatSession> selectLatestSessionsByUser(@Param("userId") Long userId, @Param("userType") Integer userType);
}
