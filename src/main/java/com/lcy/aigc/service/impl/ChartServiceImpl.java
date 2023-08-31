package com.lcy.aigc.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lcy.aigc.common.ErrorCode;
import com.lcy.aigc.constant.CommonConstant;
import com.lcy.aigc.exception.BusinessException;
import com.lcy.aigc.mapper.ChartMapper;
import com.lcy.aigc.model.dto.chart.ChartQueryRequest;
import com.lcy.aigc.model.entity.Chart;
import com.lcy.aigc.model.entity.UserBlog;
import com.lcy.aigc.service.ChartService;
import com.lcy.aigc.utils.SqlUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
@Service
public class ChartServiceImpl extends ServiceImpl<ChartMapper, Chart> implements ChartService {

    @Resource
    private ChartMapper chartMapper;
    @Override
    public QueryWrapper<Chart> getQueryWrapper(ChartQueryRequest chartQueryRequest) {
        if (chartQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请求参数为空");
        }
        String chartType = chartQueryRequest.getChartType();
        String goal = chartQueryRequest.getGoal();
        Long id = chartQueryRequest.getId();
        String name = chartQueryRequest.getName();
        String optMessage = chartQueryRequest.getOptMessage();
        Integer status = chartQueryRequest.getStatus();
        Long userId = chartQueryRequest.getUserId();
        String sortField = chartQueryRequest.getSortField();
        String sortOrder = chartQueryRequest.getSortOrder();
                QueryWrapper<Chart> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq(id != null, "id", id);
        queryWrapper.eq(userId != null, "userId", userId);
        queryWrapper.eq(status != null, "status", status);
        queryWrapper.like(StringUtils.isNotBlank(chartType), "chartType", chartType);
        queryWrapper.like(StringUtils.isNotBlank(goal), "goal", goal);
        queryWrapper.like(StringUtils.isNotBlank(name), "name", name);
        queryWrapper.like(StringUtils.isNotBlank(optMessage), "optMessage", optMessage);
        queryWrapper.orderBy(SqlUtils.validSortField(sortField), sortOrder.equals(CommonConstant.SORT_ORDER_ASC),
                sortField);
        return queryWrapper;
    }

    @Override
    public void createChartDataTable(String querySql) {
        chartMapper.createChartDataTable(querySql);
    }

    @Override
    public List<String> getTableColumns(String tableName) {
        return chartMapper.getTableColumns(tableName);
    }

    @Override
    public void insertChartTable(String sql) {
        chartMapper.insertChartTable(sql);
    }

}
