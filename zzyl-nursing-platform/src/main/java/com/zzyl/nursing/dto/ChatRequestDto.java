package com.zzyl.nursing.dto;

import lombok.Data;

@Data
public class ChatRequestDto {
    /** 会话ID (0=新建会话) */
    private Long sessionId;
    /** 消息内容 */
    private String content;
    /** 操作: send=发送消息, new=新建会话 */
    private String action;
}
