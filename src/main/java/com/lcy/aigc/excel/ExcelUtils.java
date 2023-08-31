package com.lcy.aigc.excel;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.support.ExcelTypeEnum;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ExcelUtils {
    public static String excelToCsv(MultipartFile multipartFile) {
        List<Map<Integer, String>> list = null;
        try {
            list = EasyExcel.read(multipartFile.getInputStream())
                    .excelType(ExcelTypeEnum.XLSX)
                    .sheet()
                    .headRowNumber(0)
                    .doReadSync();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        //拼接
        StringBuilder builder = new StringBuilder();
        //表头
        LinkedHashMap<Integer,String> linkedHashMap = (LinkedHashMap<Integer,String>) list.get(0);
        //过滤
        List<String> collect = linkedHashMap.values().stream().map(item ->{
            //如果为null把它设置为空字符串
            if (item == null){
                item = "0";
            }
            return item;
        }).collect(Collectors.toList());
        String join = StringUtils.join(collect, ","); // 用户数,月份,点赞数
        builder.append(join).append("\n");
        //数据部分,索引从1开始
        for (int i = 1; i < list.size(); i++) {
            LinkedHashMap<Integer,String> dataMap = (LinkedHashMap<Integer,String>) list.get(i);
            List<String> collect1 = dataMap.values().stream().map(item ->{
                //如果为null把它设置为空字符串或者0，看业务，一般不会出现空
                if (item == null){
                    item = "";
                }
                return item;
            }).collect(Collectors.toList());
            String one = StringUtils.join(collect1, ",");
            builder.append(one).append("\n");
        }
        return builder.toString();
    }
}
