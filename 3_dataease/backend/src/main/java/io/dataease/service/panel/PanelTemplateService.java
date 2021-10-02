package io.dataease.service.panel;

import io.dataease.base.domain.*;
import io.dataease.base.mapper.PanelTemplateMapper;
import io.dataease.base.mapper.ext.ExtPanelTemplateMapper;
import io.dataease.commons.constants.CommonConstants;
import io.dataease.commons.utils.AuthUtils;
import io.dataease.commons.utils.BeanUtils;
import io.dataease.controller.request.panel.PanelTemplateRequest;
import io.dataease.dto.panel.PanelTemplateDTO;
import io.dataease.exception.DataEaseException;
import io.dataease.i18n.Translator;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Author: wangjiahao
 * Date: 2021-03-05
 * Description:
 */
@Service
public class PanelTemplateService {

    private Logger LOGGER = LoggerFactory.getLogger(this.getClass());

    @Resource
    private PanelTemplateMapper panelTemplateMapper;
    @Resource
    private ExtPanelTemplateMapper extPanelTemplateMapper;

    public List<PanelTemplateDTO> templateList(PanelTemplateRequest panelTemplateRequest) {
        panelTemplateRequest.setWithBlobs("N");
        List<PanelTemplateDTO> panelTemplateList = extPanelTemplateMapper.panelTemplateList(panelTemplateRequest);
        if(panelTemplateRequest.getWithChildren()){
            getTreeChildren(panelTemplateList);
        }
        return panelTemplateList;
    }

    public void getTreeChildren(List<PanelTemplateDTO> parentPanelTemplateDTO){
        Optional.ofNullable(parentPanelTemplateDTO).ifPresent(parent -> parent.forEach(panelTemplateDTO -> {
            List<PanelTemplateDTO> panelTemplateDTOChildren = extPanelTemplateMapper.panelTemplateList(new PanelTemplateRequest(panelTemplateDTO.getId()));
            panelTemplateDTO.setChildren(panelTemplateDTOChildren);
            getTreeChildren(panelTemplateDTOChildren);
        }));
    }

    public List<PanelTemplateDTO> getSystemTemplateType(PanelTemplateRequest panelTemplateRequest){
        return extPanelTemplateMapper.panelTemplateList(panelTemplateRequest);
    }


    @Transactional
    public PanelTemplateDTO save(PanelTemplateRequest request) {
        if (StringUtils.isEmpty(request.getId())) {
            request.setId(UUID.randomUUID().toString());
            request.setCreateTime(System.currentTimeMillis());
            request.setCreateBy(AuthUtils.getUser().getUsername());
            //如果level 是0（第一级）指的是分类目录 设置父级为对应的templateType
            if(request.getLevel()==0){
                request.setPid(request.getTemplateType());
                String nameCheckResult = this.nameCheck(CommonConstants.OPT_TYPE.INSERT,request.getName(),request.getPid(),null);
                if(CommonConstants.CHECK_RESULT.EXIST_ALL.equals(nameCheckResult)){
                    DataEaseException.throwException(Translator.get("i18n_same_folder_can_not_repeat"));
                }
            }else{//模板插入 相同文件夹同名的模板进行覆盖(先删除)
                PanelTemplateExample exampleDelete = new PanelTemplateExample();
                exampleDelete.createCriteria().andPidEqualTo(request.getPid()).andNameEqualTo(request.getName());
                panelTemplateMapper.deleteByExample(exampleDelete);
            }
            panelTemplateMapper.insert(request);
        } else {
            String nameCheckResult = this.nameCheck(CommonConstants.OPT_TYPE.UPDATE,request.getName(),request.getPid(),request.getId());
            if(CommonConstants.CHECK_RESULT.EXIST_ALL.equals(nameCheckResult)){
                DataEaseException.throwException(Translator.get("i18n_same_folder_can_not_repeat"));
            }
            panelTemplateMapper.updateByPrimaryKeySelective(request);
        }
        PanelTemplateDTO panelTemplateDTO = new PanelTemplateDTO();
        BeanUtils.copyBean(panelTemplateDTO, request);
        panelTemplateDTO.setLabel(request.getName());
        return panelTemplateDTO;
    }


    //名称检查
    public String nameCheck(String optType,String name,String pid,String id){
        PanelTemplateExample example = new PanelTemplateExample();
        if(CommonConstants.OPT_TYPE.INSERT.equals(optType)){
            example.createCriteria().andPidEqualTo(pid).andNameEqualTo(name);

        }else if(CommonConstants.OPT_TYPE.UPDATE.equals(optType)){
            example.createCriteria().andPidEqualTo(pid).andNameEqualTo(name).andIdNotEqualTo(id);
        }
        List<PanelTemplate> panelTemplates = panelTemplateMapper.selectByExample(example);
        if(CollectionUtils.isEmpty(panelTemplates)){
            return CommonConstants.CHECK_RESULT.NONE;
        }else{
            return CommonConstants.CHECK_RESULT.EXIST_ALL;
        }
    }

    public String nameCheck(PanelTemplateRequest request){
        return nameCheck(request.getOptType(),request.getName(),request.getPid(),request.getId());

    }


    public void delete(String id){
        Assert.notNull(id, "id cannot be null");
        panelTemplateMapper.deleteByPrimaryKey(id);
    }


    public PanelTemplateWithBLOBs findOne(String panelId){
       return panelTemplateMapper.selectByPrimaryKey(panelId);
    }

    public List<PanelTemplateDTO> find(PanelTemplateRequest panelTemplateRequest){
        List<PanelTemplateDTO> panelTemplateList = extPanelTemplateMapper.panelTemplateList(panelTemplateRequest);
        return panelTemplateList;
    }

}
