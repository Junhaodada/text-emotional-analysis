package io.dataease.base.domain;

import java.io.Serializable;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class SysMsgSetting implements Serializable {
    @ApiModelProperty(hidden = true)
    private Long msgSettingId;

    @ApiModelProperty("订阅用户ID")
    private Long userId;

    @ApiModelProperty("订阅类型ID")
    private Long typeId;

    @ApiModelProperty("订阅渠道ID")
    private Long channelId;

    @ApiModelProperty("订阅状态ID")
    private Boolean enable;

    private static final long serialVersionUID = 1L;

    public Boolean match(SysMsgSetting setting) {
        return setting.getTypeId() == typeId && setting.getChannelId() == channelId;
    }
}