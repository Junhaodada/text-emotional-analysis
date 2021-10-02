package io.dataease.base.mapper.ext;

import io.dataease.base.mapper.ext.query.GridExample;
import io.dataease.dto.dataset.DataSetTaskDTO;
import io.dataease.dto.dataset.DataSetTaskLogDTO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * @Author gin
 * @Date 2021/3/9 3:26 下午
 */
@Mapper
public interface ExtDataSetTaskMapper {
    List<DataSetTaskLogDTO> listTaskLog(GridExample example);

    List<DataSetTaskLogDTO> listUserTaskLog(GridExample example);

    List<DataSetTaskDTO> taskList(GridExample example);

    List<DataSetTaskDTO> userTaskList(GridExample example);

    List<DataSetTaskDTO> taskWithTriggers(GridExample example);
}
