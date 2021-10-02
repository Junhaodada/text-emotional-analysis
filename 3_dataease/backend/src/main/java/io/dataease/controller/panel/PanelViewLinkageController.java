package io.dataease.controller.panel;

import com.github.xiaoymin.knife4j.annotations.ApiSupport;
import io.dataease.controller.request.panel.PanelLinkageRequest;
import io.dataease.dto.PanelViewLinkageDTO;
import io.dataease.service.panel.PanelViewLinkageService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

/**
 * Author: wangjiahao
 * Date: 8/4/21
 * Description:
 */
@Api(tags = "仪表板：视图联动")
@ApiSupport(order = 171)
@RestController
@RequestMapping("linkage")
public class PanelViewLinkageController {

    @Resource
    private PanelViewLinkageService panelViewLinkageService;

    @ApiOperation("获取仪表板视图联动信息")
    @PostMapping("/getViewLinkageGather")
    public Map getViewLinkageGather(@RequestBody PanelLinkageRequest request){
        return panelViewLinkageService.getViewLinkageGather(request);
    }


    @ApiOperation("获取仪表板视图联动信息")
    @PostMapping("/saveLinkage")
    public void saveLinkage(@RequestBody PanelLinkageRequest request){
        panelViewLinkageService.saveLinkage(request);
    }


    @ApiOperation("获取当前仪表板所有联动信息")
    @GetMapping("/getPanelAllLinkageInfo/{panelId}")
    public Map<String, List<String>> getPanelAllLinkageInfo(@PathVariable String panelId){
        return panelViewLinkageService.getPanelAllLinkageInfo(panelId);
    }

}
