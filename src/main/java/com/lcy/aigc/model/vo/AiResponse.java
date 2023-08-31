package com.lcy.aigc.model.vo;

import lombok.Data;

import java.io.Serializable;
@Data
public class AiResponse implements Serializable {
    private static final long serialVersionUID = -8111133133241677638L;
    String genChart;
    String genResult;
}
