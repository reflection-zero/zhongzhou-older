package com.zzyl.nursing.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessageVo {
    /** 消息类型: message/typing/error/session */
    private String type;
    /** 会话ID */
    private Long sessionId;
    /** 角色 user|assistant */
    private String role;
    /** 消息内容 */
    private String content;
    /** 时间戳 */
    private String timestamp;
    /** typing状态 */
    private Boolean status;
}
