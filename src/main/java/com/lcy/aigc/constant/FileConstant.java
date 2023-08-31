package com.lcy.aigc.constant;

import java.util.Arrays;
import java.util.List;

/**
 * 文件常量
*
 */
public interface FileConstant {

    /**
     * COS 访问地址
     * todo 需替换配置
     */
    String COS_HOST = "https://yupi.icu";
    long FILE_MAX_SIZE = 1024*1024*10;

    List<String> FILE_SUFFIX_LIST = Arrays.asList("jpg","png","svg","xlsx","pdf","css","js","xml","gif","doc","txt","mp3");

    String EXCEL_SUFFIX = "xlsx";


}
