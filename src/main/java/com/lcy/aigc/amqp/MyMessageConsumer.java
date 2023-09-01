package com.lcy.aigc.amqp;

import com.lcy.aigc.common.ErrorCode;
import com.lcy.aigc.exception.BusinessException;
import com.lcy.aigc.manager.AiManager;
import com.lcy.aigc.model.entity.Chart;
import com.lcy.aigc.model.enums.ChartEnum;
import com.lcy.aigc.service.ChartService;
import com.rabbitmq.client.Channel;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import static com.lcy.aigc.constant.AigcConstant.AIGC_QUEUE_NAME;

@Component
@Slf4j
public class MyMessageConsumer {
    @Resource
    private ChartService chartService;


    @Resource
    private AiManager aiManager;

    /**
     * 接收消息
     *
     * @param message     消息
     * @param channel     频道
     * @param deliveryTag
     */
    //监听小李的队列
    @SneakyThrows
    @RabbitListener(queues = {AIGC_QUEUE_NAME}, ackMode = "MANUAL")
    public void receiveMessage(String message,
                               Channel channel,
                               @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag) {

        log.info("receiveMessage: {}", message);
        if (StringUtils.isBlank(message)) {
            channel.basicNack(deliveryTag, false, false);
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "参数为空");
        }
        long id = Long.parseLong(message);
        Chart one = chartService.getById(id);
        String goal = one.getGoal();
        String chartType = one.getChartType();
        //拼接表名
        String tableName = "chartdata_" + id;
        //从数据库表中查出并拼接原始数据
        List<String> tableColumns = chartService.getTableColumns(tableName);
        StringBuilder builder = new StringBuilder();

        for (int i = 1; i < tableColumns.size(); i++) {
            if (i != tableColumns.size() - 1) {
                builder.append(tableColumns.get(i)).append(",");
            } else {
                builder.append(tableColumns.get(i));
            }
        }
        //拼接完成
        String sql = "SELECT CONCAT_WS(','," + builder + ") as all_data from " + tableName;
        List<Map<Integer, String>> mapList = chartService.selectChartData(sql);

        StringBuilder finalBuilder = new StringBuilder();
        for (int i = 1; i < mapList.size(); i++) {

            List<String> values = new ArrayList<>(mapList.get(i).values()) ;
            String s = values.get(0);
            if (i != mapList.size() - 1){
                finalBuilder.append(s).append("\n");
            } else {
                finalBuilder.append(s);
            }
        }
        String data = finalBuilder.toString();


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
        try {
            long start = System.currentTimeMillis();
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
            if (cost > 60) {
                handleException(one, "任务执行超时");
                channel.basicNack(deliveryTag,false,false);
            }
            //限流判断  针对每个用户做限流，每个用户单位时间内只能执行这个方法2次
            //给每一个用户色设定一个限流器，根据key的不同来区分
            //划分粒度，针对每个方法被xx调用做限流，针对某个用户操作某个方法做限流
            // String key = 方法名+userID
//            String key = "genChartByAi_" + loginUser.getId();
            //限流
//            limiterManager.doRedisLimiter(key);
            //获取结果
            String[] split = chatResult.split("【【【【【【");
//            ThrowUtils.throwIf(split.length<3,ErrorCode.SYSTEM_ERROR,"Ai响应错误");
            if (split.length < 3) {
                handleException(one, "生成图表数据有误");
                channel.basicNack(deliveryTag,false,false);
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
            if (!updateResult) {
                handleException(one, "更新图表成功状态失败");
                channel.basicNack(deliveryTag,false,false);
            }
            channel.basicAck(deliveryTag, false); //只确认本条消息
        } catch (IOException e) {
            log.error("获取信息异常");
            channel.basicNack(deliveryTag,false,false);
        }

    }

    /**
     * 捕获更新图表失败状态
     *
     * @param one
     * @param errorMessage
     */
    private void handleException(Chart one, String errorMessage) {
        one.setStatus(ChartEnum.FAILED.getKey());
        boolean b = chartService.updateById(one);
        if (!b) {
            log.error("更新图表失败状态失败," + one.getId() + "," + errorMessage);

        }
    }

}
