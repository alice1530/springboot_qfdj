package com.alice.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import javax.annotation.PostConstruct;
import java.text.SimpleDateFormat;

@Configuration
@EnableScheduling
public class MyScheduleTask extends CommonBean {


    @Scheduled(cron = "${qfdj.task_cron}")
    private void run() {
        try {
            long begin = System.currentTimeMillis();
            SimpleDateFormat f = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            log.info("[{}]定时任务开始......", f.format(begin));
            log.info("配置文件参数如下：");
            log.info("-----------------------");
            log.info("文件存放路径=[{}]", staticFilePath);
            log.info("定时任务时间=[{}]", taskCron);
            log.info("保留文件天数=[{}]", deleteNDays);
            log.info("失败重试次数=[{}]", retryTimes);
            log.info("下载线程个数=[{}]", downloadThreadsNumber);
            log.info("连接超时时间=[{}]", connectTimedOut);
            log.info("列表本地优先=[{}]", localListFirst);
            log.info("自定义的文本=[{}]", freedomText);
            log.info("-----------------------");
            downloadM3u8.handle();
            log.info("-----------------------");
            //删除前几天的文件
            downloadM3u8.deleteNdays();
            log.info("-----------------------");
            //生成Html页面
            createHtml.createHtml();
            long end = System.currentTimeMillis();
            log.info("[{}]定时任务结束!耗时:{}ms", f.format(end), end - begin);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //启动时执行一次
    @PostConstruct
    private void startRunOnce() {

        new Thread(() -> run()).start();
    }

}
