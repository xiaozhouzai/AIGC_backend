package com.lcy.aigc.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lcy.aigc.model.entity.Chart;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
* @author lcyzh
* @description 针对表【chart(图表信息表)】的数据库操作Mapper
* @createDate 2023-08-16 19:40:03
* @Entity com.lcy.aigc.model.entity.chart
*/

@Mapper
public interface ChartMapper extends BaseMapper<Chart> {

    void createChartDataTable(String querySql);

    /**
     * 按照存入表的顺序，获取有序的字段
     * @param tableName
     * @return
     */
    @Select("SELECT COLUMN_NAME FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = #{tableName} ORDER BY ORDINAL_POSITION")
    List<String> getTableColumns(@Param("tableName") String tableName);

    void insertChartTable(String sql);




}




