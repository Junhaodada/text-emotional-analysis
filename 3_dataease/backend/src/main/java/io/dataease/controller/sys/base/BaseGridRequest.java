package io.dataease.controller.sys.base;

import io.dataease.base.mapper.ext.query.GridExample;
import io.swagger.annotations.ApiModelProperty;
import org.apache.commons.collections.CollectionUtils;

import java.io.Serializable;
import java.util.List;


public class BaseGridRequest implements Serializable {

    @ApiModelProperty("查询条件")
    private List<ConditionEntity> conditions;

    public List<ConditionEntity> getConditions() {
        return conditions;
    }

    public void setConditions(List<ConditionEntity> conditions) {
        this.conditions = conditions;
    }

    public List<String> getOrders() {
        return orders;
    }

    public void setOrders(List<String> orders) {
        this.orders = orders;
    }

    @ApiModelProperty("排序描述")
    private List<String> orders;

    public GridExample convertExample(){
        GridExample gridExample = new GridExample();
        if (CollectionUtils.isNotEmpty(conditions)) {
            GridExample.Criteria criteria = gridExample.createCriteria();
            conditions.forEach(criteria::addCondtion);
        }

        if (CollectionUtils.isNotEmpty(orders)){
            String orderByClause = String.join(", ", orders);
            gridExample.setOrderByClause(orderByClause);
        }

        return gridExample;
    }
}
