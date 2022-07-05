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
            downloadM3u8.handle();
            //删除前几天的文件
            downloadM3u8.deleteNdays();
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
        new Thread(() -> {
            run();
        }).start();
    }

}
