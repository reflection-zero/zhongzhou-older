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
@ApiModel("聊天会话")
public class ChatSession extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    @ApiModelProperty("id")
    private Long id;

    @ApiModelProperty("用户ID")
    private Long userId;

    @ApiModelProperty("用户类型 0=admin,1=family_member")
    private Integer userType;

    @ApiModelProperty("会话标题")
    private String title;

    @ApiModelProperty("对话历史摘要")
    private String summary;

    @ApiModelProperty("状态 1=active,0=closed")
    private Integer status;
}
