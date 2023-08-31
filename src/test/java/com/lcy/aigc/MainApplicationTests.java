package com.lcy.aigc;

import com.lcy.aigc.config.WxOpenConfig;
import com.lcy.aigc.service.ChartService;

import javax.annotation.Resource;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.Map;

/**
 * 主类测试
*
 */
@SpringBootTest
class MainApplicationTests {

    @Resource
    private WxOpenConfig wxOpenConfig;
    @Resource
    private ChartService chartService;

    @Test
    void contextLoads() {

        String data = "星期,用户数\n"+"星期四,225\n"+"星期三,600\n"+"星期六,800";
        String[] split1 = data.split("\n");
        String[] split2 = split1[0].split(",");
        //获取长度，循环写拼接sql
        int length = split2.length;
        String number = "11156465149";
        StringBuilder stringBuilder = new StringBuilder();
        String sql1 = "create table chartData_" +number+
                "(id  bigint auto_increment  primary key,";
        stringBuilder.append(sql1);
        for (int i1 = 0; i1 < length; i1++) {
            if (i1 == length - 1){
                stringBuilder.append(split2[i1]).append(" varchar(128) null");
            } else {
                stringBuilder.append(split2[i1]).append(" varchar(128) null,");
            }
        }
        stringBuilder.append(")");
        //拼接完成
        String sql = stringBuilder.toString();
        //执行建表语句
        chartService.createChartDataTable(sql);
    }

    @Test
    void testInsert() {
        String sql = "insert into chartdata_11156465149 (星期, 用户数) VALUES ('周一','600')";
        chartService.insertChartTable(sql);
    }
    @Test
    void test1() {
        String s = "'2021/22','2222','888','6666'";
        String[] split = s.split(",");
        System.out.println(split);
    }



}
