package io.dataease.service.chart;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import io.dataease.base.domain.*;
import io.dataease.base.mapper.ChartViewMapper;
import io.dataease.base.mapper.ext.ExtChartGroupMapper;
import io.dataease.base.mapper.ext.ExtChartViewMapper;
import io.dataease.commons.constants.JdbcConstants;
import io.dataease.commons.utils.AuthUtils;
import io.dataease.commons.utils.BeanUtils;
import io.dataease.commons.utils.CommonBeanFactory;
import io.dataease.commons.utils.LogUtil;
import io.dataease.controller.request.chart.*;
import io.dataease.datasource.provider.DatasourceProvider;
import io.dataease.datasource.provider.ProviderFactory;
import io.dataease.datasource.request.DatasourceRequest;
import io.dataease.datasource.service.DatasourceService;
import io.dataease.dto.chart.*;
import io.dataease.dto.dataset.DataSetTableUnionDTO;
import io.dataease.dto.dataset.DataTableInfoDTO;
import io.dataease.i18n.Translator;
import io.dataease.listener.util.CacheUtils;
import io.dataease.provider.QueryProvider;
import io.dataease.service.dataset.DataSetTableFieldsService;
import io.dataease.service.dataset.DataSetTableService;
import io.dataease.service.dataset.DataSetTableUnionService;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

/**
 * @Author gin
 * @Date 2021/3/1 12:34 下午
 */
@Service
public class ChartViewService {
    @Resource
    private ChartViewMapper chartViewMapper;
    @Resource
    private ExtChartViewMapper extChartViewMapper;
    @Resource
    private DataSetTableService dataSetTableService;
    @Resource
    private DatasourceService datasourceService;
    @Resource
    private DataSetTableFieldsService dataSetTableFieldsService;
    @Resource
    private ExtChartGroupMapper extChartGroupMapper;
    @Resource
    private DataSetTableUnionService dataSetTableUnionService;

    //默认使用非公平
    private ReentrantLock lock = new ReentrantLock();

    public ChartViewWithBLOBs save(ChartViewWithBLOBs chartView) {
        checkName(chartView);
        long timestamp = System.currentTimeMillis();
        chartView.setUpdateTime(timestamp);
        int i = chartViewMapper.updateByPrimaryKeySelective(chartView);
        if (i == 0) {
            chartView.setId(UUID.randomUUID().toString());
            chartView.setCreateBy(AuthUtils.getUser().getUsername());
            chartView.setCreateTime(timestamp);
            chartView.setUpdateTime(timestamp);
            chartViewMapper.insertSelective(chartView);
        }
        Optional.ofNullable(chartView.getId()).ifPresent(id -> {
            CacheUtils.remove(JdbcConstants.VIEW_CACHE_KEY, id);
        });
        return getOneWithPermission(chartView.getId());
    }

    public List<ChartViewDTO> list(ChartViewRequest chartViewRequest) {
        chartViewRequest.setUserId(String.valueOf(AuthUtils.getUser().getUserId()));
        return extChartViewMapper.search(chartViewRequest);
    }

    public List<ChartViewDTO> listAndGroup(ChartViewRequest chartViewRequest) {
        chartViewRequest.setUserId(String.valueOf(AuthUtils.getUser().getUserId()));
        List<ChartViewDTO> charts = extChartViewMapper.search(chartViewRequest);
        charts.forEach(ele -> ele.setIsLeaf(true));
        // 获取group下的子group
        ChartGroupRequest chartGroupRequest = new ChartGroupRequest();
        chartGroupRequest.setLevel(null);
        chartGroupRequest.setType("group");
        chartGroupRequest.setPid(chartViewRequest.getSceneId());
        chartGroupRequest.setUserId(String.valueOf(AuthUtils.getUser().getUserId()));
        chartGroupRequest.setSort("name asc,create_time desc");
        List<ChartGroupDTO> groups = extChartGroupMapper.search(chartGroupRequest);
        List<ChartViewDTO> group = groups.stream().map(ele -> {
            ChartViewDTO dto = new ChartViewDTO();
            BeanUtils.copyBean(dto, ele);
            dto.setIsLeaf(false);
            dto.setType("group");
            return dto;
        }).collect(Collectors.toList());
        group.addAll(charts);
        return group;
    }

    public List<ChartViewDTO> search(ChartViewRequest chartViewRequest) {
        String userId = String.valueOf(AuthUtils.getUser().getUserId());
        chartViewRequest.setUserId(userId);
        chartViewRequest.setSort("name asc");
        List<ChartViewDTO> ds = extChartViewMapper.search(chartViewRequest);
        if (CollectionUtils.isEmpty(ds)) {
            return ds;
        }

        TreeSet<String> ids = new TreeSet<>();
        ds.forEach(ele -> {
            ele.setIsLeaf(true);
            ele.setPid(ele.getSceneId());
            ids.add(ele.getPid());
        });

        List<ChartViewDTO> group = new ArrayList<>();
        ChartGroupRequest chartGroupRequest = new ChartGroupRequest();
        chartGroupRequest.setUserId(userId);
        chartGroupRequest.setIds(ids);
        List<ChartGroupDTO> search = extChartGroupMapper.search(chartGroupRequest);
        while (CollectionUtils.isNotEmpty(search)) {
            ids.clear();
            search.forEach(ele -> {
                ChartViewDTO dto = new ChartViewDTO();
                BeanUtils.copyBean(dto, ele);
                dto.setIsLeaf(false);
                dto.setType("group");
                group.add(dto);
                ids.add(ele.getPid());
            });
            chartGroupRequest.setIds(ids);
            search = extChartGroupMapper.search(chartGroupRequest);
        }

        List<ChartViewDTO> res = new ArrayList<>();
        Map<String, ChartViewDTO> map = new TreeMap<>();
        group.forEach(ele -> map.put(ele.getId(), ele));
        Iterator<Map.Entry<String, ChartViewDTO>> iterator = map.entrySet().iterator();
        while (iterator.hasNext()) {
            res.add(iterator.next().getValue());
        }
        res.sort(Comparator.comparing(ChartViewDTO::getName));
        res.addAll(ds);
        return res;
    }

    public ChartViewWithBLOBs get(String id) {
        return chartViewMapper.selectByPrimaryKey(id);
    }

    public ChartViewDTO getOneWithPermission(String id) {
        ChartViewRequest chartViewRequest = new ChartViewRequest();
        chartViewRequest.setId(id);
        chartViewRequest.setUserId(String.valueOf(AuthUtils.getUser().getUserId()));
        return extChartViewMapper.searchOne(chartViewRequest);
    }

    public void delete(String id) {
        chartViewMapper.deleteByPrimaryKey(id);
    }

    public void deleteBySceneId(String sceneId) {
        ChartViewExample chartViewExample = new ChartViewExample();
        chartViewExample.createCriteria().andSceneIdEqualTo(sceneId);
        chartViewMapper.deleteByExample(chartViewExample);
    }

    public ChartViewDTO getData(String id, ChartExtRequest requestList) throws Exception {
        ChartViewWithBLOBs view = chartViewMapper.selectByPrimaryKey(id);
        if (ObjectUtils.isEmpty(view)) {
            throw new RuntimeException(Translator.get("i18n_chart_delete"));
        }
        List<ChartViewFieldDTO> xAxis = new Gson().fromJson(view.getXAxis(), new TypeToken<List<ChartViewFieldDTO>>() {
        }.getType());
        List<ChartViewFieldDTO> yAxis = new Gson().fromJson(view.getYAxis(), new TypeToken<List<ChartViewFieldDTO>>() {
        }.getType());
        if (StringUtils.equalsIgnoreCase(view.getType(), "chart-mix")) {
            List<ChartViewFieldDTO> yAxisExt = new Gson().fromJson(view.getYAxisExt(), new TypeToken<List<ChartViewFieldDTO>>() {
            }.getType());
            yAxis.addAll(yAxisExt);
        }
        List<ChartViewFieldDTO> extStack = new Gson().fromJson(view.getExtStack(), new TypeToken<List<ChartViewFieldDTO>>() {
        }.getType());
        List<ChartViewFieldDTO> extBubble = new Gson().fromJson(view.getExtBubble(), new TypeToken<List<ChartViewFieldDTO>>() {
        }.getType());
        List<ChartFieldCustomFilterDTO> fieldCustomFilter = new Gson().fromJson(view.getCustomFilter(), new TypeToken<List<ChartFieldCustomFilterDTO>>() {
        }.getType());
        List<ChartViewFieldDTO> drill = new Gson().fromJson(view.getDrillFields(), new TypeToken<List<ChartViewFieldDTO>>() {
        }.getType());
        List<ChartCustomFilterDTO> customFilter = new ArrayList<>();
        for (ChartFieldCustomFilterDTO ele : fieldCustomFilter) {
            List<ChartCustomFilterDTO> collect = ele.getFilter().stream().map(f -> {
                ChartCustomFilterDTO dto = new ChartCustomFilterDTO();
                BeanUtils.copyBean(dto, f);
                dto.setField(dataSetTableFieldsService.get(f.getFieldId()));
                return dto;
            }).collect(Collectors.toList());
            customFilter.addAll(collect);
        }

        if (StringUtils.equalsIgnoreCase("text", view.getType())
                || StringUtils.equalsIgnoreCase("gauge", view.getType())
                || StringUtils.equalsIgnoreCase("liquid", view.getType())) {
            xAxis = new ArrayList<>();
            if (CollectionUtils.isEmpty(yAxis)) {
                ChartViewDTO dto = new ChartViewDTO();
                BeanUtils.copyBean(dto, view);
                return dto;
            }
        } else if (StringUtils.equalsIgnoreCase("table-info", view.getType())) {
            yAxis = new ArrayList<>();
            if (CollectionUtils.isEmpty(xAxis)) {
                ChartViewDTO dto = new ChartViewDTO();
                BeanUtils.copyBean(dto, view);
                return dto;
            }
        } else {
            if (CollectionUtils.isEmpty(xAxis) && CollectionUtils.isEmpty(yAxis)) {
                ChartViewDTO dto = new ChartViewDTO();
                BeanUtils.copyBean(dto, view);
                return dto;
            }
        }

        // 过滤来自仪表板的条件
        List<ChartExtFilterRequest> extFilterList = new ArrayList<>();

        //组件过滤条件
        if (ObjectUtils.isNotEmpty(requestList.getFilter())) {
            for (ChartExtFilterRequest request : requestList.getFilter()) {
                DatasetTableField datasetTableField = dataSetTableFieldsService.get(request.getFieldId());
                request.setDatasetTableField(datasetTableField);
                if (StringUtils.equalsIgnoreCase(datasetTableField.getTableId(), view.getTableId())) {
                    if (CollectionUtils.isNotEmpty(request.getViewIds())) {
                        if (request.getViewIds().contains(view.getId())) {
                            extFilterList.add(request);
                        }
                    } else {
                        extFilterList.add(request);
                    }
                }
            }
        }

        //联动过滤条件联动条件全部加上
        if (ObjectUtils.isNotEmpty(requestList.getLinkageFilters())) {
            for (ChartExtFilterRequest request : requestList.getLinkageFilters()) {
                DatasetTableField datasetTableField = dataSetTableFieldsService.get(request.getFieldId());
                request.setDatasetTableField(datasetTableField);
                if (StringUtils.equalsIgnoreCase(datasetTableField.getTableId(), view.getTableId())) {
                    if (CollectionUtils.isNotEmpty(request.getViewIds())) {
                        if (request.getViewIds().contains(view.getId())) {
                            extFilterList.add(request);
                        }
                    } else {
                        extFilterList.add(request);
                    }
                }
            }
        }

        // 下钻
        List<ChartExtFilterRequest> drillFilters = new ArrayList<>();
        boolean isDrill = false;
        List<ChartDrillRequest> drillRequest = requestList.getDrill();
        if (CollectionUtils.isNotEmpty(drillRequest) && (drill.size() > drillRequest.size())) {
            for (int i = 0; i < drillRequest.size(); i++) {
                ChartDrillRequest request = drillRequest.get(i);
                for (ChartDimensionDTO dto : request.getDimensionList()) {
                    ChartViewFieldDTO chartViewFieldDTO = drill.get(i);
                    // 将钻取值作为条件传递，将所有钻取字段作为xAxis并加上下一个钻取字段
                    if (StringUtils.equalsIgnoreCase(dto.getId(), chartViewFieldDTO.getId())) {
                        isDrill = true;
                        DatasetTableField datasetTableField = dataSetTableFieldsService.get(dto.getId());
                        ChartViewFieldDTO d = new ChartViewFieldDTO();
                        BeanUtils.copyBean(d, datasetTableField);

                        ChartExtFilterRequest drillFilter = new ChartExtFilterRequest();
                        drillFilter.setFieldId(dto.getId());
                        drillFilter.setValue(new ArrayList<String>() {{
                            add(dto.getValue());
                        }});
                        drillFilter.setOperator("in");
                        drillFilter.setDatasetTableField(datasetTableField);
                        extFilterList.add(drillFilter);

                        drillFilters.add(drillFilter);

                        if (!checkDrillExist(xAxis, extStack, d, view)) {
                            xAxis.add(d);
                        }
                        if (i == drillRequest.size() - 1) {
                            ChartViewFieldDTO nextDrillField = drill.get(i + 1);
                            if (!checkDrillExist(xAxis, extStack, nextDrillField, view)) {
                                xAxis.add(nextDrillField);
                            }
                        }
                    }
                }
            }
        }

        // 获取数据集,需校验权限
        DatasetTable table = dataSetTableService.get(view.getTableId());
        if (ObjectUtils.isEmpty(table)) {
            throw new RuntimeException(Translator.get("i18n_dataset_delete_or_no_permission"));
        }
        // 判断连接方式，直连或者定时抽取 table.mode
        DatasourceRequest datasourceRequest = new DatasourceRequest();
        List<String[]> data = new ArrayList<>();
        if (table.getMode() == 0) {// 直连
            Datasource ds = datasourceService.get(table.getDataSourceId());
            if (ObjectUtils.isEmpty(ds)) {
                throw new RuntimeException(Translator.get("i18n_datasource_delete"));
            }
            DatasourceProvider datasourceProvider = ProviderFactory.getProvider(ds.getType());
            datasourceRequest.setDatasource(ds);
            DataTableInfoDTO dataTableInfoDTO = new Gson().fromJson(table.getInfo(), DataTableInfoDTO.class);
            QueryProvider qp = ProviderFactory.getQueryProvider(ds.getType());
            if (StringUtils.equalsIgnoreCase(table.getType(), "db")) {
                datasourceRequest.setTable(dataTableInfoDTO.getTable());
                if (StringUtils.equalsIgnoreCase("text", view.getType()) || StringUtils.equalsIgnoreCase("gauge", view.getType()) || StringUtils.equalsIgnoreCase("liquid", view.getType())) {
                    datasourceRequest.setQuery(qp.getSQLSummary(dataTableInfoDTO.getTable(), yAxis, customFilter, extFilterList));
                } else if (StringUtils.containsIgnoreCase(view.getType(), "stack")) {
                    datasourceRequest.setQuery(qp.getSQLStack(dataTableInfoDTO.getTable(), xAxis, yAxis, customFilter, extFilterList, extStack, ds));
                } else if (StringUtils.containsIgnoreCase(view.getType(), "scatter")) {
                    datasourceRequest.setQuery(qp.getSQLScatter(dataTableInfoDTO.getTable(), xAxis, yAxis, customFilter, extFilterList, extBubble, ds));
                } else if (StringUtils.equalsIgnoreCase("table-info", view.getType())) {
                    datasourceRequest.setQuery(qp.getSQLTableInfo(dataTableInfoDTO.getTable(), xAxis, customFilter, extFilterList, ds));
                } else {
                    datasourceRequest.setQuery(qp.getSQL(dataTableInfoDTO.getTable(), xAxis, yAxis, customFilter, extFilterList, ds));
                }
            } else if (StringUtils.equalsIgnoreCase(table.getType(), "sql")) {
                if (StringUtils.equalsIgnoreCase("text", view.getType()) || StringUtils.equalsIgnoreCase("gauge", view.getType()) || StringUtils.equalsIgnoreCase("liquid", view.getType())) {
                    datasourceRequest.setQuery(qp.getSQLSummaryAsTmp(dataTableInfoDTO.getSql(), yAxis, customFilter, extFilterList));
                } else if (StringUtils.containsIgnoreCase(view.getType(), "stack")) {
                    datasourceRequest.setQuery(qp.getSQLAsTmpStack(dataTableInfoDTO.getSql(), xAxis, yAxis, customFilter, extFilterList, extStack));
                } else if (StringUtils.containsIgnoreCase(view.getType(), "scatter")) {
                    datasourceRequest.setQuery(qp.getSQLAsTmpScatter(dataTableInfoDTO.getSql(), xAxis, yAxis, customFilter, extFilterList, extBubble));
                } else if (StringUtils.equalsIgnoreCase("table-info", view.getType())) {
                    datasourceRequest.setQuery(qp.getSQLAsTmpTableInfo(dataTableInfoDTO.getSql(), xAxis, customFilter, extFilterList, ds));
                } else {
                    datasourceRequest.setQuery(qp.getSQLAsTmp(dataTableInfoDTO.getSql(), xAxis, yAxis, customFilter, extFilterList));
                }
            } else if (StringUtils.equalsIgnoreCase(table.getType(), "custom")) {
                DataTableInfoDTO dt = new Gson().fromJson(table.getInfo(), DataTableInfoDTO.class);
                List<DataSetTableUnionDTO> list = dataSetTableUnionService.listByTableId(dt.getList().get(0).getTableId());
                String sql = dataSetTableService.getCustomSQLDatasource(dt, list, ds);
                if (StringUtils.equalsIgnoreCase("text", view.getType()) || StringUtils.equalsIgnoreCase("gauge", view.getType()) || StringUtils.equalsIgnoreCase("liquid", view.getType())) {
                    datasourceRequest.setQuery(qp.getSQLSummaryAsTmp(sql, yAxis, customFilter, extFilterList));
                } else if (StringUtils.containsIgnoreCase(view.getType(), "stack")) {
                    datasourceRequest.setQuery(qp.getSQLAsTmpStack(sql, xAxis, yAxis, customFilter, extFilterList, extStack));
                } else if (StringUtils.containsIgnoreCase(view.getType(), "scatter")) {
                    datasourceRequest.setQuery(qp.getSQLAsTmpScatter(sql, xAxis, yAxis, customFilter, extFilterList, extBubble));
                } else if (StringUtils.equalsIgnoreCase("table-info", view.getType())) {
                    datasourceRequest.setQuery(qp.getSQLAsTmpTableInfo(sql, xAxis, customFilter, extFilterList, ds));
                } else {
                    datasourceRequest.setQuery(qp.getSQLAsTmp(sql, xAxis, yAxis, customFilter, extFilterList));
                }
            }
            data = datasourceProvider.getData(datasourceRequest);
            /**
             * 直连不实用缓存
             String key = "provider_sql_"+datasourceRequest.getDatasource().getId() + "_" + datasourceRequest.getTable() + "_" +datasourceRequest.getQuery();
             Object cache;
             if ((cache = CacheUtils.get(JdbcConstants.JDBC_PROVIDER_KEY, key)) == null) {
             data = datasourceProvider.getData(datasourceRequest);
             CacheUtils.put(JdbcConstants.JDBC_PROVIDER_KEY,key ,data, null, null);
             }else {
             data = (List<String[]>) cache;
             }
             */
        } else if (table.getMode() == 1) {// 抽取
            // 连接doris，构建doris数据源查询
            Datasource ds = (Datasource) CommonBeanFactory.getBean("DorisDatasource");
            DatasourceProvider datasourceProvider = ProviderFactory.getProvider(ds.getType());
            datasourceRequest.setDatasource(ds);
            String tableName = "ds_" + table.getId().replaceAll("-", "_");
            datasourceRequest.setTable(tableName);
            QueryProvider qp = ProviderFactory.getQueryProvider(ds.getType());
            if (StringUtils.equalsIgnoreCase("text", view.getType()) || StringUtils.equalsIgnoreCase("gauge", view.getType()) || StringUtils.equalsIgnoreCase("liquid", view.getType())) {
                datasourceRequest.setQuery(qp.getSQLSummary(tableName, yAxis, customFilter, extFilterList));
            } else if (StringUtils.containsIgnoreCase(view.getType(), "stack")) {
                datasourceRequest.setQuery(qp.getSQLStack(tableName, xAxis, yAxis, customFilter, extFilterList, extStack, ds));
            } else if (StringUtils.containsIgnoreCase(view.getType(), "scatter")) {
                datasourceRequest.setQuery(qp.getSQLScatter(tableName, xAxis, yAxis, customFilter, extFilterList, extBubble, ds));
            } else if (StringUtils.equalsIgnoreCase("table-info", view.getType())) {
                datasourceRequest.setQuery(qp.getSQLTableInfo(tableName, xAxis, customFilter, extFilterList, ds));
            } else {
                datasourceRequest.setQuery(qp.getSQL(tableName, xAxis, yAxis, customFilter, extFilterList, ds));
            }
            /*// 定时抽取使用缓存
            Object cache;
            // 仪表板有参数不实用缓存
            if (CollectionUtils.isNotEmpty(requestList.getFilter())) {
                data = datasourceProvider.getData(datasourceRequest);
            }
            // 仪表板无参数 且 未缓存过该视图 则查询后缓存
            else if ((cache = CacheUtils.get(JdbcConstants.VIEW_CACHE_KEY, id)) == null) {
                lock.lock();
                data = datasourceProvider.getData(datasourceRequest);
                CacheUtils.put(JdbcConstants.VIEW_CACHE_KEY, id, data, null, null);
            }
            // 仪表板有缓存 使用缓存
            else {
                data = (List<String[]>) cache;
            }*/
            // 仪表板有参数不实用缓存
            if (CollectionUtils.isNotEmpty(requestList.getFilter())
                    || CollectionUtils.isNotEmpty(requestList.getLinkageFilters())
                    || CollectionUtils.isNotEmpty(requestList.getDrill())) {
                data = datasourceProvider.getData(datasourceRequest);
            } else {
                try {
                    data = cacheViewData(datasourceProvider, datasourceRequest, id);
                } catch (Exception e) {
                    LogUtil.error(e);
                } finally {
                    // 如果当前对象被锁 且 当前线程冲入次数 > 0 则释放锁
                    if (lock.isLocked() && lock.getHoldCount() > 0) {
                        lock.unlock();
                    }
                }
            }
        }
        if (StringUtils.containsIgnoreCase(view.getType(), "pie") && data.size() > 1000) {
            data = data.subList(0, 1000);
        }

        Map<String, Object> map = new TreeMap<>();
        // 图表组件可再扩展
        Map<String, Object> mapChart;
        if (StringUtils.containsIgnoreCase(view.getType(), "stack")) {
            mapChart = transStackChartData(xAxis, yAxis, view, data, extStack, isDrill);
        } else if (StringUtils.containsIgnoreCase(view.getType(), "scatter")) {
            mapChart = transScatterData(xAxis, yAxis, view, data, extBubble, isDrill);
        } else if (StringUtils.containsIgnoreCase(view.getType(), "radar")) {
            mapChart = transRadarChartData(xAxis, yAxis, view, data, isDrill);
        } else if (StringUtils.containsIgnoreCase(view.getType(), "text")
                || StringUtils.containsIgnoreCase(view.getType(), "gauge")) {
            mapChart = transNormalChartData(xAxis, yAxis, view, data, isDrill);
        } else if (StringUtils.containsIgnoreCase(view.getType(), "chart-mix")) {
            mapChart = transMixChartData(xAxis, yAxis, view, data, isDrill);
        } else {
            mapChart = transChartData(xAxis, yAxis, view, data, isDrill);
        }
        // table组件，明细表，也用于导出数据
        Map<String, Object> mapTableNormal = transTableNormal(xAxis, yAxis, view, data, extStack);

        map.putAll(mapChart);
        map.putAll(mapTableNormal);

        List<DatasetTableField>  sourceFields = dataSetTableFieldsService.getFieldsByTableId(view.getTableId());
        map.put("sourceFields",sourceFields);

        ChartViewDTO dto = new ChartViewDTO();
        BeanUtils.copyBean(dto, view);
        dto.setData(map);
        dto.setSql(datasourceRequest.getQuery());

        dto.setDrill(isDrill);
        dto.setDrillFilters(drillFilters);
        return dto;
    }

    private boolean checkDrillExist(List<ChartViewFieldDTO> xAxis, List<ChartViewFieldDTO> extStack, ChartViewFieldDTO dto, ChartViewWithBLOBs view) {
        if (CollectionUtils.isNotEmpty(xAxis)) {
            for (ChartViewFieldDTO x : xAxis) {
                if (StringUtils.equalsIgnoreCase(x.getId(), dto.getId())) {
                    return true;
                }
            }
        }
        if (StringUtils.containsIgnoreCase(view.getType(), "stack") && CollectionUtils.isNotEmpty(extStack)) {
            for (ChartViewFieldDTO x : extStack) {
                if (StringUtils.equalsIgnoreCase(x.getId(), dto.getId())) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 避免缓存击穿
     * 虽然流量不一定能够达到击穿的水平
     *
     * @param datasourceProvider
     * @param datasourceRequest
     * @param viewId
     * @return
     * @throws Exception
     */
    public List<String[]> cacheViewData(DatasourceProvider datasourceProvider, DatasourceRequest datasourceRequest, String viewId) throws Exception {
        List<String[]> result;
        Object cache = CacheUtils.get(JdbcConstants.VIEW_CACHE_KEY, viewId);
        if (cache == null) {
            if (lock.tryLock()) {// 获取锁成功
                try {
                    result = datasourceProvider.getData(datasourceRequest);
                    if (result != null) {
                        CacheUtils.put(JdbcConstants.VIEW_CACHE_KEY, viewId, result, null, null);
                    }
                } catch (Exception e) {
                    LogUtil.error(e);
                    throw e;
                } finally {
                    lock.unlock();
                }
            } else {//获取锁失败
                Thread.sleep(100);//避免CAS自旋频率过大 占用cpu资源过高
                result = cacheViewData(datasourceProvider, datasourceRequest, viewId);
            }
        } else {
            result = (List<String[]>) cache;
        }
        return result;
    }

    // 基础图形
    private Map<String, Object> transChartData(List<ChartViewFieldDTO> xAxis, List<ChartViewFieldDTO> yAxis, ChartViewWithBLOBs view, List<String[]> data, boolean isDrill) {
        Map<String, Object> map = new HashMap<>();

        List<String> x = new ArrayList<>();
        List<Series> series = new ArrayList<>();
        for (ChartViewFieldDTO y : yAxis) {
            Series series1 = new Series();
            series1.setName(y.getName());
            series1.setType(view.getType());
            series1.setData(new ArrayList<>());
            series.add(series1);
        }
        for (int i1 = 0; i1 < data.size(); i1++) {
            String[] d = data.get(i1);

            StringBuilder a = new StringBuilder();
            for (int i = xAxis.size(); i < xAxis.size() + yAxis.size(); i++) {
                List<ChartDimensionDTO> dimensionList = new ArrayList<>();
                List<ChartQuotaDTO> quotaList = new ArrayList<>();
                AxisChartDataDTO axisChartDataDTO = new AxisChartDataDTO();

                for (int j = 0; j < xAxis.size(); j++) {
                    ChartDimensionDTO chartDimensionDTO = new ChartDimensionDTO();
                    chartDimensionDTO.setId(xAxis.get(j).getId());
                    chartDimensionDTO.setValue(d[j]);
                    dimensionList.add(chartDimensionDTO);
                }
                axisChartDataDTO.setDimensionList(dimensionList);

                int j = i - xAxis.size();
                ChartQuotaDTO chartQuotaDTO = new ChartQuotaDTO();
                chartQuotaDTO.setId(yAxis.get(j).getId());
                quotaList.add(chartQuotaDTO);
                axisChartDataDTO.setQuotaList(quotaList);
                try {
                    axisChartDataDTO.setValue(new BigDecimal(StringUtils.isEmpty(d[i]) ? "0" : d[i]));
                } catch (Exception e) {
                    axisChartDataDTO.setValue(new BigDecimal(0));
                }
                series.get(j).getData().add(axisChartDataDTO);
            }
            if (isDrill) {
                a.append(d[xAxis.size() - 1]);
            } else {
                for (int i = 0; i < xAxis.size(); i++) {
                    if (i == xAxis.size() - 1) {
                        a.append(d[i]);
                    } else {
                        a.append(d[i]).append("\n");
                    }
                }
            }
            x.add(a.toString());
        }

        map.put("x", x);
        map.put("series", series);
        return map;
    }

    // 组合图形
    private Map<String, Object> transMixChartData(List<ChartViewFieldDTO> xAxis, List<ChartViewFieldDTO> yAxis, ChartViewWithBLOBs view, List<String[]> data, boolean isDrill) {
        Map<String, Object> map = new HashMap<>();

        List<String> x = new ArrayList<>();
        List<Series> series = new ArrayList<>();
        for (ChartViewFieldDTO y : yAxis) {
            Series series1 = new Series();
            series1.setName(y.getName());
            series1.setType(y.getChartType());
            series1.setData(new ArrayList<>());
            series.add(series1);
        }
        for (int i1 = 0; i1 < data.size(); i1++) {
            String[] d = data.get(i1);

            StringBuilder a = new StringBuilder();
            for (int i = xAxis.size(); i < xAxis.size() + yAxis.size(); i++) {
                List<ChartDimensionDTO> dimensionList = new ArrayList<>();
                List<ChartQuotaDTO> quotaList = new ArrayList<>();
                AxisChartDataDTO axisChartDataDTO = new AxisChartDataDTO();

                for (int j = 0; j < xAxis.size(); j++) {
                    ChartDimensionDTO chartDimensionDTO = new ChartDimensionDTO();
                    chartDimensionDTO.setId(xAxis.get(j).getId());
                    chartDimensionDTO.setValue(d[j]);
                    dimensionList.add(chartDimensionDTO);
                }
                axisChartDataDTO.setDimensionList(dimensionList);

                int j = i - xAxis.size();
                ChartQuotaDTO chartQuotaDTO = new ChartQuotaDTO();
                chartQuotaDTO.setId(yAxis.get(j).getId());
                quotaList.add(chartQuotaDTO);
                axisChartDataDTO.setQuotaList(quotaList);
                try {
                    axisChartDataDTO.setValue(new BigDecimal(StringUtils.isEmpty(d[i]) ? "0" : d[i]));
                } catch (Exception e) {
                    axisChartDataDTO.setValue(new BigDecimal(0));
                }
                series.get(j).getData().add(axisChartDataDTO);
            }
            if (isDrill) {
                a.append(d[xAxis.size() - 1]);
            } else {
                for (int i = 0; i < xAxis.size(); i++) {
                    if (i == xAxis.size() - 1) {
                        a.append(d[i]);
                    } else {
                        a.append(d[i]).append("\n");
                    }
                }
            }
            x.add(a.toString());
        }

        map.put("x", x);
        map.put("series", series);
        return map;
    }

    // 常规图形
    private Map<String, Object> transNormalChartData(List<ChartViewFieldDTO> xAxis, List<ChartViewFieldDTO> yAxis, ChartViewWithBLOBs view, List<String[]> data, boolean isDrill) {
        Map<String, Object> map = new HashMap<>();

        List<String> x = new ArrayList<>();
        List<Series> series = new ArrayList<>();
        for (ChartViewFieldDTO y : yAxis) {
            Series series1 = new Series();
            series1.setName(y.getName());
            series1.setType(view.getType());
            series1.setData(new ArrayList<>());
            series.add(series1);
        }
        for (String[] d : data) {
            StringBuilder a = new StringBuilder();
            if (isDrill) {
                a.append(d[xAxis.size() - 1]);
            } else {
                for (int i = 0; i < xAxis.size(); i++) {
                    if (i == xAxis.size() - 1) {
                        a.append(d[i]);
                    } else {
                        a.append(d[i]).append("\n");
                    }
                }
            }
            x.add(a.toString());
            for (int i = xAxis.size(); i < xAxis.size() + yAxis.size(); i++) {
                int j = i - xAxis.size();
                try {
                    series.get(j).getData().add(new BigDecimal(StringUtils.isEmpty(d[i]) ? "0" : d[i]));
                } catch (Exception e) {
                    series.get(j).getData().add(new BigDecimal(0));
                }
            }
        }

        map.put("x", x);
        map.put("series", series);
        return map;
    }

    // radar图
    private Map<String, Object> transRadarChartData(List<ChartViewFieldDTO> xAxis, List<ChartViewFieldDTO> yAxis, ChartViewWithBLOBs view, List<String[]> data, boolean isDrill) {
        Map<String, Object> map = new HashMap<>();

        List<String> x = new ArrayList<>();
        List<Series> series = new ArrayList<>();
        for (ChartViewFieldDTO y : yAxis) {
            Series series1 = new Series();
            series1.setName(y.getName());
            series1.setType(view.getType());
            series1.setData(new ArrayList<>());
            series.add(series1);
        }
        for (String[] d : data) {
            StringBuilder a = new StringBuilder();
            if (isDrill) {
                a.append(d[xAxis.size() - 1]);
            } else {
                for (int i = 0; i < xAxis.size(); i++) {
                    if (i == xAxis.size() - 1) {
                        a.append(d[i]);
                    } else {
                        a.append(d[i]).append("\n");
                    }
                }
            }
            x.add(a.toString());
            for (int i = xAxis.size(); i < xAxis.size() + yAxis.size(); i++) {
                int j = i - xAxis.size();
                try {
                    series.get(j).getData().add(new BigDecimal(StringUtils.isEmpty(d[i]) ? "0" : d[i]));
                } catch (Exception e) {
                    series.get(j).getData().add(new BigDecimal(0));
                }
            }
        }

        map.put("x", x);
        map.put("series", series);
        return map;
    }

    // 堆叠图
    private Map<String, Object> transStackChartData(List<ChartViewFieldDTO> xAxis, List<ChartViewFieldDTO> yAxis, ChartViewWithBLOBs view, List<String[]> data, List<ChartViewFieldDTO> extStack, boolean isDrill) {
        Map<String, Object> map = new HashMap<>();

        List<String> x = new ArrayList<>();
        List<String> stack = new ArrayList<>();
        List<Series> series = new ArrayList<>();

        if (CollectionUtils.isNotEmpty(extStack)) {
            AxisChartDataDTO defaultAxisChartDataDTO = new AxisChartDataDTO();
            BigDecimal defaultValue = StringUtils.containsIgnoreCase(view.getType(), "line") ? new BigDecimal(0) : null;
            defaultAxisChartDataDTO.setValue(defaultValue);
            // 构建横轴
            for (String[] d : data) {
                StringBuilder a = new StringBuilder();
                if (isDrill) {
                    a.append(d[xAxis.size() - 1]);
                } else {
                    for (int i = 0; i < xAxis.size(); i++) {
                        if (i == xAxis.size() - 1) {
                            a.append(d[i]);
                        } else {
                            a.append(d[i]).append("\n");
                        }
                    }
                }
                x.add(a.toString());
            }
            x = x.stream().distinct().collect(Collectors.toList());
            // 构建堆叠
            for (String[] d : data) {
                stack.add(d[xAxis.size()]);
            }
            stack = stack.stream().distinct().collect(Collectors.toList());
            for (String s : stack) {
                Series series1 = new Series();
                series1.setName(s);
                series1.setType(view.getType());
                List<Object> list = new ArrayList<>();
                for (int i = 0; i < x.size(); i++) {
                    list.add(defaultAxisChartDataDTO);
                }
                series1.setData(list);
                series.add(series1);
            }
            for (Series ss : series) {
                for (int i = 0; i < x.size(); i++) {
                    for (String[] row : data) {
                        String stackColumn = row[xAxis.size()];
                        if (StringUtils.equals(ss.getName(), stackColumn)) {
                            StringBuilder a = new StringBuilder();
                            if (isDrill) {
                                a.append(row[xAxis.size() - 1]);
                            } else {
                                for (int j = 0; j < xAxis.size(); j++) {
                                    if (j == xAxis.size() - 1) {
                                        a.append(row[j]);
                                    } else {
                                        a.append(row[j]).append("\n");
                                    }
                                }
                            }
                            if (StringUtils.equals(a.toString(), x.get(i))) {
                                if (row.length > xAxis.size() + extStack.size()) {
                                    List<ChartDimensionDTO> dimensionList = new ArrayList<>();
                                    List<ChartQuotaDTO> quotaList = new ArrayList<>();
                                    AxisChartDataDTO axisChartDataDTO = new AxisChartDataDTO();

                                    ChartQuotaDTO chartQuotaDTO = new ChartQuotaDTO();
                                    chartQuotaDTO.setId(yAxis.get(0).getId());
                                    quotaList.add(chartQuotaDTO);
                                    axisChartDataDTO.setQuotaList(quotaList);

                                    for (int k = 0; k < xAxis.size(); k++) {
                                        ChartDimensionDTO chartDimensionDTO = new ChartDimensionDTO();
                                        chartDimensionDTO.setId(xAxis.get(k).getId());
                                        chartDimensionDTO.setValue(row[k]);
                                        dimensionList.add(chartDimensionDTO);
                                    }
                                    ChartDimensionDTO chartDimensionDTO = new ChartDimensionDTO();
                                    chartDimensionDTO.setId(extStack.get(0).getId());
                                    chartDimensionDTO.setValue(row[xAxis.size()]);
                                    dimensionList.add(chartDimensionDTO);
                                    axisChartDataDTO.setDimensionList(dimensionList);

                                    String s = row[xAxis.size() + extStack.size()];
                                    if (StringUtils.isNotEmpty(s)) {
                                        axisChartDataDTO.setValue(new BigDecimal(s));
                                        ss.getData().set(i, axisChartDataDTO);
                                    }
                                }
                                break;
                            }
                        }
                    }
                }
            }
        } else {
            for (ChartViewFieldDTO y : yAxis) {
                Series series1 = new Series();
                series1.setName(y.getName());
                series1.setType(view.getType());
                series1.setData(new ArrayList<>());
                series.add(series1);
            }
            for (int i1 = 0; i1 < data.size(); i1++) {
                String[] d = data.get(i1);

                StringBuilder a = new StringBuilder();
                for (int i = xAxis.size(); i < xAxis.size() + yAxis.size(); i++) {
                    List<ChartDimensionDTO> dimensionList = new ArrayList<>();
                    List<ChartQuotaDTO> quotaList = new ArrayList<>();
                    AxisChartDataDTO axisChartDataDTO = new AxisChartDataDTO();

                    for (int j = 0; j < xAxis.size(); j++) {
                        ChartDimensionDTO chartDimensionDTO = new ChartDimensionDTO();
                        chartDimensionDTO.setId(xAxis.get(j).getId());
                        chartDimensionDTO.setValue(d[j]);
                        dimensionList.add(chartDimensionDTO);
                    }
                    axisChartDataDTO.setDimensionList(dimensionList);

                    int j = i - xAxis.size();
                    ChartQuotaDTO chartQuotaDTO = new ChartQuotaDTO();
                    chartQuotaDTO.setId(yAxis.get(j).getId());
                    quotaList.add(chartQuotaDTO);
                    axisChartDataDTO.setQuotaList(quotaList);
                    try {
                        axisChartDataDTO.setValue(new BigDecimal(StringUtils.isEmpty(d[i]) ? "0" : d[i]));
                    } catch (Exception e) {
                        axisChartDataDTO.setValue(new BigDecimal(0));
                    }
                    series.get(j).getData().add(axisChartDataDTO);
                }
                if (isDrill) {
                    a.append(d[xAxis.size() - 1]);
                } else {
                    for (int i = 0; i < xAxis.size(); i++) {
                        if (i == xAxis.size() - 1) {
                            a.append(d[i]);
                        } else {
                            a.append(d[i]).append("\n");
                        }
                    }
                }
                x.add(a.toString());
            }
        }

        map.put("x", x);
        map.put("series", series);
        return map;
    }

    // 散点图
    private Map<String, Object> transScatterData(List<ChartViewFieldDTO> xAxis, List<ChartViewFieldDTO> yAxis, ChartViewWithBLOBs view, List<String[]> data, List<ChartViewFieldDTO> extBubble, boolean isDrill) {
        Map<String, Object> map = new HashMap<>();

        List<String> x = new ArrayList<>();
        List<Series> series = new ArrayList<>();
        for (ChartViewFieldDTO y : yAxis) {
            Series series1 = new Series();
            series1.setName(y.getName());
            series1.setType(view.getType());
            series1.setData(new ArrayList<>());
            series.add(series1);
        }
        for (int i1 = 0; i1 < data.size(); i1++) {
            String[] d = data.get(i1);

            StringBuilder a = new StringBuilder();
            if (isDrill) {
                a.append(d[xAxis.size() - 1]);
            } else {
                for (int i = 0; i < xAxis.size(); i++) {
                    if (i == xAxis.size() - 1) {
                        a.append(d[i]);
                    } else {
                        a.append(d[i]).append("\n");
                    }
                }
            }
            x.add(a.toString());
            for (int i = xAxis.size(); i < xAxis.size() + yAxis.size(); i++) {
                List<ChartDimensionDTO> dimensionList = new ArrayList<>();
                List<ChartQuotaDTO> quotaList = new ArrayList<>();
                ScatterChartDataDTO scatterChartDataDTO = new ScatterChartDataDTO();

                for (int j = 0; j < xAxis.size(); j++) {
                    ChartDimensionDTO chartDimensionDTO = new ChartDimensionDTO();
                    chartDimensionDTO.setId(xAxis.get(j).getId());
                    chartDimensionDTO.setValue(d[j]);
                    dimensionList.add(chartDimensionDTO);
                }
                scatterChartDataDTO.setDimensionList(dimensionList);

                int j = i - xAxis.size();
                ChartQuotaDTO chartQuotaDTO = new ChartQuotaDTO();
                chartQuotaDTO.setId(yAxis.get(j).getId());
                quotaList.add(chartQuotaDTO);
                scatterChartDataDTO.setQuotaList(quotaList);
//                try {
//                    axisChartDataDTO.setValue(new BigDecimal(StringUtils.isEmpty(d[i]) ? "0" : d[i]));
//                } catch (Exception e) {
//                    axisChartDataDTO.setValue(new BigDecimal(0));
//                }
                if (CollectionUtils.isNotEmpty(extBubble) && extBubble.size() > 0) {
                    try {
                        scatterChartDataDTO.setValue(new Object[]{
                                a.toString(),
                                new BigDecimal(StringUtils.isEmpty(d[i]) ? "0" : d[i]),
                                new BigDecimal(StringUtils.isEmpty(d[xAxis.size() + yAxis.size()]) ? "0" : d[xAxis.size() + yAxis.size()])
                        });
                    } catch (Exception e) {
                        scatterChartDataDTO.setValue(new Object[]{a.toString(), new BigDecimal(0), new BigDecimal(0)});
                    }
                } else {
                    try {
                        scatterChartDataDTO.setValue(new Object[]{
                                a.toString(),
                                new BigDecimal(StringUtils.isEmpty(d[i]) ? "0" : d[i])
                        });
                    } catch (Exception e) {
                        scatterChartDataDTO.setValue(new Object[]{a.toString(), new BigDecimal(0)});
                    }
                }
                series.get(j).getData().add(scatterChartDataDTO);
            }
        }

        /*for (String[] d : data) {
            StringBuilder a = new StringBuilder();
            for (int i = 0; i < xAxis.size(); i++) {
                if (i == xAxis.size() - 1) {
                    a.append(d[i]);
                } else {
                    a.append(d[i]).append("\n");
                }
            }
            x.add(a.toString());
            for (int i = xAxis.size(); i < xAxis.size() + yAxis.size(); i++) {
                int j = i - xAxis.size();
                if (CollectionUtils.isNotEmpty(extBubble) && extBubble.size() > 0) {
                    try {
                        series.get(j).getData().add(new Object[]{
                                a.toString(),
                                new BigDecimal(StringUtils.isEmpty(d[i]) ? "0" : d[i]),
                                new BigDecimal(StringUtils.isEmpty(d[xAxis.size() + yAxis.size()]) ? "0" : d[xAxis.size() + yAxis.size()])
                        });
                    } catch (Exception e) {
                        series.get(j).getData().add(new Object[]{a.toString(), new BigDecimal(0), new BigDecimal(0)});
                    }
                } else {
                    try {
                        series.get(j).getData().add(new Object[]{
                                a.toString(),
                                new BigDecimal(StringUtils.isEmpty(d[i]) ? "0" : d[i])
                        });
                    } catch (Exception e) {
                        series.get(j).getData().add(new Object[]{a.toString(), new BigDecimal(0)});
                    }
                }
            }
        }*/

        map.put("x", x);
        map.put("series", series);
        return map;
    }

    // 表格
    private Map<String, Object> transTableNormal(List<ChartViewFieldDTO> xAxis, List<ChartViewFieldDTO> yAxis, ChartViewWithBLOBs view, List<String[]> data, List<ChartViewFieldDTO> extStack) {
        Map<String, Object> map = new TreeMap<>();
        List<ChartViewFieldDTO> fields = new ArrayList<>();
        List<Map<String, Object>> tableRow = new ArrayList<>();
        if (ObjectUtils.isNotEmpty(xAxis)) {
            fields.addAll(xAxis);
        }
        if (StringUtils.containsIgnoreCase(view.getType(), "stack")) {
            if (ObjectUtils.isNotEmpty(extStack)) {
                fields.addAll(extStack);
            }
        }
        fields.addAll(yAxis);
        data.forEach(ele -> {
            Map<String, Object> d = new HashMap<>();
            for (int i = 0; i < fields.size(); i++) {
                ChartViewFieldDTO chartViewFieldDTO = fields.get(i);
                if (chartViewFieldDTO.getDeType() == 0 || chartViewFieldDTO.getDeType() == 1) {
                    d.put(fields.get(i).getDataeaseName(), StringUtils.isEmpty(ele[i]) ? "" : ele[i]);
                } else if (chartViewFieldDTO.getDeType() == 2 || chartViewFieldDTO.getDeType() == 3) {
                    d.put(fields.get(i).getDataeaseName(), new BigDecimal(StringUtils.isEmpty(ele[i]) ? "0" : ele[i]).setScale(2, RoundingMode.HALF_UP));
                }
            }
            tableRow.add(d);
        });
        map.put("fields", fields);
        map.put("tableRow", tableRow);
        return map;
    }

    private void checkName(ChartViewWithBLOBs chartView) {
//        if (StringUtils.isEmpty(chartView.getId())) {
//            return;
//        }
        ChartViewExample chartViewExample = new ChartViewExample();
        ChartViewExample.Criteria criteria = chartViewExample.createCriteria();
        if (StringUtils.isNotEmpty(chartView.getId())) {
            criteria.andIdNotEqualTo(chartView.getId());
        }
        if (StringUtils.isNotEmpty(chartView.getSceneId())) {
            criteria.andSceneIdEqualTo(chartView.getSceneId());
        }
        if (StringUtils.isNotEmpty(chartView.getName())) {
            criteria.andNameEqualTo(chartView.getName());
        }
        List<ChartViewWithBLOBs> list = chartViewMapper.selectByExampleWithBLOBs(chartViewExample);
        if (list.size() > 0) {
            throw new RuntimeException(Translator.get("i18n_name_cant_repeat_same_group"));
        }
    }

    public Map<String, Object> getChartDetail(String id) {
        Map<String, Object> map = new HashMap<>();
        ChartViewWithBLOBs chartViewWithBLOBs = chartViewMapper.selectByPrimaryKey(id);
        map.put("chart", chartViewWithBLOBs);
        if (ObjectUtils.isNotEmpty(chartViewWithBLOBs)) {
            Map<String, Object> datasetDetail = dataSetTableService.getDatasetDetail(chartViewWithBLOBs.getTableId());
            map.putAll(datasetDetail);
        }
        return map;
    }

    public List<ChartView> viewsByIds(List<String> viewIds) {
        ChartViewExample example = new ChartViewExample();
        example.createCriteria().andIdIn(viewIds);
        return chartViewMapper.selectByExample(example);
    }

    public ChartViewWithBLOBs findOne(String id) {
        return chartViewMapper.selectByPrimaryKey(id);
    }

    public String chartCopy(String id) {
        String newChartId = UUID.randomUUID().toString();
        extChartViewMapper.chartCopy(newChartId, id);
        return newChartId;
    }

    public String searchAdviceSceneId(String panelId) {
        return extChartViewMapper.searchAdviceSceneId(AuthUtils.getUser().getUserId().toString(), panelId);
    }
}
