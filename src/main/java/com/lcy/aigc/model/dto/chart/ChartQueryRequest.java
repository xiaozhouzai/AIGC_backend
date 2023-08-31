package com.lcy.aigc.model.dto.chart;

import com.lcy.aigc.common.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

@EqualsAndHashCode(callSuper = true)
@Data
public class ChartQueryRequest extends PageRequest implements Serializable {
    private static final long serialVersionUID = 2530648364261243434L;
    private String chartType;
    private String goal;
    private Long id;
    private String name;
    private String optMessage;
    private Integer status;
    private Long userId;
}
