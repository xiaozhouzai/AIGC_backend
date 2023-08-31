package com.lcy.aigc.model.dto.chart;

import lombok.Data;

import java.io.Serializable;
@Data
public class ChartUpdateRequest implements Serializable {

    private static final long serialVersionUID = 2652129249157589734L;
    private String chartData;
    private String chartType;
    private String goal;
    private Long id;
    private String name;
    private String optMessage;
    private Integer status;
}
