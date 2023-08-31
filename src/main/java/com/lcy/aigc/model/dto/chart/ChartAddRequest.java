package com.lcy.aigc.model.dto.chart;

import lombok.Data;

import java.io.Serializable;


@Data
public class ChartAddRequest implements Serializable {
    private static final long serialVersionUID = -3590622193823691470L;
    private String chartData;
    private String chartType;
    private String goal;
    private String name;
}
