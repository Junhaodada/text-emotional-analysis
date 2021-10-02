package io.dataease.provider;

import com.google.gson.Gson;
import io.dataease.base.domain.DatasetTableField;
import io.dataease.base.domain.Datasource;
import io.dataease.controller.request.chart.ChartExtFilterRequest;
import io.dataease.datasource.dto.JdbcDTO;
import io.dataease.dto.chart.ChartCustomFilterDTO;
import io.dataease.dto.chart.ChartViewFieldDTO;
import io.dataease.dto.sqlObj.SQLObj;
import io.dataease.provider.pg.PgConstants;

import java.util.List;

/**
 * @Author gin
 * @Date 2021/5/17 2:42 下午
 */
public abstract class QueryProvider {

    public abstract Integer transFieldType(String field);

    public abstract String createSQLPreview(String sql, String orderBy);

    public abstract String createQuerySQL(String table, List<DatasetTableField> fields, boolean isGroup, Datasource ds);

    public abstract String createQuerySQLAsTmp(String sql, List<DatasetTableField> fields, boolean isGroup);

    public abstract String createQuerySQLWithPage(String table, List<DatasetTableField> fields, Integer page, Integer pageSize, Integer realSize, boolean isGroup, Datasource ds);

    public abstract String createQueryTableWithLimit(String table, List<DatasetTableField> fields, Integer limit, boolean isGroup, Datasource ds);

    public abstract String createQuerySqlWithLimit(String sql, List<DatasetTableField> fields, Integer limit, boolean isGroup);

    public abstract String createQuerySQLAsTmpWithPage(String sql, List<DatasetTableField> fields, Integer page, Integer pageSize, Integer realSize, boolean isGroup);

    public abstract String getSQL(String table, List<ChartViewFieldDTO> xAxis, List<ChartViewFieldDTO> yAxis, List<ChartCustomFilterDTO> customFilter, List<ChartExtFilterRequest> extFilterRequestList, Datasource ds);

    public abstract String getSQLAsTmp(String table, List<ChartViewFieldDTO> xAxis, List<ChartViewFieldDTO> yAxis, List<ChartCustomFilterDTO> customFilter, List<ChartExtFilterRequest> extFilterRequestList);

    public abstract String getSQLTableInfo(String table, List<ChartViewFieldDTO> xAxis, List<ChartCustomFilterDTO> customFilter, List<ChartExtFilterRequest> extFilterRequestList, Datasource ds);

    public abstract String getSQLAsTmpTableInfo(String sql, List<ChartViewFieldDTO> xAxis, List<ChartCustomFilterDTO> customFilter, List<ChartExtFilterRequest> extFilterRequestList, Datasource ds);

    public abstract String getSQLStack(String table, List<ChartViewFieldDTO> xAxis, List<ChartViewFieldDTO> yAxis, List<ChartCustomFilterDTO> customFilter, List<ChartExtFilterRequest> extFilterRequestList, List<ChartViewFieldDTO> extStack, Datasource ds);

    public abstract String getSQLAsTmpStack(String table, List<ChartViewFieldDTO> xAxis, List<ChartViewFieldDTO> yAxis, List<ChartCustomFilterDTO> customFilter, List<ChartExtFilterRequest> extFilterRequestList, List<ChartViewFieldDTO> extStack);

    public abstract String getSQLScatter(String table, List<ChartViewFieldDTO> xAxis, List<ChartViewFieldDTO> yAxis, List<ChartCustomFilterDTO> customFilter, List<ChartExtFilterRequest> extFilterRequestList, List<ChartViewFieldDTO> extBubble, Datasource ds);

    public abstract String getSQLAsTmpScatter(String table, List<ChartViewFieldDTO> xAxis, List<ChartViewFieldDTO> yAxis, List<ChartCustomFilterDTO> customFilter, List<ChartExtFilterRequest> extFilterRequestList, List<ChartViewFieldDTO> extBubble);

    public abstract String searchTable(String table);

    public abstract String getSQLSummary(String table, List<ChartViewFieldDTO> yAxis, List<ChartCustomFilterDTO> customFilter, List<ChartExtFilterRequest> extFilterRequestList);

    /**
     * 单指标汇总
     *
     * @param sql
     * @param yAxis
     * @param customFilter
     * @param extFilterRequestList
     * @return
     */
    public abstract String getSQLSummaryAsTmp(String sql, List<ChartViewFieldDTO> yAxis, List<ChartCustomFilterDTO> customFilter, List<ChartExtFilterRequest> extFilterRequestList);

    public abstract String wrapSql(String sql);

    public abstract String createRawQuerySQL(String table, List<DatasetTableField> fields, Datasource ds);

    public abstract String createRawQuerySQLAsTmp(String sql, List<DatasetTableField> fields);

    public void setSchema(SQLObj tableObj, Datasource ds){
        if(ds != null){
            String schema = new Gson().fromJson(ds.getConfiguration(), JdbcDTO.class).getSchema();
            schema = String.format( PgConstants.KEYWORD_TABLE, schema);
            tableObj.setTableName(schema + "." + tableObj.getTableName());
        }
    }
}
