package com.zzyl.nursing.vo;

import lombok.Data;

@Data
public class ChatSessionVo {
    private Long id;
    private String title;
    private String lastMessage;
    private String lastTime;
    private Integer messageCount;
    private Integer status;
}
