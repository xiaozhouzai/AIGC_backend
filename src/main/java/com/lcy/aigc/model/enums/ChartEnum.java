package com.lcy.aigc.model.enums;

import org.apache.commons.lang3.ObjectUtils;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public enum ChartEnum {
    WAIT(0,"wait"),
    RUNNING(1,"running"),
    SUCCEED(2,"succeed"),
    FAILED(3,"failed");


    private final Integer key;




    private final String value;


    ChartEnum(Integer key,String value) {
        this.key=key;
        this.value = value;
    }

    /**
     * 获取值列表
     *
     * @return
     */
    public static List<String> getValues() {
        return Arrays.stream(values()).map(item -> item.value).collect(Collectors.toList());
    }

    /**
     * 根据 value 获取枚举
     *
     * @param value
     * @return
     */
    public static ChartEnum getEnumByValue(String value) {
        if (ObjectUtils.isEmpty(value)) {
            return null;
        }
        for (ChartEnum anEnum : ChartEnum.values()) {
            if (anEnum.value.equals(value)) {
                return anEnum;
            }
        }
        return null;
    }
    public Integer getKey() {
        return key;
    }

    public String getValue() {
        return value;
    }
}
