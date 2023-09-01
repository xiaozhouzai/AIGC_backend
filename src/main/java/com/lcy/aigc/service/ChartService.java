package com.lcy.aigc.service;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.IService;
import com.lcy.aigc.model.dto.chart.ChartQueryRequest;
import com.lcy.aigc.model.entity.Chart;

import java.util.List;
import java.util.Map;

/**
* @author lcyzh
* @description 针对表【chart(图表信息表)】的数据库操作Service
* @createDate 2023-08-16 19:40:03
*/
public interface ChartService extends IService<Chart> {

    QueryWrapper<Chart> getQueryWrapper(ChartQueryRequest chartQueryRequest);

    void createChartDataTable(String querySql);

    List<String> getTableColumns(String tableName);

    void insertChartTable(String sql);

    List<Map<Integer,String>>  selectChartData(String sql);
}
