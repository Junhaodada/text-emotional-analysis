package io.dataease.dto.panel;

import io.dataease.commons.model.ITreeBase;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Data
public class PanelViewDto implements ITreeBase<PanelViewDto> {
    @ApiModelProperty("ID")
    private String id;
    @ApiModelProperty("父ID")
    private String pid;
    @ApiModelProperty("类型")
    private String type;
    @ApiModelProperty("名称")
    private String name;
    @ApiModelProperty("子节点")
    private List<PanelViewDto> children;

    public void addChild(PanelViewDto dto){
        children = Optional.ofNullable(children).orElse(new ArrayList<>());
        children.add(dto);
    }
}
