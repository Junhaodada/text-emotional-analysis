package io.dataease.base.mapper.ext;

import org.apache.ibatis.annotations.Param;


public interface ExtSysAuthMapper {

    Boolean checkTreeNoManageCount(@Param("userId") Long userId , @Param("modelType") String modelType, @Param("nodeId") String nodeId);



}
