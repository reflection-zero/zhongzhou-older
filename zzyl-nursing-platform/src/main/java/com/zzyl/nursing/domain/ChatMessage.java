package com.zzyl.nursing.domain;

import com.zzyl.common.core.domain.BaseEntity;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ApiModel("聊天消息")
public class ChatMessage extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    @ApiModelProperty("id")
    private Long id;

    @ApiModelProperty("会话ID")
    private Long sessionId;

    @ApiModelProperty("角色 user|assistant")
    private String role;

    @ApiModelProperty("消息内容")
    private String content;

    @ApiModelProperty("注入的养老数据JSON(调试用)")
    private String contextData;
}
