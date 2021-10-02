package io.dataease.service.panel;

import io.dataease.base.domain.PanelPdfTemplate;
import io.dataease.base.domain.PanelPdfTemplateExample;
import io.dataease.base.mapper.PanelPdfTemplateMapper;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * Author: wangjiahao
 * Date: 9/1/21
 * Description:
 */
@Service
public class PanelPdfTemplateService {

    @Resource
    private PanelPdfTemplateMapper panelPdfTemplateMapper;

    public List<PanelPdfTemplate> queryAll(){
        PanelPdfTemplateExample example = new PanelPdfTemplateExample();
        example.setOrderByClause("sort asc");
        return panelPdfTemplateMapper.selectByExampleWithBLOBs(example);
    }
}
