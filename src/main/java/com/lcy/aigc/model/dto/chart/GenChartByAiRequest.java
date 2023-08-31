package com.lcy.aigc.model.dto.chart;

import lombok.Data;

import java.io.Serializable;

@Data
public class GenChartByAiRequest implements Serializable {
    private static final long serialVersionUID = 7367988785851827739L;
    private String name;
    private String goal;
    private String chartType;

}
