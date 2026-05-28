package com.zzyl.nursing.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zzyl.nursing.domain.ChatMessage;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface ChatMessageMapper extends BaseMapper<ChatMessage>
{
    List<ChatMessage> selectMessagesBySessionId(@Param("sessionId") Long sessionId);

    List<ChatMessage> selectRecentMessages(@Param("sessionId") Long sessionId, @Param("limit") Integer limit);
}
