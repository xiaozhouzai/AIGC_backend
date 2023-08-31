package com.lcy.aigc.controller;

import cn.hutool.core.io.FileUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lcy.aigc.annotation.AuthCheck;
import com.lcy.aigc.common.BaseResponse;
import com.lcy.aigc.common.DeleteRequest;
import com.lcy.aigc.common.ErrorCode;
import com.lcy.aigc.common.ResultUtils;
import com.lcy.aigc.constant.FileConstant;
import com.lcy.aigc.constant.UserConstant;
import com.lcy.aigc.excel.ExcelUtils;
import com.lcy.aigc.exception.BusinessException;
import com.lcy.aigc.exception.ThrowUtils;
import com.lcy.aigc.manager.RedisLimiterManager;
import com.lcy.aigc.model.dto.chart.*;
import com.lcy.aigc.model.entity.Chart;
import com.lcy.aigc.model.entity.User;
import com.lcy.aigc.model.enums.ChartEnum;
import com.lcy.aigc.service.ChartService;
import com.lcy.aigc.service.UserService;
import com.lcy.aigc.manager.AiManager;
import com.lcy.aigc.model.dto.chart.*;
import com.lcy.aigc.model.vo.AiResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * 帖子接口
 */
@RestController
@RequestMapping("/chart")
@Slf4j
public class ChartController {

    @Resource
    private ChartService chartService;

    @Resource
    private UserService userService;
    @Resource
    private AiManager aiManager;

    @Resource
    private RedisLimiterManager limiterManager;

    @Resource
    private ThreadPoolExecutor poolExecutor;

    /**
     * 创建
     *
     * @param chartAddRequest
     * @param request
     * @return
     */
    @PostMapping("/add")
    public BaseResponse<Long> addChart(@RequestBody ChartAddRequest chartAddRequest, HttpServletRequest request) {
        if (chartAddRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Chart chart = new Chart();
        BeanUtils.copyProperties(chartAddRequest, chart);
        User loginUser = userService.getLoginUser(request);
        chart.setUserId(loginUser.getId());
        boolean result = chartService.save(chart);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        long newChartId = chart.getId();
        return ResultUtils.success(newChartId);
    }

    /**
     * 删除
     *
     * @param deleteRequest
     * @param request
     * @return
     */
    @PostMapping("/delete")
    public BaseResponse<Boolean> deleteChart(@RequestBody DeleteRequest deleteRequest, HttpServletRequest request) {
        if (deleteRequest == null || deleteRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User user = userService.getLoginUser(request);
        long id = deleteRequest.getId();
        // 判断是否存在
        Chart oldchart = chartService.getById(id);
        ThrowUtils.throwIf(oldchart == null, ErrorCode.NOT_FOUND_ERROR);
        // 仅本人或管理员可删除
        if (!oldchart.getUserId().equals(user.getId()) && !userService.isAdmin(request)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        boolean b = chartService.removeById(id);
        return ResultUtils.success(b);
    }

    /**
     * 更新（仅管理员）
     *
     * @param chartUpdateRequest
     * @return
     */
    @PostMapping("/update")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> updateChart(@RequestBody ChartUpdateRequest chartUpdateRequest) {
        if (chartUpdateRequest == null || chartUpdateRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Chart chart = new Chart();
        BeanUtils.copyProperties(chartUpdateRequest, chart);
        long id = chartUpdateRequest.getId();
        // 判断是否存在
        Chart oldchart = chartService.getById(id);
        ThrowUtils.throwIf(oldchart == null, ErrorCode.NOT_FOUND_ERROR);
        boolean result = chartService.updateById(chart);
        return ResultUtils.success(result);
    }


    /**
     * 根据 id 获取
     *
     * @param id
     * @return
     */
    @GetMapping("/get")
    public BaseResponse<Chart> getChartById(Long id) {
        if (id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Chart chart = chartService.getById(id);
        if (chart == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }
        return ResultUtils.success(chart);
    }

    /**
     * 分页获取列表（封装类）
     *
     * @param chartQueryRequest
     * @return
     */
    @PostMapping("/list/page")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Page<Chart>> listChartByPage(@RequestBody ChartQueryRequest chartQueryRequest) {
        long current = chartQueryRequest.getCurrent();
        long size = chartQueryRequest.getPageSize();
        // 限制爬虫
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
        Page<Chart> chartPage = chartService.page(new Page<>(current, size),
                chartService.getQueryWrapper(chartQueryRequest));
        return ResultUtils.success(chartPage);
    }

    /**
     * 分页获取当前用户生成的图表资源列表
     *
     * @param chartQueryRequest
     * @param request
     * @return
     */
    @PostMapping("/my/list/page")
    public BaseResponse<Page<Chart>> listMyChartByPage(@RequestBody ChartQueryRequest chartQueryRequest,
                                                       HttpServletRequest request) {
        if (chartQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        Long userId = loginUser.getId();
        chartQueryRequest.setUserId(userId);
        long current = chartQueryRequest.getCurrent();
        long size = chartQueryRequest.getPageSize();
        // 限制爬虫
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
        QueryWrapper<Chart> queryWrapper = chartService.getQueryWrapper(chartQueryRequest);
        Page<Chart> chartPage = chartService.page(new Page<>(current, size), queryWrapper);
        return ResultUtils.success(chartPage);
    }

    // endregion
    /**
     * 编辑（用户）
     *
     * @param chartEditRequest
     * @param request
     * @return
     */
    @PostMapping("/edit")
    public BaseResponse<Boolean> editChart(@RequestBody ChartEditRequest chartEditRequest, HttpServletRequest request) {
        if (chartEditRequest == null || chartEditRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Chart chart = new Chart();
        BeanUtils.copyProperties(chartEditRequest, chart);

        User loginUser = userService.getLoginUser(request);
        long id = chartEditRequest.getId();
        // 判断是否存在
        Chart oldchart = chartService.getById(id);
        ThrowUtils.throwIf(oldchart == null, ErrorCode.NOT_FOUND_ERROR);
        // 仅本人或管理员可编辑
        if (!oldchart.getUserId().equals(loginUser.getId()) && !userService.isAdmin(loginUser)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        boolean result = chartService.updateById(chart);
        return ResultUtils.success(result);
    }


    /**
     * 这是静态的图标，如果你想拓展，你可以再让ai生成函数function,返回类型再加一个字段,把生成的函数代码再设置到function中
     * 单机同步生成图表
     * @param file
     * @param genChartByAiRequest
     * @param request
     * @return
     */
    @PostMapping("/gen")
    public BaseResponse<AiResponse> genChartByAi(@RequestPart("file") MultipartFile file,
                                                 GenChartByAiRequest genChartByAiRequest, HttpServletRequest request) {
        String name = genChartByAiRequest.getName();
        String goal = genChartByAiRequest.getGoal();
        String chartType = genChartByAiRequest.getChartType();
        String data = ExcelUtils.excelToCsv(file);
        //校验
        ThrowUtils.throwIf(StringUtils.isBlank(name) && name.length() > 100, ErrorCode.NOT_FOUND_ERROR, "图标名为空");
        ThrowUtils.throwIf(StringUtils.isBlank(goal) && goal.length() > 1024, ErrorCode.NOT_FOUND_ERROR, "分析目标为空");
        ThrowUtils.throwIf(StringUtils.isBlank(chartType), ErrorCode.NOT_FOUND_ERROR, "图表类型为空");
        //校验文件
        long size = file.getSize();
        //定义一个预设最大限度10兆,
        ThrowUtils.throwIf(size > FileConstant.FILE_MAX_SIZE, ErrorCode.PARAMS_ERROR, "文件过大，上传失败");
        //校验文件名后缀
        String originalFilename = file.getOriginalFilename();
        String suffix = FileUtil.getSuffix(originalFilename);
        if (suffix == null) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "后缀名解析错误");
        }
        ThrowUtils.throwIf(!suffix.equals(FileConstant.EXCEL_SUFFIX), ErrorCode.NOT_FOUND_ERROR, "后缀名不匹配,上传失败");

        //Ai预设
        final String prompt = "假如你是一个资深数据分析师和前端开发专家，接下来我会按照以下固定格式给你提供内容: \n" +
                "分析需求: \n" +
                "{数据分析的需求或者目标}\n" +
                "需要分析的数据: \n" +
                "{csv格式的原始数据，用,作为分隔符}\n" +
                "请根据这两部分内容，按照以下指定的格式生成内容(此外不要输出任何多余的注释，开头，结尾，符号等) \n" +
                "【【【【【【\n" +
                "{前端Echarts V5版本的 option 配置对象的js代码的Json格式，强调一下，一定要Json格式的代码，便于前端解析成Json解析器解析，合理的将数据进行可视化，这里只需要option 配置对象的js代码，不要生成任何多余的内容，比如注释}\n" +
                "【【【【【【\n" +
                "{明确的数据分析结论，越详细越好，不要生成多余的注释}";
        //用户输入填充
        StringBuilder userInput = new StringBuilder();
        userInput.append(prompt).append("\n");
        userInput.append("以下是我的提问内容，请你按照上面的输入和输出的格式，提取我下面的内容，并按上述格式生成代码和结论。").append("\n");
        userInput.append("我的分析需求:").append("\n").append(goal).append("。我需要生成的图表类型为:").append(chartType).append("\n");
        userInput.append("原始csv数据:").append("\n").append(data);
        //把用户输入保存至数据库
        User loginUser = userService.getLoginUser(request);
        //先向数据库插入除了ChartData除外的数据
        Chart chart = new Chart();
        chart.setGoal(goal);
        chart.setName(name);
        chart.setChartData(null);
        chart.setChartType(chartType);
        chart.setUserId(loginUser.getId());
        boolean save = chartService.save(chart);
        if (!save) {
            throw new BusinessException(12131, "保存图表数据失败");
        }
        //获取已保存的chartId
        //根据name查询id
        LambdaQueryWrapper<Chart> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Chart::getName, name);
        Chart one = chartService.getOne(queryWrapper);
        Long id = one.getId();
        //保存数据库
        String[] split1 = data.split("\n");
        String[] split2 = split1[0].split(",");
        List<String> list = Arrays.asList(split2);
        //获取长度，循环写拼接sql
        int length = list.size();
        StringBuilder stringBuilder = new StringBuilder();
        String sql1 = "create table chartData_" + id +
                "(id  bigint auto_increment  primary key,";
        stringBuilder.append(sql1);
        for (int k = 0; k < length; k++) {
            if (k == length - 1) {
                stringBuilder.append(list.get(k)).append(" varchar(128) null");
            } else {
                stringBuilder.append(list.get(k)).append(" varchar(128) null,");
            }
        }
        stringBuilder.append(")");
        //拼接完成
        String sql = stringBuilder.toString();
        //执行建表语句
        chartService.createChartDataTable(sql);
        //获取新建表的字段
        List<String> tableColumns = chartService.getTableColumns("chartData_" + id);
        StringBuilder builder = new StringBuilder();
        builder.append("insert into chartData_").append(id).append(" (");
        for (int k = 1; k < tableColumns.size(); k++) {
            if (k == tableColumns.size() - 1) {
                builder.append(tableColumns.get(k));
            } else {
                builder.append(tableColumns.get(k)).append(",");
            }
        }
        builder.append(") values (");
        //把builder转化为String不可变对象,不然下面的每次拼接都会在上次的基础上拼接
        String insertBuilder = builder.toString();
        StringBuilder stringBuilder1 = new StringBuilder();
        String[] split3 = data.split("\n");
        for (int i1 = 1; i1 < split3.length; i1++) {
            String[] split4 = split3[i1].split(",");
            for (int i2 = 0; i2 < split4.length; i2++) {
                String s = split4[i2];
                if (i2 == split4.length - 1) {
                    stringBuilder1.append("'").append(s).append("'");
                } else {
                    stringBuilder1.append("'").append(s).append("',");
                }
            }
            stringBuilder1.append(")");
            //执行插入的拼接sql
            String sqlGood = insertBuilder + stringBuilder1;
            chartService.insertChartTable(sqlGood);
            //把使用过的stringBuilder1清空，便于下次循环的执行
            stringBuilder1.delete(0, stringBuilder1.length());
            //TODO BUG 插入不进去  已解决
        }
        //使用Ai传输问题
        String chatResult = aiManager.doChat(userInput.toString());
        //限流判断  针对每个用户做限流，每个用户单位时间内只能执行这个方法2次
        //给每一个用户色设定一个限流器，根据key的不同来区分
        //划分粒度，针对每个方法被xx调用做限流，针对某个用户操作某个方法做限流
        // String key = 方法名+userID
        String key = "genChartByAi_" + loginUser.getId();
        //限流
        limiterManager.doRedisLimiter(key);
        //获取结果
        String[] split = chatResult.split("【【【【【【");
        ThrowUtils.throwIf(split.length < 3, ErrorCode.SYSTEM_ERROR, "Ai响应错误");
        String option = split[1].trim();  //trim()去除了首尾空格的新字符串
        int i = option.indexOf("{");
        String optionJs = option.substring(i);
        String genResult = split[2].trim();
        //打印输出
        log.info(optionJs);
        log.info(genResult);
        //向表中插入数据
        //返回
        AiResponse aiResponse = new AiResponse();
        aiResponse.setGenChart(optionJs);
        aiResponse.setGenResult(genResult);
        return ResultUtils.success(aiResponse);
    }


    /**
     * 异步生成图表
     * @param file
     * @param genChartByAiRequest
     * @param request
     * @return
     */
    @PostMapping("/gen/async")
    public BaseResponse<AiResponse> genChartAsyncByAi(@RequestPart("file") MultipartFile file,
                                                 GenChartByAiRequest genChartByAiRequest, HttpServletRequest request) {
        String name = genChartByAiRequest.getName();
        String goal = genChartByAiRequest.getGoal();
        String chartType = genChartByAiRequest.getChartType();
        String data = ExcelUtils.excelToCsv(file);
        //校验
        ThrowUtils.throwIf(StringUtils.isBlank(name) && name.length() > 100, ErrorCode.NOT_FOUND_ERROR, "图标名为空");
        ThrowUtils.throwIf(StringUtils.isBlank(goal) && goal.length() > 1024, ErrorCode.NOT_FOUND_ERROR, "分析目标为空");
        ThrowUtils.throwIf(StringUtils.isBlank(chartType), ErrorCode.NOT_FOUND_ERROR, "图表类型为空");
        //校验文件
        long size = file.getSize();
        //定义一个预设最大限度10兆,
        ThrowUtils.throwIf(size > FileConstant.FILE_MAX_SIZE,ErrorCode.PARAMS_ERROR,"文件过大，上传失败");
        //校验文件名后缀
        String originalFilename = file.getOriginalFilename();
        String suffix = FileUtil.getSuffix(originalFilename);
        if (suffix == null){
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"后缀名解析错误");
        }
        ThrowUtils.throwIf(!suffix.equals(FileConstant.EXCEL_SUFFIX),ErrorCode.NOT_FOUND_ERROR,"后缀名不匹配,上传失败");

        //Ai预设
        final String prompt = "假如你是一个资深数据分析师和前端开发专家，接下来我会按照以下固定格式给你提供内容: \n" +
                "分析需求: \n" +
                "{数据分析的需求或者目标}\n" +
                "需要分析的数据: \n" +
                "{csv格式的原始数据，用,作为分隔符}\n" +
                "请根据这两部分内容，按照以下指定的格式生成内容(此外不要输出任何多余的注释，开头，结尾，符号等) \n" +
                "【【【【【【\n" +
                "{前端Echarts V5版本的 option 配置对象的js代码的Json格式，强调一下，一定要Json格式的代码，便于前端解析成Json解析器解析，合理的将数据进行可视化，这里只需要option 配置对象的js代码，不要生成任何多余的内容，比如注释}\n" +
                "【【【【【【\n" +
                "{明确的数据分析结论，越详细越好，不要生成多余的注释}";
        //用户输入填充
        StringBuilder userInput = new StringBuilder();
        userInput.append(prompt).append("\n");
        userInput.append("以下是我的提问内容，请你按照上面的输入和输出的格式，提取我下面的内容，并按上述格式生成代码和结论。").append("\n");
        userInput.append("我的分析需求:").append("\n").append(goal).append("。我需要生成的图表类型为:").append(chartType).append("\n");
        userInput.append("原始csv数据:").append("\n").append(data);
        //执行任务之前，先把用户输入保存至数据库
        User loginUser = userService.getLoginUser(request);
        //先向数据库插入除了ChartData除外的数据
        Chart chart = new Chart();
        chart.setGoal(goal);
        chart.setName(name);
        chart.setChartData(null);
        //设置为等待状态
        chart.setStatus(ChartEnum.WAIT.getKey());
        chart.setChartType(chartType);
        chart.setUserId(loginUser.getId());
        boolean save = chartService.save(chart);
        if (!save){
            throw new BusinessException(12131,"保存图表数据失败");
        }
        //获取已保存的chartId
        //根据name查询id
        LambdaQueryWrapper<Chart> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Chart::getName,name);
        Chart one = chartService.getOne(queryWrapper);
        Long id = one.getId();
        //保存数据库
        String[] split1 = data.split("\n");
        String[] split2 = split1[0].split(",");
        List<String> list = Arrays.asList(split2);
        //获取长度，循环写拼接sql
        int length = list.size();
        StringBuilder stringBuilder = new StringBuilder();
        String sql1 = "create table chartData_" +id+
                "(id  bigint auto_increment  primary key,";
        stringBuilder.append(sql1);
        for (int k = 0; k < length; k++) {
            if (k == length - 1){
                stringBuilder.append(list.get(k)).append(" varchar(128) null");
            } else {
                stringBuilder.append(list.get(k)).append(" varchar(128) null,");
            }
        }
        stringBuilder.append(")");
        //拼接完成
        String sql = stringBuilder.toString();
        //执行建表语句
        chartService.createChartDataTable(sql);
        //获取新建表的字段
        List<String> tableColumns = chartService.getTableColumns("chartData_" + id);
        StringBuilder builder = new StringBuilder();
        builder.append("insert into chartData_").append(id).append(" (");
        for (int k = 1; k < tableColumns.size(); k++) {
            if ( k == tableColumns.size()-1){
                builder.append(tableColumns.get(k));
            } else {
                builder.append(tableColumns.get(k)).append(",");
            }
        }
        builder.append(") values (");
        //把builder转化为String不可变对象,不然下面的每次拼接都会在上次的基础上拼接
        String insertBuilder = builder.toString();
        StringBuilder stringBuilder1 = new StringBuilder();
        String[] split3 = data.split("\n");
        for (int i1 = 1; i1 < split3.length; i1++) {
            String[] split4 = split3[i1].split(",");
            for (int i2 = 0; i2 < split4.length; i2++) {
                String s = split4[i2];
                if (i2 == split4.length -1){
                    stringBuilder1.append("'").append(s).append("'");
                } else {
                    stringBuilder1.append("'").append(s).append("',");
                }
            }
            stringBuilder1.append(")");
            //执行插入的拼接sql
            String sqlGood = insertBuilder + stringBuilder1;
            chartService.insertChartTable(sqlGood);
            //把使用过的stringBuilder1清空，便于下次循环的执行
            stringBuilder1.delete(0,stringBuilder1.length());
            //TODO BUG 插入不进去  已解决
        }
        //TODO 处理任务队列满了之后的抛异常
        //新增任务,将调用Ai任务塞进去
        CompletableFuture.runAsync(() -> {
            //todo 添加超时时间。超过时间标记任务为失败，手动结束任务
            //todo 添加超时时间字段,or 给线程池添加一个任务超时参数？
            long start = System.currentTimeMillis();
            //修改ai生成图表的状态为执行中running
            one.setStatus(ChartEnum.RUNNING.getKey());
            boolean b = chartService.updateById(one);
            //判断是否更新成功
            if (!b){
                handleException(one,"更新图表执行状态失败");
                return;
            }
            //使用Ai传输问题
            String chatResult = aiManager.doChat(userInput.toString());
            long stop = System.currentTimeMillis();
            long cost = (stop - start) / 1000;
            if (cost > 60){
                handleException(one,"任务执行超时");
            }
            //限流判断  针对每个用户做限流，每个用户单位时间内只能执行这个方法2次
            //给每一个用户色设定一个限流器，根据key的不同来区分
            //划分粒度，针对每个方法被xx调用做限流，针对某个用户操作某个方法做限流
            // String key = 方法名+userID
            String key = "genChartByAi_" + loginUser.getId();
            //限流
            limiterManager.doRedisLimiter(key);
            //获取结果
            String[] split = chatResult.split("【【【【【【");
//            ThrowUtils.throwIf(split.length<3,ErrorCode.SYSTEM_ERROR,"Ai响应错误");
            if (split.length <3){
                handleException(one,"生成图表数据有误");
            }
            String option = split[1].trim();  //trim()去除了首尾空格的新字符串
            int i = option.indexOf("{");
            String optionJs = option.substring(i);
            String genResult = split[2].trim();
            //打印输出
            log.info(optionJs);
            log.info(genResult);
            //更改数据库图标状态为 成功 succeed , 失败 failed
            one.setStatus(ChartEnum.SUCCEED.getKey());
            one.setGenChart(optionJs);
            one.setGenResult(genResult);
            boolean updateResult = chartService.updateById(one);
            if (!updateResult){
                handleException(one,"更新图表成功状态失败");
            }
        },poolExecutor);
        //向表中插入数据
        //返回
        AiResponse aiResponse = new AiResponse();
        return ResultUtils.success(aiResponse);
    }


    /**
     * 捕获更新图表失败状态
     * @param one
     * @param errorMessage
     */
    private void  handleException(Chart one,String errorMessage){
        one.setStatus(ChartEnum.FAILED.getKey());
        boolean b = chartService.updateById(one);
        if (!b) {
            log.error("更新图表失败状态失败,"+one.getId()+ ","+ errorMessage);
        }
    }
}
