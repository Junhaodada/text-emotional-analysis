package io.dataease.dto.dataset;

import io.dataease.datasource.dto.TableFiled;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class ExcelSheetData {
    @ApiModelProperty("标签")
    private String excelLable;
    @ApiModelProperty("数据集合")
    private List<List<String>> data;
    @ApiModelProperty("字段集合")
    private List<TableFiled> fields;
    @ApiModelProperty("是否sheet")
    private boolean isSheet = true;
    @ApiModelProperty("json数组")
    private List<Map<String, Object>> jsonArray;
    @ApiModelProperty("数据集名称")
    private String datasetName;
    @ApiModelProperty("excelID")
    private String sheetExcelId;
    @ApiModelProperty("sheetId")
    private String id;
    @ApiModelProperty("路径")
    private String path;
    @ApiModelProperty("字段MD5")
    private String fieldsMd5;
}
