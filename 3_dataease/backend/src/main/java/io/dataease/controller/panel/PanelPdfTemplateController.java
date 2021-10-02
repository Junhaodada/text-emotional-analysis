package io.dataease.controller.panel;

import com.github.xiaoymin.knife4j.annotations.ApiSupport;
import io.dataease.base.domain.PanelPdfTemplate;
import io.dataease.service.panel.PanelPdfTemplateService;
import io.swagger.annotations.Api;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;


/**
 * Author: wangjiahao
 * Date: 2021-03-05
 * Description:
 */
@Api(tags = "仪表板：PDF导出模板")
@ApiSupport(order = 170)
@RestController
@RequestMapping("pdf-template")
public class PanelPdfTemplateController {

    @Resource
    private PanelPdfTemplateService panelPdfTemplateService;

    @GetMapping("queryAll")
    public List<PanelPdfTemplate> queryAll(){
        return panelPdfTemplateService.queryAll();
    }


}
