package com.lcy.aigc.model.dto.chart;

import lombok.Data;

import java.io.Serializable;

@Data
public class ChartEditRequest implements Serializable {

    private static final long serialVersionUID = 7365169664704070067L;
    private String chartData;
    private String chartType;
    private String goal;
    private Long id;
    private String name;
}
